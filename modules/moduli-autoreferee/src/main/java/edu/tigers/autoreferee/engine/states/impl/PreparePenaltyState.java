/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.states.impl;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author "Lukas Magel"
 */
public class PreparePenaltyState extends AbstractAutoRefState
{
	private static final Logger	log						= Logger.getLogger(PreparePenaltyState.class);
	private static final Color		AREA_COLOR				= new Color(0, 0, 255, 150);
	
	@Configurable(comment = "[ms] The minimum time to wait before sending the start signal")
	private static long minWaitTimeMs = 5_000;
	
	@Configurable(comment = "[ms] The time to wait after all bots have come to a stop and the ball has been placed correctly")
	private static long readyWaitTimeMs = 1_500;
	
	private IVector2					penaltyMark;
	private Rectangle					keeperArea;
	private ETeamColor				shooterTeam;
	
	private Long						readyTime				= null;
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		GameState gamestate = frame.getGameState();
		shooterTeam = gamestate.getForTeam();
		penaltyMark = NGeometry.getPenaltyMark(shooterTeam.opposite());
		keeperArea = calcKeeperArea(shooterTeam.opposite());
	}
	
	
	@Override
	protected void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		
		shapes.add(new DrawableRectangle(keeperArea, AREA_COLOR));
		
		if (stillInTime(minWaitTimeMs))
		{
			return;
		}
		
		setCanProceed(true);
		
		boolean ballPlaced = checkBallPlaced(wFrame.getBall(), penaltyMark, shapes);
		boolean keeperPosCorrect = checkKeeper(frame.getRefereeMsg(), wFrame.getBots(), shapes);
		boolean kickingTeamCorrect = checkKickingTeam(wFrame.getBots(), shapes);
		boolean defendingTeamCorrect = checkDefendingTeam(frame.getRefereeMsg(), wFrame.getBots(), shapes);
		boolean ready = false;
		
		if (ballPlaced && keeperPosCorrect && kickingTeamCorrect && defendingTeamCorrect)
		{
			if (readyTime == null)
			{
				readyTime = frame.getTimestamp();
			}
			long waitTimeMS = TimeUnit.NANOSECONDS.toMillis(frame.getTimestamp() - readyTime);
			drawReadyCircle((int) ((waitTimeMS * 100) / readyWaitTimeMs), wFrame.getBall().getPos(), shapes);
			ready = (frame.getTimestamp() - readyTime) > TimeUnit.MILLISECONDS.toNanos(readyWaitTimeMs);
		} else
		{
			readyTime = null;
		}
		
		if (ready || ctx.doProceed())
		{
			RefboxRemoteCommand cmd = new RefboxRemoteCommand(Command.NORMAL_START, null);
			sendCommandIfReady(ctx, cmd, ctx.doProceed());
		}
	}
	
	
	@Override
	protected void doReset()
	{
		readyTime = null;
	}
	
	
	private boolean checkKeeper(final RefereeMsg refMsg, final IBotIDMap<ITrackedBot> bots,
			final List<IDrawableShape> shapes)
	{
		BotID keeperID = refMsg.getKeeperBotID(shooterTeam.opposite());
		ITrackedBot keeper = bots.getWithNull(keeperID);
		if (keeper == null)
		{
			log.debug("Keeper not present on the field");
			return false;
		}
		boolean keeperInsideGoal = keeperArea.isPointInShape(keeper.getPos());
		
		if (!keeperInsideGoal)
		{
			shapes.add(new DrawableCircle(keeper.getPos(), Geometry.getBotRadius() * 2, Color.RED));
		}
		return keeperInsideGoal;
	}
	
	
	private boolean checkKickingTeam(final IBotIDMap<ITrackedBot> bots, final List<IDrawableShape> shapes)
	{
		List<ITrackedBot> possibleKicker = bots.values().stream()
				.filter(ColorFilter.get(shooterTeam))
				.filter(bot -> botInPenaltyKickArea(bot.getPos()))
				.collect(Collectors.toList());
		
		if (possibleKicker.size() > 1)
		{
			possibleKicker.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2,
					Color.RED)));
			return false;
		}
		
		return true;
	}
	
	
	private boolean checkDefendingTeam(final RefereeMsg refMsg, final IBotIDMap<ITrackedBot> bots,
			final List<IDrawableShape> shapes)
	{
		BotID keeperID = refMsg.getKeeperBotID(shooterTeam.opposite());
		List<ITrackedBot> defender = bots.values().stream()
				.filter(ColorFilter.get(shooterTeam.opposite()))
				.filter(bot -> botInPenaltyKickArea(bot.getPos()))
				.filter(bot -> !bot.getBotId().equals(keeperID))
				.collect(Collectors.toList());
		
		defender.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return defender.isEmpty();
	}
	
	
	private Rectangle calcKeeperArea(final ETeamColor color)
	{
		Goal goal = NGeometry.getGoal(color);
		IVector2 topLeft = goal.getLeftPost().addNew(Vector2f.fromXY(Geometry.getBotRadius(), 0));
		IVector2 bottomRight = goal.getRightPost().addNew(Vector2f.fromXY(-Geometry.getBotRadius(), 0));
		return Rectangle.fromPoints(topLeft, bottomRight);
	}
	
	
	private boolean botInPenaltyKickArea(final IVector2 pos)
	{
		return AutoRefMath.positionInPenaltyKickArea(shooterTeam, pos, Geometry.getBotRadius());
	}
}
