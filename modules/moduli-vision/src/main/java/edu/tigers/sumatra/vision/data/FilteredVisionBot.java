/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Data structure for a filtered robot.
 */
@Value
@Builder(setterPrefix = "with")
public class FilteredVisionBot implements IExportable
{
	@NonNull
	BotID botID;

	@NonNull
	Long timestamp;

	/**
	 * [mm,mm]
	 */
	@NonNull
	IVector2 pos;

	/**
	 * [m/s,m/s]
	 */
	@NonNull
	IVector2 vel;

	/**
	 * [rad]
	 */
	@NonNull
	Double orientation;

	/**
	 * [rad/s]
	 */
	@NonNull
	Double angularVel;

	/**
	 * 0-1
	 */
	double quality;


	/**
	 * Extrapolate bot into future.
	 *
	 * @param timestampNow
	 * @param timestampFuture
	 * @return
	 */
	public FilteredVisionBot extrapolate(final long timestampNow, final long timestampFuture)
	{
		if (timestampFuture < timestampNow)
		{
			return this;
		}

		double dt = (timestampFuture - timestampNow) * 1e-9;

		return builder()
				.withBotID(botID)
				.withTimestamp(timestampFuture)
				.withQuality(quality)
				.withPos(pos.addNew(vel.multiplyNew(dt * 1e3)))
				.withVel(vel)
				.withOrientation(AngleMath.normalizeAngle(orientation + (angularVel * dt)))
				.withAngularVel(angularVel)
				.build();
	}


	public BotState toBotState()
	{
		return BotState.of(botID, State.of(Pose.from(pos, orientation), Vector3.from2d(vel, angularVel)));
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(botID.getNumber());
		numbers.addAll(botID.getTeamColor().getNumberList());
		numbers.add(timestamp);
		numbers.addAll(Vector3.from2d(getPos(), getOrientation()).getNumberList());
		numbers.addAll(Vector3.from2d(getVel(), getAngularVel()).getNumberList());
		numbers.addAll(Vector3.zero().getNumberList());
		numbers.add(getQuality());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("id", "color", "timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x",
				"acc_y", "acc_z", "quality");
	}
}
