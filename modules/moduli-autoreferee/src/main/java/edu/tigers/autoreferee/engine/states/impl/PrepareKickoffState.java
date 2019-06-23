/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.states.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This rule waits for the teams to settle and take their positions before a kickoff. When all bots are on their sides
 * and the ball/bots are stationary the kickoff command is issued.
 * 
 * @author "Lukas Magel"
 */
public class PrepareKickoffState extends AbstractAutoRefState
{
	@Configurable(comment = "[ms] The minimum time to wait before sending the kickoff signal", defValue = "4000")
	private static long minWaitTimeMs = 4_000;
	
	@Configurable(comment = "[ms] The minimum time to wait before sending the kickoff signal", defValue = "1500")
	private static long readyWaitTimeMs = 1_500;
	
	static
	{
		AbstractAutoRefState.registerClass(PrepareKickoffState.class);
	}
	
	private Long readyWaitTime = null;
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		if (stillInTime(minWaitTimeMs))
		{
			return;
		}
		setCanProceed(true);
		
		ETeamColor shooterTeam = frame.getGameState().getForTeam();
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		ITrackedBall ball = wFrame.getBall();
		
		boolean ballIsPlaced = checkBallPlaced(ball, Geometry.getCenter(), shapes);
		
		boolean botPosCorrect = checkBotsOnCorrectSide(frame, shapes)
				&& checkBotDistance(frame, shooterTeam.opposite(), shapes);
		
		boolean readyWaitTimeOver = false;
		
		if (ballIsPlaced && botPosCorrect)
		{
			if (readyWaitTime == null)
			{
				readyWaitTime = frame.getTimestamp();
			}
			long waitTime = TimeUnit.NANOSECONDS.toMillis(frame.getTimestamp() - readyWaitTime);
			readyWaitTimeOver = waitTime > readyWaitTimeMs;
			drawReadyCircle((int) ((waitTime * 100) / readyWaitTimeMs), ball.getPos(), shapes);
		} else
		{
			readyWaitTime = null;
		}
		
		if (readyWaitTimeOver || ctx.doProceed())
		{
			sendCommandIfReady(ctx, new RefboxRemoteCommand(Command.NORMAL_START, null), ctx.doProceed());
		}
	}
	
	
	private boolean checkBotDistance(final IAutoRefFrame frame, final ETeamColor color,
			final List<IDrawableShape> shapes)
	{
		Collection<ITrackedBot> bots = AutoRefUtil.filterByColor(frame.getWorldFrame().getBots(), color);
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		return checkBotStopDistance(bots, ballPos, shapes);
	}
	
	
	@Override
	protected void doReset()
	{
		readyWaitTime = null;
	}
}
