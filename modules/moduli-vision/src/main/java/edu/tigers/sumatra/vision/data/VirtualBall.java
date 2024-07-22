/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(setterPrefix = "with", toBuilder = true)
public class VirtualBall
{
	long timestamp;
	/**
	 * Adjusted position due to line-of-sight changes or barrier information.
	 */
	IVector3 position;
	/**
	 * Raw position as reported by robot.
	 */
	IVector3 observedPosition;
	/**
	 * From where this ball was observed.
	 */
	IVector2 observedFromPosition;
	/**
	 * Which bot reported this virtual ball.
	 */
	BotID observingBot;
	/**
	 * Where was the reporting bot.
	 */
	Pose observingBotPose;
	/**
	 * Is this a ball reported from barrier contact?
	 */
	boolean fromBarrier;


	public CamBall toCamBall(final int camId, final long frameId)
	{
		return new CamBall(0, 0, position, Vector2f.ZERO_VECTOR,
				timestamp, timestamp, timestamp, camId, frameId);
	}
}
