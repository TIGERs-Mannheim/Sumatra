/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(setterPrefix = "with")
public class FilteredVisionKick
{
	long kickTimestamp;
	long trajectoryStartTime;

	BotID kickingBot;
	IVector2 kickingBotPosition;
	double kickingBotOrientation;

	long numBallDetectionsSinceKick;

	IBallTrajectory ballTrajectory;
}
