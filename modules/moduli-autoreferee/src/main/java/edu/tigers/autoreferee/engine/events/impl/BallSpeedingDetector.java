/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule detects ball speed violations when the game is running.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedingDetector extends AGameEventDetector
{
	private static int				priority					= 1;
	private static final Logger	log						= Logger.getLogger(BallSpeedingDetector.class);
	
	private static final int		REQUIRED_FRAME_COUNT	= 10;
	@Configurable(comment = "[m/s] The ball is not considered to be too fast if above this threshold to prevent false positives")
	private static double			topSpeedThreshold		= 20.0d;
	
	private boolean					speedingDetected		= true;
	private int							speedingFrameCount	= 0;
	
	
	/**
	 *
	 */
	public BallSpeedingDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		double ballVelocity = frame.getWorldFrame().getBall().getVel().getLength2();
		if ((ballVelocity > AutoRefConfig.getMaxBallVelocity()) && (ballVelocity < topSpeedThreshold))
		{
			speedingFrameCount++;
			if ((speedingFrameCount >= REQUIRED_FRAME_COUNT) && (speedingDetected == false))
			{
				speedingDetected = true;
				BotID violatorID = frame.getBotLastTouchedBall().getId();
				ITrackedBot violator = frame.getWorldFrame().getBot(violatorID);
				if (violator == null)
				{
					log.debug("Ball Speed Violator disappeard from the field: " + violatorID);
					return Optional.empty();
				}
				
				IVector2 kickPos = AutoRefMath.getClosestFreekickPos(violator.getPos(), violator.getTeamColor().opposite());
				
				FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
						kickPos);
				
				SpeedViolation violation = new SpeedViolation(EGameEvent.BALL_SPEEDING, frame.getTimestamp(),
						violatorID, action, ballVelocity);
				return Optional.of(violation);
			}
		} else
		{
			reset();
		}
		
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		speedingDetected = false;
		speedingFrameCount = 0;
	}
	
}
