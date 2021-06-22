/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.IKickEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * A kick event from simulation
 */
@Persistent
@AllArgsConstructor
public class SimKickEvent implements IKickEvent
{
	private final IVector2 pos;
	private final BotID kickingBot;
	private final long timestamp;
	@Getter
	private final IVector2 kickingBotPosition;
	@Getter
	private final double botDirection;
	@Getter
	private final BallState kickBallState;


	@SuppressWarnings("unused")
	private SimKickEvent()
	{
		pos = Vector2.zero();
		kickingBot = BotID.noBot();
		timestamp = 0;
		kickingBotPosition = Vector2f.ZERO_VECTOR;
		botDirection = 0;
		kickBallState = new BallState();
	}
	
	@Override
	public IVector2 getPosition()
	{
		return pos;
	}
	
	
	@Override
	public BotID getKickingBot()
	{
		return kickingBot;
	}
	
	
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	@Override
	public IKickEvent mirrored()
	{
		return new SimKickEvent(pos.multiplyNew(-1), kickingBot, timestamp, kickingBotPosition.multiplyNew(-1),
				botDirection + AngleMath.DEG_180_IN_RAD, kickBallState);
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pos", pos)
				.append("kickingBot", kickingBot)
				.append("timestamp", timestamp)
				.append("kickingBotPosition", kickingBotPosition)
				.append("botDirection", botDirection)
				.toString();
	}
}
