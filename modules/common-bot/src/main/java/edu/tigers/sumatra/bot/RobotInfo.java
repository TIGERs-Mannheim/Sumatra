/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import org.apache.commons.lang.Validate;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 3)
public class RobotInfo implements IMirrorable<RobotInfo>
{
	private final BotID botId;
	private final long timestamp;
	private final EBotType type;
	private final ERobotMode robotMode;
	private final ITrajectory<IVector3> trajectory;
	private final Map<EFeature, EFeatureState> botFeatures;
	private final float kickSpeed;
	private final boolean chip;
	private final boolean armed;
	private final float batteryRelative;
	private final float kickerLevelRelative;
	private final float dribbleRpm;
	private final int hardwareId;
	private final BotState internalState;
	private final boolean isBarrierInterrupted;
	private final IBotParams botParams;
	private final boolean isOk;
	private final boolean isAvailableToAi;


	@SuppressWarnings("unused")
	private RobotInfo()
	{
		this(stubBuilder(BotID.noBot(), 0));
	}


	private RobotInfo(final Builder builder)
	{
		botId = builder.botId;
		timestamp = builder.timestamp;
		type = builder.type;
		robotMode = builder.robotMode;
		trajectory = builder.trajectory;
		botFeatures = builder.botFeatures;
		kickSpeed = builder.kickSpeed;
		chip = builder.chip;
		armed = builder.armed;
		batteryRelative = builder.batteryRelative;
		kickerLevelRelative = builder.kickerLevelRelative;
		dribbleRpm = builder.dribbleRpm;
		hardwareId = builder.hardwareId;
		internalState = builder.internalState;
		isBarrierInterrupted = builder.barrierInterrupted;
		botParams = builder.botParams;
		isOk = builder.isOk;
		isAvailableToAi = builder.isAvailableToAi;
	}


	/**
	 * @return a new builder
	 */
	public static Builder newBuilder()
	{
		return new Builder();
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
		return stubBuilder(botId, timestamp).build();
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
		return newBuilder()
				.withBotId(botId)
				.withTimestamp(timestamp)
				.withType(EBotType.UNKNOWN)
				.withRobotMode(ERobotMode.IDLE)
				.withTrajectory(null)
				.withBotFeatures(new EnumMap<>(EFeature.class))
				.withKickSpeed(0)
				.withChip(false)
				.withArmed(false)
				.withBatteryRelative(0)
				.withKickerLevelRelative(0)
				.withDribbleRpm(0)
				.withHardwareId(255)
				.withInternalState(null)
				.withBarrierInterrupted(false)
				.withBotParams(new BotParams())
				.withOk(true)
				.withAvailableToAi(true);
	}


	/**
	 * @param copy for inital values
	 * @return new builder
	 */
	private static Builder copyBuilder(final RobotInfo copy)
	{
		Builder builder = new Builder();
		builder.botId = copy.botId;
		builder.timestamp = copy.timestamp;
		builder.type = copy.type;
		builder.robotMode = copy.robotMode;
		builder.trajectory = copy.trajectory;
		builder.botFeatures = copy.botFeatures;
		builder.kickSpeed = copy.kickSpeed;
		builder.chip = copy.chip;
		builder.armed = copy.armed;
		builder.batteryRelative = copy.batteryRelative;
		builder.kickerLevelRelative = copy.kickerLevelRelative;
		builder.dribbleRpm = copy.dribbleRpm;
		builder.hardwareId = copy.hardwareId;
		builder.internalState = copy.internalState;
		builder.barrierInterrupted = copy.isBarrierInterrupted;
		builder.botParams = copy.botParams;
		builder.isOk = copy.isOk;
		builder.isAvailableToAi = copy.isAvailableToAi;
		return builder;
	}


	/**
	 * @return new deep copy
	 */
	public RobotInfo copy()
	{
		return RobotInfo.copyBuilder(this).build();
	}


	@Override
	public RobotInfo mirrored()
	{
		Builder builder = copyBuilder(this);
		if (internalState != null)
		{
			builder.withInternalState(internalState.mirrored());
		}
		if (trajectory != null)
		{
			builder.withTrajectory(trajectory.mirrored());
		}
		return builder.build();
	}


	public boolean isConnected()
	{
		return type != EBotType.UNKNOWN;
	}


	public BotID getBotId()
	{
		return botId;
	}


	public long getTimestamp()
	{
		return timestamp;
	}


	public EBotType getType()
	{
		return type;
	}


	public ERobotMode getRobotMode()
	{
		return robotMode;
	}


	public double getCenter2DribblerDist()
	{
		return botParams.getDimensions().getCenter2DribblerDist();
	}


	public IBotParams getBotParams()
	{
		return botParams;
	}


	public Optional<ITrajectory<IVector3>> getTrajectory()
	{
		return Optional.ofNullable(trajectory);
	}


	public Map<EFeature, EFeatureState> getBotFeatures()
	{
		return botFeatures;
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


	public float getDribbleRpm()
	{
		return dribbleRpm;
	}


	public int getHardwareId()
	{
		return hardwareId;
	}


	public boolean isOk()
	{
		return isOk;
	}


	public boolean isAvailableToAi()
	{
		return isAvailableToAi;
	}


	public boolean isBarrierInterrupted()
	{
		return isBarrierInterrupted;
	}


	public Optional<BotState> getInternalState()
	{
		return Optional.ofNullable(internalState);
	}


	/**
	 * {@code RobotInfo} builder static inner class.
	 */
	public static final class Builder
	{
		private BotID botId;
		private Long timestamp;
		private EBotType type;
		private ERobotMode robotMode;
		private ITrajectory<IVector3> trajectory;
		private Map<EFeature, EFeatureState> botFeatures;
		private Float kickSpeed;
		private Boolean chip;
		private Boolean armed;
		private Float batteryRelative;
		private Float kickerLevelRelative;
		private Float dribbleRpm;
		private int hardwareId;
		private BotState internalState;
		private boolean barrierInterrupted;
		private IBotParams botParams;
		private boolean isOk = true;
		private boolean isAvailableToAi;


		private Builder()
		{
		}


		/**
		 * Sets the {@code botId} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code botId} to set
		 * @return a reference to this Builder
		 */
		public Builder withBotId(final BotID val)
		{
			botId = val;
			return this;
		}


		/**
		 * Sets if the robot is ok (thus usable for the AI)
		 *
		 * @param ok
		 * @return
		 */
		public Builder withOk(final boolean ok)
		{
			isOk = ok;
			return this;
		}


		public Builder withAvailableToAi(final boolean val)
		{
			this.isAvailableToAi = val;
			return this;
		}


		/**
		 * Sets the {@code timestamp} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code timestamp} to set
		 * @return a reference to this Builder
		 */
		public Builder withTimestamp(final long val)
		{
			timestamp = val;
			return this;
		}


		/**
		 * Sets the {@code type} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code type} to set
		 * @return a reference to this Builder
		 */
		public Builder withType(final EBotType val)
		{
			type = val;
			return this;
		}


		/**
		 * Sets the {@code robotMode} and returns a reference to this Builder so that
		 * the methods can be chained together.
		 *
		 * @param val the (@code robotMode) to set
		 * @return a reference to this builder
		 */
		public Builder withRobotMode(final ERobotMode val)
		{
			robotMode = val;
			return this;
		}


		/**
		 * Sets the {@code botParams} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code botParams} to set
		 * @return a reference to this Builder
		 */
		public Builder withBotParams(final IBotParams val)
		{
			botParams = val;
			return this;
		}


		/**
		 * Sets the {@code trajectory} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code trajectory} to set
		 * @return a reference to this Builder
		 */
		public Builder withTrajectory(final ITrajectory<IVector3> val)
		{
			trajectory = val;
			return this;
		}


		/**
		 * Sets the {@code botFeatures} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code botFeatures} to set
		 * @return a reference to this Builder
		 */
		public Builder withBotFeatures(final Map<EFeature, EFeatureState> val)
		{
			botFeatures = val;
			return this;
		}


		/**
		 * Sets the {@code kickSpeed} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code kickSpeed} to set
		 * @return a reference to this Builder
		 */
		public Builder withKickSpeed(final double val)
		{
			kickSpeed = (float) val;
			return this;
		}


		/**
		 * Sets the {@code chip} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code chip} to set
		 * @return a reference to this Builder
		 */
		public Builder withChip(final boolean val)
		{
			chip = val;
			return this;
		}


		/**
		 * Sets the {@code arm} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code arm} to set
		 * @return a reference to this Builder
		 */
		public Builder withArmed(final boolean val)
		{
			armed = val;
			return this;
		}


		/**
		 * Sets the {@code batteryRelative} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code batteryRelative} to set
		 * @return a reference to this Builder
		 */
		public Builder withBatteryRelative(final double val)
		{
			batteryRelative = (float) val;
			return this;
		}


		/**
		 * Sets the {@code kickerLevelRelative} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code kickerLevelRelative} to set
		 * @return a reference to this Builder
		 */
		public Builder withKickerLevelRelative(final double val)
		{
			kickerLevelRelative = (float) val;
			return this;
		}


		/**
		 * Sets the {@code dribbleRpm} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code dribbleRpm} to set
		 * @return a reference to this Builder
		 */
		public Builder withDribbleRpm(final double val)
		{
			dribbleRpm = (float) val;
			return this;
		}


		/**
		 * Sets the {@code hardwareId} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code hardwareId} to set
		 * @return a reference to this Builder
		 */
		public Builder withHardwareId(final int val)
		{
			hardwareId = val;
			return this;
		}


		/**
		 * Sets the {@code internalState} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param internalState the {@code internalState} to set
		 * @return a reference to this Builder
		 */
		public Builder withInternalState(final BotState internalState)
		{
			this.internalState = internalState;
			return this;
		}


		/**
		 * Sets the {@code barrierInterrupted} and returns a reference to this Builder so that the methods can be
		 * chained
		 * together.
		 *
		 * @param isBarrierInterrupted the {@code barrierInterrupted} to set
		 * @return a reference to this Builder
		 */
		public Builder withBarrierInterrupted(final boolean isBarrierInterrupted)
		{
			barrierInterrupted = isBarrierInterrupted;
			return this;
		}


		/**
		 * Returns a {@code RobotInfo} built from the parameters previously set.
		 *
		 * @return a {@code RobotInfo} built with parameters of this {@code RobotInfo.Builder}
		 */
		public RobotInfo build()
		{
			Validate.notNull(botId);
			Validate.notNull(type);
			Validate.notNull(robotMode);
			Validate.notNull(botParams);
			Validate.notNull(botFeatures);
			Validate.notNull(kickSpeed);
			Validate.notNull(chip);
			Validate.notNull(armed);
			Validate.notNull(batteryRelative);
			Validate.notNull(kickerLevelRelative);
			Validate.notNull(dribbleRpm);
			return new RobotInfo(this);
		}
	}
}
