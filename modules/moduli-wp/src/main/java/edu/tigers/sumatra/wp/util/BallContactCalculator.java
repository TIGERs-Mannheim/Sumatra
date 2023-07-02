/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.BallContact;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Decide if a bot has contact to the ball atm
 */
public class BallContactCalculator
{
	private static final double BALL_POSS_TOLERANCE_HAS = 60;
	private static final double BALL_POSS_TOLERANCE_GET = 20;

	private IVector2 ballPos;

	private final Map<BotID, Boolean> ballContactLastFrame = new HashMap<>();
	private final Map<BotID, Long> startBallContactMap = new HashMap<>();
	private final Map<BotID, Long> endBallContactMap = new HashMap<>();


	public BallContact ballContact(final RobotInfo robotInfo, final Pose pose, final double center2Dribbler)
	{
		boolean ballContactFromVision = hasBallContactFromVision(robotInfo, pose, center2Dribbler);
		boolean ballContact = hasBallContact(robotInfo).orElse(ballContactFromVision);
		ballContactLastFrame.put(robotInfo.getBotId(), ballContact);
		if (ballContact)
		{
			startBallContactMap.putIfAbsent(robotInfo.getBotId(), robotInfo.getTimestamp());
			endBallContactMap.put(robotInfo.getBotId(), robotInfo.getTimestamp());
		} else
		{
			startBallContactMap.remove(robotInfo.getBotId());
		}

		return new BallContact(
				robotInfo.getTimestamp(),
				startBallContactMap.getOrDefault(robotInfo.getBotId(), (long) -1e9),
				endBallContactMap.getOrDefault(robotInfo.getBotId(), (long) -1e9),
				ballContactFromVision
		);
	}


	private Optional<Boolean> hasBallContact(final RobotInfo robotInfo)
	{
		if (robotInfo.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
		{
			return Optional.of(robotInfo.isBarrierInterrupted());
		}
		return Optional.empty();
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
		return optimalBallPossPos.distanceTo(ballPos) < ballPossTolerance;
	}


	public void setBallPos(final IVector2 ballPos)
	{
		this.ballPos = ballPos;
	}


	public void reset()
	{
		ballContactLastFrame.clear();
		startBallContactMap.clear();
		endBallContactMap.clear();
	}
}
