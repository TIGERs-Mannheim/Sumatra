/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * A kick event from simulation
 */
@Persistent
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


	@SuppressWarnings("unused")
	private SimKickEvent()
	{
		position = Vector2.zero();
		kickingBot = BotID.noBot();
		timestamp = 0;
		kickingBotPosition = Vector2f.ZERO_VECTOR;
		botDirection = 0;
		kickBallState = new BallState();
	}
}
