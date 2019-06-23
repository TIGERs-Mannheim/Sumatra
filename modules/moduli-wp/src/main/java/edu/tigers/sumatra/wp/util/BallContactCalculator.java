/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Decide if a bot has contact to the ball atm
 */
public class BallContactCalculator
{
	private static final double BALL_POSS_TOLERANCE_HAS = 60;
	private static final double BALL_POSS_TOLERANCE_GET = 20;
	
	private final Map<BotID, Boolean> ballContactLastFrame = new HashMap<>();
	private IVector2 ballPos;
	
	private final Map<BotID, Long> lastBallContactMap = new HashMap<>();
	
	
	public long ballContact(final RobotInfo robotInfo, final Pose pose, final double center2Dribbler)
	{
		boolean ballContact = hasBallContact(robotInfo, pose, center2Dribbler);
		ballContactLastFrame.put(robotInfo.getBotId(), ballContact);
		if (ballContact)
		{
			lastBallContactMap.put(robotInfo.getBotId(), robotInfo.getTimestamp());
		}
		long timestamp = lastBallContactMap.getOrDefault(robotInfo.getBotId(), 0L);
		if (timestamp <= robotInfo.getTimestamp())
		{
			return timestamp;
		}
		return 0;
	}
	
	
	private boolean hasBallContact(final RobotInfo robotInfo, final Pose pose, final double center2Dribbler)
	{
		if (robotInfo.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
		{
			return robotInfo.isBarrierInterrupted();
		}
		return hasBallContactFromVision(robotInfo, pose, center2Dribbler);
	}
	
	
	private boolean hasBallContactFromVision(final RobotInfo robotInfo, final Pose pose, final double center2Dribbler)
	{
		double ballPossTolerance;
		if (ballContactLastFrame.getOrDefault(robotInfo.getBotId(), false))
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_HAS;
		} else
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_GET;
		}
		
		final IVector2 optimalBallPossPos = BotShape.getKickerCenterPos(pose.getPos(), pose.getOrientation(),
				center2Dribbler + Geometry.getBallRadius());
		ICircle circle = Circle.createCircle(optimalBallPossPos, ballPossTolerance);
		
		return circle.isPointInShape(ballPos);
	}
	
	
	public void setBallPos(final IVector2 ballPos)
	{
		this.ballPos = ballPos;
	}
}
