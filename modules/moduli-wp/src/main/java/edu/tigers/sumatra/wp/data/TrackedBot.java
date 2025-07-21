/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.bot.BotState;
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
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * A tracked (filtered, predicted) robot.
 */
public final class TrackedBot implements ITrackedBot
{
	private final long timestamp;
	private final long tAssembly;
	private final BotID botId;
	private final State botState;
	private final transient State filteredState;
	private final BallContact ballContact;
	private final RobotInfo robotInfo;
	private final double quality;
	private final boolean malFunctioning;


	private TrackedBot(final Builder builder)
	{
		timestamp = builder.timestamp;
		botId = builder.botId;
		botState = builder.state;
		filteredState = builder.filteredState;
		ballContact = builder.ballContact;
		robotInfo = builder.robotInfo;
		quality = builder.quality;
		malFunctioning = builder.malFunctioning;
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
		builder.ballContact = copy.getBallContact();
		builder.quality = copy.getQuality();
		builder.malFunctioning = copy.isMalFunctioning();
		return builder;
	}


	/**
	 * Create a stub instance with default values
	 *
	 * @param botID     required
	 * @param timestamp required
	 * @return new stub builder
	 */
	public static Builder stubBuilder(final BotID botID, final long timestamp)
	{
		return newBuilder()
				.withBotId(botID)
				.withTimestamp(timestamp)
				.withState(State.zero())
				.withLastBallContact(BallContact.def(timestamp))
				.withBotInfo(RobotInfo.stub(botID, timestamp))
				.withMalFunctioning(false);
	}


	/**
	 * Create a stub instance with default values
	 *
	 * @param botID     required
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
				.withMalFunctioning(malFunctioning)
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
		return getBotKickerPos(0.0);
	}


	@Override
	public IVector2 getBotKickerPos(double margin)
	{
		return BotShape.getKickerCenterPos(botState.getPose(), getCenter2DribblerDist() + margin);
	}


	@Override
	public IVector2 getBotKickerPosByTime(final double t)
	{
		return BotShape.getKickerCenterPos(getPosByTime(t), getOrientation(), getCenter2DribblerDist());
	}


	@Override
	public IVector2 getBotKickerPosByTime(double t, double margin)
	{
		return BotShape.getKickerCenterPos(getPosByTime(t), getOrientation(), getCenter2DribblerDist() + margin);
	}


	@Override
	public MoveConstraints getMoveConstraints()
	{
		return new MoveConstraints(getRobotInfo().getBotParams().getMovementLimits());
	}


	@Override
	public Optional<Pose> getDestinationPose()
	{
		return robotInfo.getTrajectory()
				.map(t -> t.getPositionMM(Double.MAX_VALUE))
				.map(Pose::from);
	}


	@Override
	public IVector2 getPosByTime(final double t)
	{
		final Optional<ITrajectory<IVector3>> trajectory = robotInfo.getTrajectory();
		if (trajectory.isPresent())
		{
			return trajectory.get().getPositionMM(t).getXYVector();
		}
		return getPos().addNew(getVel().multiplyNew(1000 * t));
	}


	@Override
	public IVector2 getVelByTime(final double t)
	{
		final Optional<ITrajectory<IVector3>> trajectory = robotInfo.getTrajectory();
		if (trajectory.isPresent())
		{
			return trajectory.get().getVelocity(t).getXYVector();
		}
		return getVel();
	}


	@Override
	public double getAngleByTime(final double t)
	{
		final Optional<ITrajectory<IVector3>> trajectory = robotInfo.getTrajectory();
		return trajectory.map(traj -> traj.getPosition(t).z())
				.orElseGet(this::getOrientation);
	}


	@Override
	public Optional<ITrajectory<IVector3>> getCurrentTrajectory()
	{
		return robotInfo.getTrajectory();
	}


	@Override
	public boolean isMalFunctioning()
	{
		return malFunctioning;
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
		return getBotId();
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
	public BallContact getBallContact()
	{
		return ballContact;
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
	public double getQuality()
	{
		return quality;
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
		numbers.add(robotInfo.getDribbleSpeed());
		numbers.add(robotInfo.isBarrierInterrupted() ? 1 : 0);
		numbers.add(tAssembly);
		numbers.addAll(Vector3.zero().getNumberList()); // buffered_pos
		numbers.addAll(Vector3.zero().getNumberList()); // buffered_vel
		numbers.add(0); // dist2Traj
		numbers.addAll(robotInfo.getInternalState().orElse(BotState.nan()).getNumberList());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("id", "color", "timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x",
				"acc_y", "acc_z", "visible", "kickSpeed", "isChip", "dribbleRpm", "barrierInterrupted", "tAssembly",
				"buffered_pos_x", "buffered_pos_y", "buffered_pos_z",
				"buffered_vel_x", "buffered_vel_y", "buffered_vel_z",
				"dist2Traj",
				"feedback_pos_x", "feedback_pos_y", "feedback_pos_z",
				"feedback_vel_x", "feedback_vel_y", "feedback_vel_z"
		);
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
		private BallContact ballContact;
		private RobotInfo robotInfo;
		private double quality;
		private boolean malFunctioning;


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
		 * Sets the {@code ballContact} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param ballContact the {@code ballContact} to set
		 * @return a reference to this Builder
		 */
		public Builder withLastBallContact(final BallContact ballContact)
		{
			this.ballContact = ballContact;
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
		 * Sets the {@code quality} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param quality the {@code quality} to set
		 * @return a reference to this Builder
		 */
		public Builder withQuality(final double quality)
		{
			this.quality = quality;
			return this;
		}


		/**
		 * Sets the {@code malFunctioning} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param malFunctioning the {@code malFunctioning} to set
		 * @return a reference to this Builder
		 */
		public Builder withMalFunctioning(final boolean malFunctioning)
		{
			this.malFunctioning = malFunctioning;
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
			Validate.notNull(ballContact);
			Validate.notNull(robotInfo);

			return new TrackedBot(this);
		}
	}
}
