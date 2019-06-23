/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Simple data holder describing tracked bots
 *
 * @author Gero
 */
@Persistent(version = 2)
public final class TrackedBot implements ITrackedBot
{
	private final long timestamp;
	private final long tAssembly;
	private final BotID botId;
	private final State botState;
	private final transient State filteredState;
	private final transient State bufferedTrajState;
	private final long lastBallContact;
	private final RobotInfo robotInfo;
	
	
	@SuppressWarnings("unused")
	private TrackedBot()
	{
		timestamp = 0;
		botId = BotID.noBot();
		botState = State.of(Pose.from(Vector3.zero()), Vector3.zero());
		filteredState = State.of(Pose.from(Vector3.zero()), Vector3.zero());
		bufferedTrajState = null;
		lastBallContact = 0;
		robotInfo = null;
		tAssembly = 0;
	}
	
	
	private TrackedBot(final Builder builder)
	{
		timestamp = builder.timestamp;
		botId = builder.botId;
		botState = builder.state;
		filteredState = builder.filteredState;
		bufferedTrajState = builder.bufferedTrajState;
		lastBallContact = builder.lastBallContact;
		robotInfo = builder.robotInfo;
		tAssembly = System.nanoTime();
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
		builder.state = copy.getBotState();
		builder.filteredState = copy.getFilteredState().orElse(null);
		builder.robotInfo = copy.getRobotInfo();
		builder.lastBallContact = copy.getLastBallContact();
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
				.withState(State.zero())
				.withLastBallContact(0)
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
				.withState(botState.mirrored())
				.withFilteredState(filteredState == null ? null : filteredState.mirrored())
				.withBotInfo(robotInfo.mirrored())
				.build();
	}
	
	
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	@Override
	public IBotShape getBotShape()
	{
		double botRadius = robotInfo.getBotParams().getDimensions().getDiameter() / 2;
		return BotShape.fromFullSpecification(getPos(), botRadius, getCenter2DribblerDist(),
				getOrientation());
	}
	
	
	@Override
	public IVector2 getBotKickerPos()
	{
		return BotShape.getKickerCenterPos(botState.getPose(), getCenter2DribblerDist());
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
		return botState.getPos();
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return botState.getVel2();
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
		return botState.getOrientation();
	}
	
	
	@Override
	public double getAngularVel()
	{
		return botState.getAngularVel();
	}
	
	
	@Override
	public boolean hasBallContact()
	{
		return hadBallContact(0.2);
	}
	
	
	@Override
	public boolean hadBallContact(double horizon)
	{
		return (timestamp - lastBallContact) * 1e-9 < horizon;
	}
	
	
	@Override
	public long getLastBallContact()
	{
		return lastBallContact;
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
	public State getBotState()
	{
		return botState;
	}
	
	
	@Override
	public Optional<State> getFilteredState()
	{
		return Optional.ofNullable(filteredState);
	}
	
	
	@Override
	public Optional<State> getBufferedTrajState()
	{
		return Optional.ofNullable(bufferedTrajState);
	}
	
	
	@Override
	public String toString()
	{
		return "TrackedBot [id=" +
				botId +
				", pos=" +
				getPos() +
				", vel=" +
				getVel() +
				", orientation=" +
				getOrientation() +
				", angularVel=" +
				getAngularVel() +
				"]";
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(getBotId().getNumber());
		numbers.addAll(getBotId().getTeamColor().getNumberList());
		numbers.add(timestamp);
		numbers.addAll(Vector3.from2d(getPos(), getOrientation()).getNumberList());
		numbers.addAll(Vector3.from2d(getVel(), getAngularVel()).getNumberList());
		numbers.addAll(Vector3.zero().getNumberList());
		numbers.add(filteredState == null ? 0 : 1);
		numbers.add(robotInfo.getKickSpeed());
		numbers.add(robotInfo.isChip() ? 1 : 0);
		numbers.add(robotInfo.getDribbleRpm());
		numbers.add(robotInfo.isBarrierInterrupted() ? 1 : 0);
		numbers.add(tAssembly);
		return numbers;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("id", "color", "timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x",
				"acc_y", "acc_z", "visible", "kickSpeed", "isChip", "dribbleRpm", "barrierInterrupted", "tAssembly");
	}
	
	/**
	 * {@code TrackedBot} builder static inner class.
	 */
	public static final class Builder
	{
		private BotID botId;
		private Long timestamp;
		private State state;
		private State filteredState;
		private State bufferedTrajState;
		private Long lastBallContact;
		private RobotInfo robotInfo;
		
		
		private Builder()
		{
		}
		
		
		public BotID getBotId()
		{
			return botId;
		}
		
		
		public RobotInfo getRobotInfo()
		{
			return robotInfo;
		}
		
		
		private void initState()
		{
			if (state == null)
			{
				state = State.zero();
			}
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
		 * Sets the {@code state} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param botState the {@code state} to set
		 * @return a reference to this Builder
		 */
		public Builder withState(final State botState)
		{
			this.state = botState;
			return this;
		}
		
		
		/**
		 * Sets the {@code filteredState} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param filteredState the {@code filteredState} to set
		 * @return a reference to this Builder
		 */
		public Builder withFilteredState(final State filteredState)
		{
			this.filteredState = filteredState;
			return this;
		}
		
		
		/**
		 * Sets the {@code bufferedTrajState} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param bufferedTrajState the {@code bufferedTrajState} to set
		 * @return a reference to this Builder
		 */
		public Builder withBufferedTrajState(final State bufferedTrajState)
		{
			this.bufferedTrajState = bufferedTrajState;
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
			initState();
			state = State.of(Pose.from(pos, state.getOrientation()), state.getVel3());
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
			initState();
			state = State.of(state.getPose(), Vector3.from2d(vel, state.getAngularVel()));
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
			initState();
			state = State.of(Pose.from(state.getPos(), orientation), state.getVel3());
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
			initState();
			state = State.of(state.getPose(), Vector3.from2d(state.getVel2(), angularVel));
			return this;
		}
		
		
		/**
		 * Sets the {@code lastBallContact} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param lastBallContact the {@code lastBallContact} to set
		 * @return a reference to this Builder
		 */
		public Builder withLastBallContact(final long lastBallContact)
		{
			this.lastBallContact = lastBallContact;
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
			Validate.notNull(state);
			Validate.notNull(lastBallContact);
			Validate.notNull(robotInfo);
			
			return new TrackedBot(this);
		}
	}
}