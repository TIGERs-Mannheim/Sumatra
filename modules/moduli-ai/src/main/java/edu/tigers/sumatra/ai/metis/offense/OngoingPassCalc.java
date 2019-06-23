/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OngoingPassInfo;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Dominik Engelhardt
 */
public class OngoingPassCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(OngoingPassCalc.class);
	private IPassTarget ongoingPass = null;
	private IPassTarget previousTarget = null;
	private long passStart = 0;
	
	private static final String PASS_END_DETECTED = "End of pass detected. Reason: ";
	private BaseAiFrame baseAiFrame;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		this.baseAiFrame = baseAiFrame;
		detectPassStart();
		
		detectPassEnd();
		
		writeToTacticalField(newTacticalField);
		if (ongoingPass != null)
		{
			DrawableCircle circle = new DrawableCircle(baseAiFrame.getWorldFrame().getBall().getPos(), 100, Color.white);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING).add(circle);
		}
	}
	
	
	private void detectPassStart()
	{
		IPassTarget currentPassTarget = baseAiFrame.getPrevFrame().getAICom().getPassTarget();
		if (currentPassTarget != previousTarget)
		{
			if (currentPassTarget == null)
			{
				ongoingPass = previousTarget;
				passStart = baseAiFrame.getWorldFrame().getTimestamp();
				log.debug("Pass to " + ongoingPass.getBotId() + " started.");
			}
			previousTarget = baseAiFrame.getPrevFrame().getAICom().getPassTarget();
		}
	}
	
	
	private void detectPassEnd()
	{
		if (ongoingPass == null)
		{
			return;
		}
		if (timeExpired() || ballStopped() || hitOtherBot() || leftField())
		{
			ongoingPass = null;
		}
	}
	
	
	private boolean timeExpired()
	{
		boolean timeExpired = baseAiFrame.getWorldFrame().getTimestamp() > passStart + (long) 2e9;
		if (timeExpired)
		{
			logEnd("Time expired.");
		}
		return timeExpired;
	}
	
	
	private boolean ballStopped()
	{
		boolean ballStopped = baseAiFrame.getWorldFrame().getBall().getVel().getLength() < 0.1;
		if (ballStopped)
		{
			logEnd("Ball stopped.");
		}
		return ballStopped;
	}
	
	
	private boolean hitOtherBot()
	{
		Collection<ITrackedBot> bots = baseAiFrame.getWorldFrame().getBots().values();
		ITrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		boolean hitOtherBot = bots.stream().anyMatch(bot -> isBallNearBot(ball, bot));
		if (hitOtherBot)
		{
			logEnd("Ball hit other bot.");
		}
		return hitOtherBot;
	}
	
	
	private boolean isBallNearBot(final ITrackedBall ball, final ITrackedBot bot)
	{
		return bot.getPos().distanceTo(ball.getPos()) < 100
				&& ball.getHeight() < bot.getRobotInfo().getBotParams().getDimensions().getHeight();
	}
	
	
	private boolean leftField()
	{
		boolean leftField = !Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos());
		if (leftField)
		{
			logEnd("Ball left filed.");
		}
		return leftField;
	}
	
	
	private void writeToTacticalField(TacticalField newTacticalField)
	{
		if (ongoingPass == null)
		{
			return;
		}
		double timeSinceStart = (baseAiFrame.getWorldFrame().getTimestamp() - passStart) / 1e9;
		OngoingPassInfo passInfo = new OngoingPassInfo(ongoingPass, timeSinceStart);
		newTacticalField.setOngoingPassInfo(passInfo);
	}
	
	
	private void logEnd(String reason)
	{
		log.debug(PASS_END_DETECTED + reason);
	}
}
