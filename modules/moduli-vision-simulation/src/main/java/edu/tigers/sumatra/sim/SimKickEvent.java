/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * A kick event from simulation
 */
@Value
@AllArgsConstructor
public class SimKickEvent
{
	IVector2 position;
	BotID kickingBot;
	long timestamp;
	IVector2 kickingBotPosition;
	double botDirection;
	BallState kickBallState;
}
