/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Describes a Kick Event.
 */
@Persistent
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class KickEvent implements IKickEvent
{
	BotID kickingBot;
	IVector2 kickingBotPosition;
	IVector2 position;
	double botDirection;
	long timestamp;
	@Singular("recordSinceKick")
	transient List<MergedBall> recordsSinceKick;
	boolean isEarlyDetection;


	@SuppressWarnings("unused")
	private KickEvent()
	{
		this(BotID.noBot(), Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, 0.0, 0, Collections.emptyList(), false);
	}


	@Override
	public IKickEvent mirrored()
	{
		return toBuilder()
				.position(position.multiplyNew(-1))
				.kickingBotPosition(kickingBotPosition.multiplyNew(-1))
				.botDirection(botDirection + AngleMath.DEG_180_IN_RAD)
				.build();
	}


	public boolean isEarlyDetection()
	{
		return isEarlyDetection;
	}


	public Optional<IVector2> getKickDirection()
	{
		List<IVector2> points = recordsSinceKick.stream()
				.map(MergedBall::getCamPos)
				.collect(Collectors.toList());

		Optional<Line> line = Line.fromPointsList(points);
		return line.map(Line::directionVector);
	}
}