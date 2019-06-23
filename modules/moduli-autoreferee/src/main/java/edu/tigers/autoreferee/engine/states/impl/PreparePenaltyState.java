/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 6, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states.impl;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * @author "Lukas Magel"
 */
public class PreparePenaltyState extends AbstractAutoRefState
{
	private static final Logger	log						= Logger.getLogger(PreparePenaltyState.class);
	private static final Color		AREA_COLOR				= new Color(0, 0, 255, 150);
	
	@Configurable(comment = "[ms] The minimum time to wait before sending the start signal")
	private static long				MIN_WAIT_TIME_MS		= 5_000;
	
	@Configurable(comment = "[ms] The time to wait after all bots have come to a stop and the ball has been placed correctly")
	private static long				READY_WAIT_TIME_MS	= 1_500;
	
	private IVector2					penaltyMark;
	private Rectangle					keeperArea;
	private Rectangle					penaltyKickArea;
	private ETeamColor				shooterTeam;
	
	private Long						readyTime				= null;
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		EGameStateNeutral gamestate = frame.getGameState();
		shooterTeam = gamestate.getTeamColor();
		penaltyMark = NGeometry.getPenaltyMark(shooterTeam.opposite());
		penaltyKickArea = NGeometry.getPenaltyKickArea(shooterTeam.opposite());
		keeperArea = calcKeeperArea(shooterTeam.opposite());
	}
	
	
	@Override
	protected void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		List<IDrawableShape> shapes = frame.getShapes().get(EWpShapesLayer.AUTOREFEREE);
		
		shapes.add(new DrawableRectangle(penaltyKickArea, AREA_COLOR));
		shapes.add(new DrawableRectangle(keeperArea, AREA_COLOR));
		
		if (!timeElapsedSinceEntry(MIN_WAIT_TIME_MS))
		{
			return;
		}
		
		setCanProceed(true);
		
		boolean ballPlaced = checkBallPlaced(wFrame.getBall(), penaltyMark, shapes);
		boolean keeperPosCorrect = checkKeeper(frame.getRefereeMsg(), wFrame.getBots(), shapes);
		boolean kickingTeamCorrect = checkKickingTeam(wFrame.getBots(), wFrame.getBall(), shapes);
		boolean defendingTeamCorrect = checkDefendingTeam(frame.getRefereeMsg(), wFrame.getBots(), shapes);
		boolean ready = false;
		
		if (ballPlaced && keeperPosCorrect && kickingTeamCorrect && defendingTeamCorrect)
		{
			if (readyTime == null)
			{
				readyTime = frame.getTimestamp();
			}
			long waitTimeMS = TimeUnit.NANOSECONDS.toMillis(frame.getTimestamp() - readyTime);
			drawReadyCircle((int) ((waitTimeMS * 100) / READY_WAIT_TIME_MS), wFrame.getBall().getPos(), shapes);
			ready = (frame.getTimestamp() - readyTime) > TimeUnit.MILLISECONDS.toNanos(READY_WAIT_TIME_MS);
		} else
		{
			readyTime = null;
		}
		
		if ((ready == true) || ctx.doProceed())
		{
			RefCommand cmd = new RefCommand(Command.NORMAL_START);
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
	
	
	private boolean checkKickingTeam(final IBotIDMap<ITrackedBot> bots, final TrackedBall ball,
			final List<IDrawableShape> shapes)
	{
		List<ITrackedBot> possibleKicker = bots.values().stream()
				.filter(ColorFilter.get(shooterTeam))
				.filter(bot -> penaltyKickArea.isPointInShape(bot.getPos(), -Geometry.getBotRadius()))
				.collect(Collectors.toList());
		
		if ((possibleKicker.size() > 1))
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
				.filter(bot -> penaltyKickArea.isPointInShape(bot.getPos(), -Geometry.getBotRadius()))
				.filter(bot -> !bot.getBotId().equals(keeperID))
				.collect(Collectors.toList());
		
		defender.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return defender.size() == 0;
	}
	
	
	private Rectangle calcKeeperArea(final ETeamColor color)
	{
		Goal goal = NGeometry.getGoal(color);
		IVector2 topLeft = goal.getGoalPostLeft().addNew(new Vector2f(Geometry.getBotRadius(), 0));
		IVector2 bottomRight = goal.getGoalPostRight().addNew(new Vector2f(-Geometry.getBotRadius(), 0));
		return new Rectangle(topLeft, bottomRight);
	}
}
