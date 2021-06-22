/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class CalibrationData
{
	long timestamp;
	ITrackedBall ball;
	BallKickFitState kickFitState;
	IVector2 rawBallPos;
	ITrackedBot bot;
	KickParams kickParams;
}
