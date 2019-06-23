/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath.ReceiveData;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class PassInterceptionRater
{
	@Configurable(comment = "Lower distance bound for alternative pass rating", defValue = "100.0")
	private static double passRatingLowerDist = 100.0;
	
	@Configurable(comment = "Upper distance bound for alternative pass rating", defValue = "2000.0")
	private static double passRatingUpperDist = 2000.0;
	
	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionRater.class);
	}
	
	
	private PassInterceptionRater()
	{
	}
	
	
	/**
	 * Calculate a score between 0 (bad) and 1 (good) for the straight line between start and end, assuming the ball is
	 * chipped.
	 * Only opponent bots that are further away from the start as ignoreDist will be considered.
	 *
	 * @param ball
	 * @param bots
	 * @param start
	 * @param end
	 * @param additionalTime a time offset [ns] for the rating (i.e. how long the shooter needs to reach start)
	 * @return a score between 0 (bad) and 1 (good)
	 */
	public static double getChipScoreForLineSegment(
			final ITrackedBall ball,
			final Collection<ITrackedBot> bots,
			final IVector2 start,
			final IVector2 end,
			final double additionalTime)
	{
		double distance = start.distanceTo(end);
		double chipVel = OffensiveMath.passSpeedChip(distance);
		double minDist = ball.getChipConsultant().getMinimumDistanceToOverChip(chipVel, 150);
		double maxDist = ball.getChipConsultant().getMaximumDistanceToOverChip(chipVel, 150);
		double timeBallReachesDest = ball.getStraightConsultant().getTimeForKick(distance, 8);
		double timeForKick = timeBallReachesDest + additionalTime;
		ILine ballLine = Line.fromPoints(start, end);
		
		return bots.stream()
				.mapToDouble(tBot -> getTimeForInterceptingChippedBall(ballLine, minDist, maxDist, tBot))
				// also use negative remaining time, because estimations are not that reliable and we do not want
				// to have all scores to be zero
				.map(timeFoeReachesBall -> timeFoeReachesBall - timeForKick)
				.map(timeRemaining -> SumatraMath.relative(timeRemaining, -5, 5))
				.min().orElse(1.0);
	}
	
	
	public static double rateStraightPass(
			final IVector2 passOrigin,
			final IVector2 passTarget,
			final Collection<ITrackedBot> consideredBots)
	{
		ABallTrajectory passTrajectory = straightBallTrajectory(passOrigin, passTarget);
		
		return rateTrajectory(passTrajectory, consideredBots);
	}
	
	
	public static double rateChippedPass(
			final IVector2 passOrigin,
			final IVector2 passTarget,
			final Collection<ITrackedBot> consideredBots)
	{
		ABallTrajectory passTrajectory = chippedBallTrajectory(passOrigin, passTarget);
		
		Collection<ITrackedBot> filteredBots = filterPossibleInterceptors(
				passOrigin,
				passTrajectory,
				consideredBots);
		
		return rateTrajectory(passTrajectory, filteredBots);
	}
	
	
	private static double rateTrajectory(final ABallTrajectory passTrajectory,
			final Collection<ITrackedBot> consideredBots)
	{
		return DefenseMath.calcReceiveRatingsFor(passTrajectory, consideredBots, passRatingUpperDist).stream()
				.min(Comparator.comparingDouble(ReceiveData::getDistToBallCurve))
				.map(ReceiveData::getDistToBallCurve)
				.map(s -> SumatraMath.relative(s, passRatingLowerDist, passRatingUpperDist))
				.orElse(1.0);
	}
	
	
	private static ABallTrajectory straightBallTrajectory(
			final IVector2 passOrigin,
			final IVector2 passTarget)
	{
		final IVector2 passReceiverTarget = Geometry.getGoalTheir().getCenter();
		
		double passVelocity = OffensiveMath.passSpeedStraight(
				passOrigin,
				passTarget,
				passReceiverTarget);
		
		IVector2 passDirection = passTarget.subtractNew(passOrigin);
		IVector3 kickVel = Vector3.from2d(passDirection.scaleToNew(passVelocity * 1000), 0.);
		
		return BallFactory.createTrajectoryFromStraightKick(passOrigin, kickVel);
	}
	
	
	private static ABallTrajectory chippedBallTrajectory(
			final IVector2 passOrigin,
			final IVector2 passTarget)
	{
		double distance = passOrigin.distanceTo(passTarget);
		double passVelocity = OffensiveMath.passSpeedChip(distance);
		
		IVector2 passDirection = passTarget.subtractNew(passOrigin);
		IVector2 xyVect = BallFactory.createChipConsultant().absoluteKickVelToVector(passVelocity);
		IVector3 kickVel = Vector3.from2d(passDirection.scaleToNew(xyVect.x() * 1000), xyVect.y());
		
		return BallFactory.createTrajectoryFromChipKick(passOrigin, kickVel);
	}
	
	
	/**
	 * Estimate the time a bot would need to intercept the chipped ball
	 *
	 * @param ballLine
	 * @param minDist
	 * @param maxDist
	 * @param tBot
	 * @return
	 */
	private static double getTimeForInterceptingChippedBall(final ILine ballLine, final double minDist,
			final double maxDist,
			final ITrackedBot tBot)
	{
		IVector2 interceptPosition = ballLine.nearestPointOnLineSegment(tBot.getPos());
		final double distanceToStart = interceptPosition.distanceTo(ballLine.getStart());
		if ((distanceToStart > minDist) && (distanceToStart < maxDist))
		{
			double distanceToMin = distanceToStart - minDist;
			double distanceToMax = maxDist - distanceToStart;
			double interceptDistance = distanceToMin < distanceToMax ? minDist : maxDist;
			interceptPosition = ballLine.directionVector().scaleToNew(interceptDistance).add(ballLine.getStart());
		}
		ITrajectory<IVector2> trajectory = TrajectoryGenerator.generatePositionTrajectory(tBot, interceptPosition);
		return trajectory.getTotalTime();
	}
	
	
	private static Collection<ITrackedBot> filterPossibleInterceptors(
			final IVector2 passingBot,
			final ABallTrajectory passTrajectory,
			final Collection<ITrackedBot> bots)
	{
		double distance = passTrajectory.getTravelLinesInterceptable().stream()
				.map(iLine -> iLine.supportVector().distanceTo(passingBot)).findFirst().orElse(0.);
		
		return bots.stream()
				.filter(bot -> bot.getPos().subtractNew(passingBot).getLength2() >= distance)
				.collect(Collectors.toList());
	}
}
