/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Simple data holder describing tracked bots
 *
 * @author Gero
 */
@Persistent(version = 1)
public final class TrackedBot implements ITrackedBot
{
	private final long		timestamp;
	private final BotID		botId;
	private final IVector2	pos;
	private final IVector2	vel;
	private final double		orientation;
	private final double		angularVel;
	private final boolean	visible;
	private final RobotInfo	robotInfo;
	
	
	@SuppressWarnings("unused")
	private TrackedBot()
	{
		timestamp = 0;
		botId = BotID.noBot();
		pos = AVector2.ZERO_VECTOR;
		vel = AVector2.ZERO_VECTOR;
		orientation = 0;
		angularVel = 0;
		visible = false;
		robotInfo = null;
	}
	
	
	private TrackedBot(final Builder builder)
	{
		timestamp = builder.timestamp;
		botId = builder.botId;
		pos = builder.pos;
		vel = builder.vel;
		orientation = builder.orientation;
		angularVel = builder.angularVel;
		visible = builder.visible;
		robotInfo = builder.robotInfo;
	}
	
	
	/**
	 * @return new builder
	 */
	public static Builder newBuilder()
	{
		return new Builder();
	}
	
	
	/**
	 * @param copy all fields will be initialized with the copy
	 * @return new builder
	 */
	public static Builder newCopyBuilder(final ITrackedBot copy)
	{
		Builder builder = new Builder();
		builder.botId = copy.getBotId();
		builder.timestamp = copy.getTimestamp();
		builder.pos = copy.getPos();
		builder.vel = copy.getVel();
		builder.orientation = copy.getOrientation();
		builder.angularVel = copy.getAngularVel();
		builder.visible = copy.isVisible();
		builder.robotInfo = copy.getRobotInfo();
		return builder;
	}
	
	
	/**
	 * Create a stub instance with default values
	 *
	 * @param botID required
	 * @param timestamp required
	 * @return new stub builder
	 */
	public static Builder stubBuilder(final BotID botID, final long timestamp)
	{
		return newBuilder()
				.withBotId(botID)
				.withTimestamp(timestamp)
				.withPos(AVector2.ZERO_VECTOR)
				.withOrientation(0)
				.withVel(AVector2.ZERO_VECTOR)
				.withAngularVel(0)
				.withVisible(false)
				.withBotInfo(RobotInfo.stub(botID, timestamp));
	}
	
	
	/**
	 * Create a stub instance with default values
	 * 
	 * @param botID required
	 * @param timestamp required
	 * @return new stub
	 */
	public static TrackedBot stub(final BotID botID, final long timestamp)
	{
		return stubBuilder(botID, timestamp).build();
	}
	
	
	@Override
	public ITrackedBot mirrored()
	{
		return newCopyBuilder(this)
				.withPos(pos.multiplyNew(-1))
				.withVel(vel.multiplyNew(-1))
				.withOrientation(AngleMath.normalizeAngle(orientation + AngleMath.PI))
				.withBotInfo(robotInfo.mirrored())
				.build();
	}
	
	
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	@Override
	public IVector2 getBotKickerPos()
	{
		return BotShape.getKickerCenterPos(getPos(), getOrientation(), getCenter2DribblerDist());
	}
	
	
	@Override
	public IVector2 getBotKickerPosByTime(final double t)
	{
		
		return BotShape.getKickerCenterPos(getPosByTime(t), getOrientation(), getCenter2DribblerDist());
	}
	
	
	@Override
	public MoveConstraints getMoveConstraints()
	{
		return new MoveConstraints(getRobotInfo().getBotParams().getMovementLimits());
	}
	
	
	@Override
	public IVector2 getPosByTime(final double t)
	{
		if (robotInfo.getTrajectory().isPresent())
		{
			return robotInfo.getTrajectory().get().getPositionMM(t).getXYVector();
		}
		return getPos().addNew(getVel().multiplyNew(1000 * t));
	}
	
	
	@Override
	public IVector2 getVelByTime(final double t)
	{
		if (robotInfo.getTrajectory().isPresent())
		{
			return robotInfo.getTrajectory().get().getVelocity(t).getXYVector();
		}
		return getVel();
	}
	
	
	@Override
	public double getAngleByTime(final double t)
	{
		if (robotInfo.getTrajectory().isPresent())
		{
			robotInfo.getTrajectory().get().getPosition(t).z();
		}
		return getOrientation();
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public IVector2 getAcc()
	{
		return AVector2.ZERO_VECTOR;
	}
	
	
	@Override
	public AObjectID getId()
	{
		return botId;
	}
	
	
	@Override
	public BotID getBotId()
	{
		return botId;
	}
	
	
	@Override
	public ETeamColor getTeamColor()
	{
		return getBotId().getTeamColor();
	}
	
	
	@Override
	public double getOrientation()
	{
		return orientation;
	}
	
	
	@Override
	public double getAngularVel()
	{
		return angularVel;
	}
	
	
	@Override
	public double getaAcc()
	{
		return 0;
	}
	
	
	@Override
	public boolean hasBallContact()
	{
		return robotInfo.isBallContact();
	}
	
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	
	
	@Override
	public double getCenter2DribblerDist()
	{
		return robotInfo.getCenter2DribblerDist();
	}
	
	
	@Override
	public RobotInfo getRobotInfo()
	{
		return robotInfo;
	}
	
	
	@Override
	public String toString()
	{
		return "TrackedBot [id=" +
				botId +
				", pos=" +
				pos +
				", vel=" +
				vel +
				", orientation=" +
				orientation +
				", angularVel=" +
				angularVel +
				"]";
	}
	
	
	/**
	 * {@code TrackedBot} builder static inner class.
	 */
	public static final class Builder
	{
		private BotID		botId;
		private Long		timestamp;
		private IVector2	pos;
		private IVector2	vel;
		private Double		orientation;
		private Double		angularVel;
		private Boolean	visible;
		private RobotInfo	robotInfo;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code botId} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param botId the {@code botId} to set
		 * @return a reference to this Builder
		 */
		public Builder withBotId(final BotID botId)
		{
			this.botId = botId;
			return this;
		}
		
		
		/**
		 * Sets the {@code timestamp} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param timestamp the {@code timestamp} to set
		 * @return a reference to this Builder
		 */
		public Builder withTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
			return this;
		}
		
		
		/**
		 * Sets the {@code pos} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param pos the {@code pos} to set
		 * @return a reference to this Builder
		 */
		public Builder withPos(final IVector2 pos)
		{
			this.pos = pos;
			return this;
		}
		
		
		/**
		 * Sets the {@code vel} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param vel the {@code vel} to set
		 * @return a reference to this Builder
		 */
		public Builder withVel(final IVector2 vel)
		{
			this.vel = vel;
			return this;
		}
		
		
		/**
		 * Sets the {@code orientation} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param orientation the {@code orientation} to set
		 * @return a reference to this Builder
		 */
		public Builder withOrientation(final double orientation)
		{
			this.orientation = orientation;
			return this;
		}
		
		
		/**
		 * Sets the {@code angularVel} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param angularVel the {@code angularVel} to set
		 * @return a reference to this Builder
		 */
		public Builder withAngularVel(final double angularVel)
		{
			this.angularVel = angularVel;
			return this;
		}
		
		
		/**
		 * Sets the {@code visible} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param visible the {@code visible} to set
		 * @return a reference to this Builder
		 */
		public Builder withVisible(final boolean visible)
		{
			this.visible = visible;
			return this;
		}
		
		
		/**
		 * Sets the {@code robotInfo} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param robotInfo the {@code robotInfo} to set
		 * @return a reference to this Builder
		 */
		public Builder withBotInfo(final RobotInfo robotInfo)
		{
			this.robotInfo = robotInfo;
			return this;
		}
		
		
		/**
		 * Returns a {@code TrackedBot} built from the parameters previously set.
		 *
		 * @return a {@code TrackedBot} built with parameters of this {@code TrackedBot.Builder}
		 */
		public TrackedBot build()
		{
			Validate.notNull(botId);
			Validate.notNull(timestamp);
			Validate.notNull(pos);
			Validate.notNull(vel);
			Validate.notNull(orientation);
			Validate.notNull(angularVel);
			Validate.notNull(visible);
			Validate.notNull(robotInfo);
			
			return new TrackedBot(this);
		}
	}
}