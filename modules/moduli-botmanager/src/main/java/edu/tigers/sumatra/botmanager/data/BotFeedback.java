/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.data;

import edu.tigers.sumatra.bot.BotBallState;
import edu.tigers.sumatra.bot.BotLastKickState;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBallObservationState;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.bot.EDribblerTemperature;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


/**
 * Full feedback data from a robot.
 * This class has reasonable defaults for the no-args constructor.
 * It is not required to set all fields (for implementations which don't have those).
 */
@Value
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with", builderMethodName = "newBuilder", toBuilder = true)
public class BotFeedback
{
	@Builder.Default
	long timestamp = System.nanoTime();

	@Builder.Default
	@Getter(AccessLevel.NONE)
	BotState internalState = null;

	/**
	 * [V]
	 */
	@Builder.Default
	double kickerLevel = 0.0f;

	/**
	 * [V]
	 */
	@Builder.Default
	double kickerMax = 0.0f;

	/**
	 * [m/s], dribbling bar surface speed
	 */
	@Builder.Default
	double dribbleSpeed = 0.0f;

	@Builder.Default
	EDribbleTractionState dribbleTraction = EDribbleTractionState.OFF;

	/**
	 * [V]
	 */
	@Builder.Default
	double batteryLevel = 0.0f;

	/**
	 * 0..1 => Empty..Full
	 */
	@Builder.Default
	double batteryLevelRelative = 0.0f;

	@Builder.Default
	boolean barrierInterrupted = false;
	@Builder.Default
	EDribblerTemperature dribblerTemperature = EDribblerTemperature.COLD;
	@Builder.Default
	boolean kickToggleBit = false;
	@Builder.Default
	EBallObservationState ballObservationState = EBallObservationState.UNKNOWN;
	@Builder.Default
	Map<EFeature, EFeatureState> botFeatures = new EnumMap<>(EFeature.class);
	@Builder.Default
	ERobotMode robotMode = ERobotMode.IDLE;

	/**
	 * Each bot has its own hardware id that uniquely identifies a robot by hardware (mainboard)
	 */
	@Builder.Default
	int hardwareId = 255;

	@Builder.Default
	@Getter(AccessLevel.NONE)
	BotBallState ballState = null;

	@Builder.Default
	BotLastKickState lastKick = new BotLastKickState();


	public Optional<BotState> getInternalState()
	{
		return Optional.ofNullable(internalState);
	}


	public Optional<BotBallState> getBallState()
	{
		return Optional.ofNullable(ballState);
	}


	public double getKickerLevelRelative()
	{
		if (kickerMax <= 0.0f)
		{
			return 0.0f;
		}

		return kickerLevel / kickerMax;
	}
}
