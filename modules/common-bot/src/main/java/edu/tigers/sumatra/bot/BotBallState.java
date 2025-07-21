/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Ball state as reported by a robot.
 */
@Value
@AllArgsConstructor
public class BotBallState implements IMirrorable<BotBallState>, IExportable
{
	/** Position in [mm] in vision coordinates. */
	IVector2 pos;

	/** Latency of reported position in [s]. Not including network and Sumatra delays. */
	double age;

	@Override
	public BotBallState mirrored()
	{
		return new BotBallState(pos.multiplyNew(-1), age);
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> list = new ArrayList<>(3);
		list.addAll(pos.getNumberList());
		list.add(age);
		return list;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("ball_pos_x", "ball_pos_y", "ball_age");
	}
}
