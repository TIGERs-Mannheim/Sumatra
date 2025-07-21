/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Builder(setterPrefix = "with", builderClassName = "Builder", builderMethodName = "newBuilder", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RobotInfo implements IMirrorable<RobotInfo>
{
	@Getter
	@NonNull
	private final BotID botId;
	@Getter
	private final long timestamp;
	@Getter
	@NonNull
	private final EBotType type;
	@Getter
	@NonNull
	private final ERobotMode robotMode;
	private final ITrajectory<IVector3> trajectory;
	@Getter
	@NonNull
	private final Map<EFeature, EFeatureState> botFeatures;
	@NonNull
	private final Float kickSpeed;
	@NonNull
	private final Boolean chip;
	@NonNull
	private final Boolean armed;
	@NonNull
	private final Float batteryRelative;
	@NonNull
	private final Float kickerLevelRelative;
	@NonNull
	private final Float dribbleSpeed;
	@Getter
	private final int hardwareId;
	private final BotState internalState;
	@Getter
	private final boolean barrierInterrupted;
	@Getter
	@NonNull
	private final IBotParams botParams;
	@Getter
	private final ERobotHealthState healthState;
	@Getter
	private final boolean availableToAi;
	private final EDribbleTractionState dribbleTraction;
	private final BotBallState ballState;


	private RobotInfo(final BotID botId, final long timestamp)
	{
		this.botId = botId;
		this.timestamp = timestamp;
		type = EBotType.UNKNOWN;
		robotMode = ERobotMode.IDLE;
		trajectory = null;
		botFeatures = new EnumMap<>(EFeature.class);
		kickSpeed = 0.0f;
		chip = false;
		armed = false;
		batteryRelative = 0.0f;
		kickerLevelRelative = 0.0f;
		dribbleSpeed = 0.0f;
		hardwareId = 255;
		internalState = null;
		barrierInterrupted = false;
		botParams = new BotParams();
		healthState = ERobotHealthState.READY;
		dribbleTraction = EDribbleTractionState.OFF;
		availableToAi = true;
		ballState = null;
	}


	/**
	 * Create a stub with default data
	 *
	 * @param botId     the botId is still required
	 * @param timestamp the timestamp of this information
	 * @return new bot info
	 */
	public static RobotInfo stub(final BotID botId, final long timestamp)
	{
		return new RobotInfo(botId, timestamp);
	}


	/**
	 * Create a stub with default data
	 *
	 * @param botId     the botId is still required
	 * @param timestamp the timestamp of this information
	 * @return new stub builder
	 */
	public static Builder stubBuilder(final BotID botId, final long timestamp)
	{
		return new RobotInfo(botId, timestamp).toBuilder();
	}


	/**
	 * @return new deep copy
	 */
	public RobotInfo copy()
	{
		return toBuilder().build();
	}


	@Override
	public RobotInfo mirrored()
	{
		Builder builder = toBuilder();
		if (internalState != null)
		{
			builder.withInternalState(internalState.mirrored());
		}
		if (trajectory != null)
		{
			builder.withTrajectory(trajectory.mirrored());
		}
		if(ballState != null)
		{
			builder.withBallState(ballState.mirrored());
		}
		return builder.build();
	}


	public boolean isConnected()
	{
		return type != EBotType.UNKNOWN;
	}


	public double getCenter2DribblerDist()
	{
		return botParams.getDimensions().getCenter2DribblerDist();
	}


	public Optional<ITrajectory<IVector3>> getTrajectory()
	{
		return Optional.ofNullable(trajectory);
	}


	public float getKickSpeed()
	{
		return kickSpeed;
	}


	public boolean isChip()
	{
		return chip;
	}


	public boolean isArmed()
	{
		return armed;
	}


	public float getBatteryRelative()
	{
		return batteryRelative;
	}


	public float getKickerLevelRelative()
	{
		return kickerLevelRelative;
	}


	public float getDribbleSpeed()
	{
		return dribbleSpeed;
	}


	public Optional<BotState> getInternalState()
	{
		return Optional.ofNullable(internalState);
	}


	public EDribbleTractionState getDribbleTraction()
	{
		return dribbleTraction;
	}


	public Optional<BotBallState> getBallState()
	{
		return Optional.ofNullable(ballState);
	}
}
