/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Data structure for a filtered vision ball.
 * <br>
 * <b>WARNING: Units of this class are [mm], [mm/s], [mm/s^2] !!!</b>
 */
@Value
@Builder(setterPrefix = "with")
public class FilteredVisionBall implements IExportable
{
	@NonNull
	Long timestamp;
	@NonNull
	BallState ballState;
	@NonNull
	Long lastVisibleTimestamp;


	/**
	 * Position in [mm]
	 *
	 * @return
	 */
	public IVector3 getPos()
	{
		return ballState.getPos();
	}


	/**
	 * Velocity in [mm/s]
	 *
	 * @return
	 */
	public IVector3 getVel()
	{
		return ballState.getVel();
	}


	/**
	 * Acceleration in [mm/s^2]
	 *
	 * @return
	 */
	public IVector3 getAcc()
	{
		return ballState.getAcc();
	}


	public long getLastVisibleTimestamp()
	{
		return lastVisibleTimestamp;
	}


	public IVector2 getSpin()
	{
		return ballState.getSpin();
	}


	/**
	 * Extrapolate ball by using trajectory.
	 *
	 * @param timestampNow
	 * @param timestampFuture
	 * @return
	 */
	public FilteredVisionBall extrapolate(final long timestampNow, final long timestampFuture)
	{
		if (timestampFuture < timestampNow)
		{
			return this;
		}

		long dt = timestampFuture - timestampNow;

		return FilteredVisionBall.builder()
				.withTimestamp(timestampFuture)
				.withBallState(
						Geometry.getBallFactory().createTrajectoryFromState(ballState).getMilliStateAtTime(dt * 1e-9))
				.withLastVisibleTimestamp(getLastVisibleTimestamp() + dt)
				.build();
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(timestamp);
		numbers.addAll(getPos().getNumberList());
		numbers.addAll(getVel().multiplyNew(1e-3).getNumberList());
		numbers.addAll(getAcc().multiplyNew(1e-3).getNumberList());
		numbers.add(lastVisibleTimestamp);
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x", "acc_y", "acc_z",
				"lastVisibleTimestamp");
	}
}
