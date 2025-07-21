/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.trajectory.IBallConsultant;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import java.util.Optional;


@Log4j2
public class KickSpeedFactory
{
	@Configurable(comment = "Number of touchdowns before a chipped pass can be received", defValue = "6")
	private static int touchdownsForPasses = 6;

	static
	{
		ConfigRegistration.registerClass("metis", KickSpeedFactory.class);
	}

	public double maxChip(ITrackedBot bot)
	{
		return Math.min(
				RuleConstraints.getMaxKickSpeed(),
				Optional.ofNullable(bot)
						.map(b -> b.getRobotInfo().getBotParams().getKickerSpecs().getMaxAbsoluteChipVelocity())
						.orElse(RuleConstraints.getMaxKickSpeed())
		);
	}


	public double maxStraight(ITrackedBot bot)
	{
		return Math.min(
				RuleConstraints.getMaxKickSpeed(),
				Optional.ofNullable(bot)
						.map(b -> b.getRobotInfo().getBotParams().getKickerSpecs().getMaxAbsoluteStraightVelocity())
						.orElse(RuleConstraints.getMaxKickSpeed())
		);
	}


	public double chip(double distance, Double maxBallSpeedAtTarget, double maxKickSpeed, double minPassDuration)
	{
		if (maxBallSpeedAtTarget == null)
		{
			return maxKickSpeed;
		}
		return Math.min(maxKickSpeed, passSpeedChip(distance, maxBallSpeedAtTarget, minPassDuration));
	}


	public double straight(double distance, Double maxBallSpeedAtTarget, double maxKickSpeed, double minPassDuration)
	{
		if (maxBallSpeedAtTarget == null)
		{
			return maxKickSpeed;
		}
		return Math.min(maxKickSpeed, passSpeedStraight(distance, maxBallSpeedAtTarget, minPassDuration));
	}


	private double passSpeedStraight(double distance, double passEndVel, double minPassDuration)
	{
		var consultant = Geometry.getBallFactory().createFlatConsultant();
		var passSpeed = consultant.getInitVelForDist(distance, passEndVel);
		return adaptedPassSpeed(consultant, distance, passSpeed, minPassDuration);
	}


	private double passSpeedChip(double distance, double passEndVel, double minPassDuration)
	{
		var consultant = Geometry.getBallFactory().createChipConsultant();
		var passSpeed = consultant.getInitVelForDistAtTouchdown(distance, touchdownsForPasses);
		passSpeed = passSpeedForMaxPassEndSpeed(consultant, distance, passSpeed, passEndVel);
		return adaptedPassSpeed(consultant, distance, passSpeed, minPassDuration);
	}


	private double adaptedPassSpeed(IBallConsultant consultant, double distance, double passSpeed,
			double minPassDuration)
	{
		return passSpeedForMinTravelTime(consultant, distance, passSpeed, minPassDuration);
	}


	/**
	 * Reduce the passSpeed such that the ball travels at least <code>minTravelTime</code>.
	 *
	 * @param ballConsultant   the straight or chip consultant
	 * @param passDist         the distance that the ball should travel
	 * @param desiredPassSpeed the preferred/desired pass speed
	 * @param minTravelTime    the minimum travel time of the ball
	 * @return
	 */
	private static double passSpeedForMinTravelTime(
			IBallConsultant ballConsultant,
			double passDist,
			double desiredPassSpeed,
			double minTravelTime
	)
	{
		try
		{
			UnivariatePointValuePair result = new BrentOptimizer(0.0001, 0.001).optimize(
					GoalType.MINIMIZE,
					new MaxEval(100),
					new MaxIter(100),
					new SearchInterval(0, desiredPassSpeed, desiredPassSpeed),
					new UnivariateObjectiveFunction(passSpeed -> {
						double travelTime = ballConsultant.getTimeForKick(passDist, passSpeed);
						return Math.abs(travelTime - minTravelTime);
					}));
			return result.getPoint();
		} catch (TooManyIterationsException | TooManyEvaluationsException e)
		{
			log.debug("Could not find a solution for passDist {} for minTravelTime {}", passDist, minTravelTime);
			return 0;
		}
	}


	/**
	 * Reduce the passSpeed such that the ball has speed lower or equal to <code>maxPassEndSpeed</code>
	 * when the ball arrives at the target.
	 *
	 * @param ballConsultant   the straight or chip consultant
	 * @param passDist         the distance that the ball should travel
	 * @param desiredPassSpeed the preferred/desired pass speed
	 * @param maxPassEndSpeed  the maximum ball speed when the ball arrives at the target
	 * @return
	 */
	private static double passSpeedForMaxPassEndSpeed(
			IBallConsultant ballConsultant,
			double passDist,
			double desiredPassSpeed,
			double maxPassEndSpeed
	)
	{
		try
		{
			UnivariatePointValuePair result = new BrentOptimizer(0.0001, 0.001).optimize(
					GoalType.MINIMIZE,
					new MaxEval(100),
					new MaxIter(100),
					new SearchInterval(0, desiredPassSpeed, desiredPassSpeed),
					new UnivariateObjectiveFunction(passSpeed -> {
						double travelTime = ballConsultant.getTimeForKick(passDist, passSpeed);
						double passEndVel = ballConsultant.getVelForKickByTime(passSpeed, travelTime);
						return Math.abs(passEndVel - maxPassEndSpeed);
					}));
			return result.getPoint();
		} catch (TooManyIterationsException | TooManyEvaluationsException e)
		{
			log.debug("Could not find a solution for passDist {} for maxPassEndSpeed {}", passDist, maxPassEndSpeed);
			return 0;
		}
	}
}
