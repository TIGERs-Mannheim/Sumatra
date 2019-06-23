/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * This rule waits for the teams to settle and take their positions before a kickoff. When all bots are on their sides
 * and the ball/bots are stationary the kickoff command is issued.
 * 
 * @author "Lukas Magel"
 */
public class PrepareKickoffState extends AbstractAutoRefState
{
	@Configurable(comment = "[ms] The minimum time to wait before sending the kickoff signal")
	private static long	MIN_WAIT_TIME_MS		= 4_000;
	
	@Configurable(comment = "[ms] The minimum time to wait before sending the kickoff signal")
	private static long	READY_WAIT_TIME_MS	= 1_500;
	
	private Long			readyWaitTime			= null;
	static
	{
		AbstractAutoRefState.registerClass(PrepareKickoffState.class);
	}
	
	
	/**
	 *
	 */
	public PrepareKickoffState()
	{
	}
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		if (!timeElapsedSinceEntry(MIN_WAIT_TIME_MS))
		{
			return;
		}
		setCanProceed(true);
		
		ETeamColor shooterTeam = frame.getGameState().getTeamColor();
		List<IDrawableShape> shapes = frame.getShapes().get(EWpShapesLayer.AUTOREFEREE);
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		TrackedBall ball = wFrame.getBall();
		
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
			long waitTime_ms = TimeUnit.NANOSECONDS.toMillis(frame.getTimestamp() - readyWaitTime);
			readyWaitTimeOver = waitTime_ms > READY_WAIT_TIME_MS;
			drawReadyCircle((int) ((waitTime_ms * 100) / READY_WAIT_TIME_MS), ball.getPos(), shapes);
		} else
		{
			readyWaitTime = null;
		}
		
		if (readyWaitTimeOver || ctx.doProceed())
		{
			sendCommandIfReady(ctx, new RefCommand(Command.NORMAL_START), ctx.doProceed());
		}
	}
	
	
	private boolean checkBotDistance(final IAutoRefFrame frame, final ETeamColor color, final List<IDrawableShape> shapes)
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
