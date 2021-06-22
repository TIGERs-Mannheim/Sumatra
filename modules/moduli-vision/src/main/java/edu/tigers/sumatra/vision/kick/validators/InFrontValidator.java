/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.validators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;

import java.util.List;


/**
 * Check if all balls are in front of the first bot.
 *
 * @author AndreR
 */
public class InFrontValidator implements IKickValidator
{
	@Configurable(defValue = "70.0", comment = "Minimum distance from bot to lead point on orientation line")
	private static double	minDistanceInFront		= 70.0;

	@Configurable(defValue = "40.0", comment = "Minimum distance from orientation line to ball position (distance is increased with distance from bot)")
	private static double	minDistanceOrthogonal	= 40.0;

	static
	{
		ConfigRegistration.registerClass("vision", InFrontValidator.class);
	}


	@Override
	public String getName()
	{
		return "InFront";
	}


	@Override
	public boolean validateKick(final List<FilteredVisionBot> bots, final List<MergedBall> balls)
	{
		FilteredVisionBot bot = bots.get(0);

		Line orientLine = Line.fromDirection(bot.getPos(), Vector2.fromAngle(bot.getOrientation()));

		for (MergedBall b : balls)
		{
			Vector2 leadPoint = orientLine.leadPointOf(b.getCamPos());

			if (!orientLine.isPointInFront(leadPoint))
			{
				return false;
			}

			double distBotToLeadPoint = bot.getPos().distanceTo(leadPoint);

			if (distBotToLeadPoint < minDistanceInFront)
			{
				return false;
			}

			double distBallToLeadPoint = b.getCamPos().distanceTo(leadPoint);

			if (distBallToLeadPoint > (minDistanceOrthogonal + (distBotToLeadPoint - minDistanceInFront)))
			{
				return false;
			}
		}

		return true;
	}

}
