/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class RobotInfo implements IMirrorable<RobotInfo>
{
	private final BotID botId;
	private final long timestamp;
	private final EBotType type;
	private final ERobotMode robotMode;
	private final boolean ballContact;
	private final EAiType aiType;
	private final transient ITrajectory<IVector3> trajectory;
	private final Map<EFeature, EFeatureState> botFeatures;
	private final float kickSpeed;
	private final boolean chip;
	private final float battery;
	private final float kickerVoltage;
	private final float dribbleRpm;
	private final int hardwareId;
	private final IVector3 internalPose;
	private final IVector3 internalVel;
	private final boolean isBarrierInterrupted;
	private final IBotParams botParams;
	
	
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
		ballContact = builder.ballContact;
		aiType = builder.aiType;
		botParams = builder.botParams;
		trajectory = builder.trajectory;
		botFeatures = builder.botFeatures;
		kickSpeed = builder.kickSpeed;
		chip = builder.chip;
		battery = builder.battery;
		kickerVoltage = builder.kickerVoltage;
		dribbleRpm = builder.dribbleRpm;
		hardwareId = builder.hardwareId;
		internalPose = builder.internalPose;
		internalVel = builder.internalVel;
		isBarrierInterrupted = builder.isBarrierInterrupted;
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
	 * @param botId the botId is still required
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
	 * @param botId the botId is still required
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
				.withBallContact(false)
				.withAiType(EAiType.NONE)
				.withBotParams(new BotParams())
				.withTrajectory(null)
				.withBotFeatures(new HashMap<>())
				.withKickSpeed(0)
				.withChip(false)
				.withBattery(0)
				.withKickerVoltage(0)
				.withDribbleRpm(0)
				.withHardwareId(255)
				.withInternalPose(null)
				.withInternalVel(null);
	}
	
	
	/**
	 * @param copy for inital values
	 * @return new builder
	 */
	public static Builder copyBuilder(final RobotInfo copy)
	{
		Builder builder = new Builder();
		builder.botId = copy.botId;
		builder.timestamp = copy.timestamp;
		builder.type = copy.type;
		builder.robotMode = copy.robotMode;
		builder.ballContact = copy.ballContact;
		builder.aiType = copy.aiType;
		builder.botParams = copy.botParams;
		builder.trajectory = copy.trajectory;
		builder.botFeatures = copy.botFeatures;
		builder.kickSpeed = copy.kickSpeed;
		builder.chip = copy.chip;
		builder.battery = copy.battery;
		builder.kickerVoltage = copy.kickerVoltage;
		builder.dribbleRpm = copy.dribbleRpm;
		builder.hardwareId = copy.hardwareId;
		builder.internalPose = copy.internalPose;
		builder.internalVel = copy.internalVel;
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
		if (internalPose != null)
		{
			IVector2 pos = internalPose.getXYVector().multiplyNew(-1);
			double orientation = AngleMath.normalizeAngle(internalPose.z() + AngleMath.PI);
			builder.withInternalPose(Vector3.from2d(pos, orientation));
		}
		if (internalVel != null)
		{
			IVector2 vel = internalVel.getXYVector().multiplyNew(-1);
			double zVel = internalVel.z();
			builder.withInternalVel(Vector3.from2d(vel, zVel));
		}
		return builder.build();
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
	
	
	public boolean isBallContact()
	{
		return ballContact;
	}
	
	
	public EAiType getAiType()
	{
		return aiType;
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
	
	
	public float getBattery()
	{
		return battery;
	}
	
	
	public float getKickerVoltage()
	{
		return kickerVoltage;
	}
	
	
	public float getDribbleRpm()
	{
		return dribbleRpm;
	}
	
	
	public int getHardwareId()
	{
		return hardwareId;
	}
	
	
	/**
	 * @return the internal bot position, if known
	 */
	public Optional<IVector3> getInternalPose()
	{
		return Optional.ofNullable(internalPose);
	}
	
	
	/**
	 * @return the internal bot velocity, if known
	 */
	public Optional<IVector3> getInternalVel()
	{
		return Optional.ofNullable(internalVel);
	}
	
	
	public boolean isBarrierInterrupted()
	{
		return isBarrierInterrupted;
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
		private Boolean ballContact;
		private EAiType aiType;
		private ITrajectory<IVector3> trajectory;
		private Map<EFeature, EFeatureState> botFeatures;
		private Float kickSpeed;
		private Boolean chip;
		private Float battery;
		private Float kickerVoltage;
		private Float dribbleRpm;
		private int hardwareId;
		private IVector3 internalPose;
		private IVector3 internalVel;
		private boolean isBarrierInterrupted;
		private IBotParams botParams;
		
		
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
		 * Sets the {@code ballContact} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code ballContact} to set
		 * @return a reference to this Builder
		 */
		public Builder withBallContact(final boolean val)
		{
			ballContact = val;
			return this;
		}
		
		
		/**
		 * Sets the {@code aiType} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code aiType} to set
		 * @return a reference to this Builder
		 */
		public Builder withAiType(final EAiType val)
		{
			aiType = val;
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
		 * Sets the {@code battery} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param val the {@code battery} to set
		 * @return a reference to this Builder
		 */
		public Builder withBattery(final double val)
		{
			battery = (float) val;
			return this;
		}
		
		
		/**
		 * Sets the {@code kickerVoltage} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param val the {@code kickerVoltage} to set
		 * @return a reference to this Builder
		 */
		public Builder withKickerVoltage(final double val)
		{
			kickerVoltage = (float) val;
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
		 * Sets the {@code internalPose} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param internalPose the {@code internalPose} to set
		 * @return a reference to this Builder
		 */
		public Builder withInternalPose(final IVector3 internalPose)
		{
			this.internalPose = internalPose;
			return this;
		}
		
		
		/**
		 * Sets the {@code internalVel} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param internalVel the {@code internalVel} to set
		 * @return a reference to this Builder
		 */
		public Builder withInternalVel(final IVector3 internalVel)
		{
			this.internalVel = internalVel;
			return this;
		}
		
		
		/**
		 * Sets the {@code isBarrierInterrupted} and returns a reference to this Builder so that the methods can be
		 * chained
		 * together.
		 *
		 * @param isBarrierInterrupted the {@code isBarrierInterrupted} to set
		 * @return a reference to this Builder
		 */
		public Builder withBarrierInterrupted(final boolean isBarrierInterrupted)
		{
			this.isBarrierInterrupted = isBarrierInterrupted;
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
			Validate.notNull(ballContact);
			Validate.notNull(aiType);
			Validate.notNull(botParams);
			Validate.notNull(botFeatures);
			Validate.notNull(kickSpeed);
			Validate.notNull(chip);
			Validate.notNull(battery);
			Validate.notNull(kickerVoltage);
			Validate.notNull(dribbleRpm);
			return new RobotInfo(this);
		}
	}
}
