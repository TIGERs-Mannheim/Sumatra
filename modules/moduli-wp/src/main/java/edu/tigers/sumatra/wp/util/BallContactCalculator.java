/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallContactCalculator
{
	private static final double			BALL_POSS_TOLERANCE_HAS	= 60;
	private static final double			BALL_POSS_TOLERANCE_GET	= 20;
	
	private final Map<BotID, Boolean>	ballContactLastFrame		= new HashMap<>();
	
	
	/**
	 * @param botID
	 * @param pos
	 * @param orientation
	 * @param center2Dribbler
	 * @param trackedBall
	 * @return true, if the ball is near to the robots kicker
	 */
	public boolean ballContact(final BotID botID, final IVector2 pos, final double orientation,
			final double center2Dribbler,
			final ITrackedBall trackedBall)
	{
		double ballPossTolerance;
		if (ballContactLastFrame.containsKey(botID) && ballContactLastFrame.get(botID))
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_HAS;
		} else
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_GET;
		}
		
		final IVector2 optimalBallPossPos = BotShape.getKickerCenterPos(pos, orientation,
				center2Dribbler + Geometry.getBallRadius());
		ICircle circle = Circle.createCircle(optimalBallPossPos, ballPossTolerance);
		
		boolean ballContact = circle.isPointInShape(trackedBall.getPos());
		ballContactLastFrame.put(botID, ballContact);
		return ballContact;
	}
}
