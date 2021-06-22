/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import java.util.Optional;


/**
 * Calculator class that determines the optimal time to intercept the ball.
 */
@Log4j2
public final class BallInterceptor
{
	@Configurable(defValue = "0.2")
	private static double tOffsetBase = 0.2;

	@Configurable(defValue = "0.8")
	private static double closestTimeThreshold = 0.8;

	@Configurable(defValue = "0.05")
	private static double accStep = 0.05;

	@Configurable(defValue = "0.3")
	private static double accOffset = 0.3;
	@Configurable(defValue = "0.2")
	private static double tOffsetYoungKick = 1.0;
	@Configurable(defValue = "6.0")
	private static double maxAcc = 6.0;

	@Configurable(defValue = "2.3")
	private static double minAcc = 2.3;
	@Configurable(defValue = "0.5")
	private static double minKickAge = 0.5;

	private final double tOffset;
	private final IBallTrajectory ballTrajectory;
	private final ITrackedBot tBot;
	private final MoveConstraints moveConstraints;

	static
	{
		ConfigRegistration.registerClass("skills", BallInterceptor.class);
	}


	private BallInterceptor(final Builder builder)
	{
		ballTrajectory = builder.ballTrajectory;
		tBot = builder.tBot;
		moveConstraints = builder.moveConstraints;

		double kickAge = Optional.ofNullable(builder.kickEvent).map(t -> (tBot.getTimestamp() - t.getTimestamp()) / 1e9)
				.orElse(minKickAge);
		if (kickAge < minKickAge)
		{
			tOffset = tOffsetBase + tOffsetYoungKick;
		} else
		{
			tOffset = tOffsetBase;
		}
	}


	/**
	 * @return a new builder for this class
	 */
	public static Builder aBallInterceptor()
	{
		return new Builder();
	}


	/**
	 * @return the time in future when the ball can best be intercepted
	 */
	public double optimalTime()
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(0.0001, 0.01, new Checker());
		double tMax = Math.max(1e-5, ballTrajectory.getTimeByVel(0));
		Optional<IVector2> fieldIntersection = ballTrajectory.getPlanarCurve()
				.getIntersectionsWithRectangle(Geometry.getField())
				.stream()
				.filter(p -> ballTrajectory.getTravelLineSegment().isPointOnLine(p))
				.findFirst();
		if (fieldIntersection.isPresent())
		{
			tMax = Math.min(tMax, ballTrajectory.getTimeByPos(fieldIntersection.get()));
		}

		double tClosest = getTimeForDestClosestToBallLine();

		if (tClosest >= tMax)
		{
			return tMax;
		}

		try
		{
			UnivariatePointValuePair result = optimizer.optimize(
					GoalType.MINIMIZE,
					new MaxEval(20),
					new MaxIter(20),
					new SearchInterval(0, tMax, tClosest),
					new UnivariateObjectiveFunction(this::absSlackTime));
			return result.getPoint();
		} catch (TooManyEvaluationsException | TooManyIterationsException e)
		{
			log.debug("Optimization failed.", e);
			return tMax;
		}
	}


	/**
	 * @return the time in future when the ball can best be intercepted, or nothing if ball can hit kicker already
	 */
	public Optional<Double> optimalTimeIfReasonable()
	{
		moveConstraints.setAccMax(minAcc);
		double optimalTime = optimalTime();
		double tClosest = getTimeForDestClosestToBallLine();
		double threshold = 0.2;
		if ((optimalTime + threshold > tClosest || tClosest < closestTimeThreshold)
				&& ballCanHitKickerWithDynAcc(tClosest))
		{
			return Optional.empty();
		}
		return Optional.of(optimalTime + tOffset);
	}


	private double getTimeForDestClosestToBallLine()
	{
		IVector2 closestDest = ballTrajectory.getTravelLineSegment().closestPointOnLine(tBot.getBotKickerPos());
		double dist2ClosestDest = ballTrajectory.getPosByTime(0).getXYVector().distanceTo(closestDest);
		double dist2Kicker = Math.max(0, dist2ClosestDest - Geometry.getBallRadius());
		return Math.max(0, ballTrajectory.getTimeByDist(dist2Kicker));
	}


	private boolean ballCanHitKicker(double tClosest)
	{
		IVector2 ballPos = ballTrajectory.getPosByTime(tClosest).getXYVector();
		ITrajectory<IVector2> traj = getBotTrajectory(ballPos);
		IBotShape botShape = BotShape.fromFullSpecification(traj.getPositionMM(tClosest), Geometry.getBotRadius(),
				tBot.getCenter2DribblerDist(), tBot.getOrientation());
		IVector2 botKickerPosAtTBall = botShape.getKickerCenterPos();

		double distToBallLine = ballTrajectory.getTravelLineSegment().distanceTo(botKickerPosAtTBall);
		return distToBallLine < botShape.getKickerWidth() / 2.0;
	}


	private boolean ballCanHitKickerWithDynAcc(double tClosest)
	{
		for (double acc = minAcc; acc < maxAcc; acc += accStep)
		{
			moveConstraints.setAccMax(acc);
			if (ballCanHitKicker(tClosest))
			{
				moveConstraints.setAccMax(Math.min(acc + accOffset, maxAcc));
				return true;
			}
		}
		moveConstraints.setAccMax(minAcc);
		return false;
	}


	private double absSlackTime(final double tBall)
	{

		IVector2 ballPos = ballTrajectory.getPosByTime(tBall).getXYVector();
		ITrajectory<IVector2> traj = getBotTrajectory(ballPos);
		double slackTime = tBall - traj.getTotalTime();
		return Math.abs(slackTime);
	}


	private ITrajectory<IVector2> getBotTrajectory(final IVector2 ballPos)
	{
		// we assume here that the target orientation is equal to the current orientation for simplicity
		// the trajectory time will not differ much, when the robot is far away. And if it is nearby,
		// it should already have the target orientation
		// we would need to pass a function for calculating the target angle otherwise to generalize for receive and
		// redirect.
		double orientation = tBot.getOrientation();
		IVector2 botDest = BotShape.getCenterFromKickerPos(ballPos, orientation, tBot.getCenter2DribblerDist());
		return TrajectoryGenerator.generatePositionTrajectory(moveConstraints, tBot.getPos(), tBot.getVel(),
				botDest);
	}


	private static class Checker implements ConvergenceChecker<UnivariatePointValuePair>
	{
		@Override
		public boolean converged(final int iteration, final UnivariatePointValuePair previous,
				final UnivariatePointValuePair current)
		{
			return false;
		}
	}


	/**
	 * {@code BallInterceptor} builder static inner class.
	 */
	public static final class Builder
	{
		private IBallTrajectory ballTrajectory;
		private ITrackedBot tBot;
		private MoveConstraints moveConstraints;
		private IKickEvent kickEvent;


		private Builder()
		{
		}


		/**
		 * Sets the {@code ballTrajectory} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param ballTrajectory the {@code ballTrajectory} to set
		 * @return a reference to this Builder
		 */
		public Builder withBallTrajectory(final IBallTrajectory ballTrajectory)
		{
			this.ballTrajectory = ballTrajectory;
			return this;
		}


		/**
		 * Sets the {@code moveConstraints} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param moveConstraints the {@code moveConstraints} to set
		 * @return a reference to this Builder
		 */
		public Builder withMoveConstraints(final MoveConstraints moveConstraints)
		{
			this.moveConstraints = moveConstraints;
			return this;
		}


		/**
		 * Set multiple fields based on the given tracked bots
		 *
		 * @param tBot a tracked bot
		 * @return a reference to this Builder
		 */
		public Builder withTrackedBot(final ITrackedBot tBot)
		{
			this.tBot = tBot;
			return this;
		}


		/**
		 * Sets the {@code kickEvent} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param kickEvent the {@code kickEvent} to set
		 * @return a reference to this Builder
		 */
		public Builder withKickEvent(final IKickEvent kickEvent)
		{
			this.kickEvent = kickEvent;
			return this;
		}


		/**
		 * Returns a {@code BallInterceptor} built from the parameters previously set.
		 *
		 * @return a {@code BallInterceptor} built with parameters of this {@code BallInterceptor.Builder}
		 */
		public BallInterceptor build()
		{
			Validate.notNull(ballTrajectory);
			Validate.notNull(tBot);
			Validate.notNull(moveConstraints);
			return new BallInterceptor(this);
		}
	}


	/**
	 * @return the min acc that is used
	 */
	public static double getMinAcc()
	{
		return minAcc;
	}


	/**
	 * @return the max acc that is used
	 */
	public static double getMaxAcc()
	{
		return maxAcc;
	}
}
