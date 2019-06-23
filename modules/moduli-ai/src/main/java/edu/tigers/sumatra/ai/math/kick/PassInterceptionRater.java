/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math.kick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.DefenseMath.ReceiveData;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
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
	@Configurable(comment = "Upper slack time bound", defValue = "1.5")
	private static double upperSlackTimeBound = 1.5;
	
	@Configurable(comment = "Lower slack time bound", defValue = "-1.5")
	private static double lowerSlackTimeBound = -1.5;
	
	@Configurable(comment = "Lower distance bound for alternative pass rating", defValue = "100.0")
	private static double passRatingLowerDist = 100.0;
	
	@Configurable(comment = "Upper distance bound for alternative pass rating", defValue = "2000.0")
	private static double passRatingUpperDist = 2000.0;
	
	static
	{
		ConfigRegistration.registerClass("support", PassInterceptionRater.class);
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
		double chipVel = ball.getChipConsultant().getInitVelForDistAtTouchdown(distance, 4);
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
	
	
	/**
	 * @param ball
	 * @param chipKick
	 * @param consideredBots
	 * @param target
	 * @param shooter
	 * @param timeStamp
	 * @return
	 */
	public static double getScoreForPass(final List<ITrackedBot> consideredBots, final ITrackedBot shooter,
			final IPassTarget target,
			final ITrackedBall ball, final long timeStamp, final boolean chipKick)
	{
		ABallTrajectory passTrajectory = generateBallTrajectory(shooter, target, chipKick, ball, timeStamp);
		List<ITrackedBot> filteredBots = filterPossibleInterceptors(shooter, passTrajectory,
				consideredBots, chipKick);
		List<Double> slackTimes = calcSlackTimesForBots(filteredBots, passTrajectory, target.getKickerPos());
		Double score = slackTimes.stream()
				.map(time -> 1 - SumatraMath.relative(time, lowerSlackTimeBound, upperSlackTimeBound))
				.reduce((a, b) -> a * b).orElse(0.);
		return 1 - score;
	}
	
	
	/**
	 * @param consideredBots
	 * @param shooter
	 * @param target
	 * @param ball
	 * @param timeStamp
	 * @return
	 */
	public static double getScoreForPassAlternative(final List<ITrackedBot> consideredBots, final ITrackedBot shooter,
			final IPassTarget target,
			final ITrackedBall ball, final long timeStamp)
	{
		ABallTrajectory passTrajectory = generateBallTrajectory(shooter, target, false, ball, timeStamp);
		
		List<ReceiveData> ratings = DefenseMath.calcReceiveRatingsFor(passTrajectory, consideredBots, passRatingUpperDist)
				.stream().sorted((r1, r2) -> Double.compare(r1.getDistToBallCurve(), r2.getDistToBallCurve()))
				.collect(Collectors.toList());
		
		if (ratings.isEmpty())
		{
			return 1.0;
		}
		
		return SumatraMath.relative(ratings.get(0).getDistToBallCurve(), passRatingLowerDist, passRatingUpperDist);
	}
	
	
	private static List<Double> calcSlackTimesForBots(final List<ITrackedBot> consideredBots,
			final ABallTrajectory passTrajectory,
			final IVector2 receivePosition)
	{
		List<Double> slackTimes = new ArrayList<>();
		for (ITrackedBot bot : consideredBots)
		{
			BangBangTrajectory2D botTrajectory = generateBotTrajectory(bot, passTrajectory, receivePosition);
			slackTimes.add(calcBotSlackTime(botTrajectory, passTrajectory, receivePosition));
		}
		return slackTimes;
	}
	
	
	private static ABallTrajectory generateBallTrajectory(final ITrackedBot offensiveBot, final IPassTarget passTarget,
			final boolean isChipKickRequired, final ITrackedBall ball, final long timeStamp)
	{
		double passVelocity = OffensiveMath.calcPassSpeedRedirect(passTarget.getTimeUntilReachedInS(timeStamp),
				ball.getPos(),
				passTarget.getKickerPos(), Geometry.getGoalTheir().getCenter());
		IVector2 passDirection = passTarget.getKickerPos().subtractNew(offensiveBot.getPos())
				.scaleTo(passVelocity * 1000);
		IVector3 kickVel = Vector3.from2d(passDirection, 0.);
		if (isChipKickRequired)
		{
			IVector2 xyVect = ball.getChipConsultant().absoluteKickVelToVector(passVelocity * 1000);
			kickVel = Vector3.from2d(passDirection.scaleToNew(xyVect.x()), xyVect.y());
		}
		return BallFactory.createTrajectoryFromKick(offensiveBot.getPos(),
				kickVel,
				isChipKickRequired);
		
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
	
	
	private static List<ITrackedBot> filterPossibleInterceptors(final ITrackedBot passingBot,
			final ABallTrajectory passTrajectory, final List<ITrackedBot> bots,
			final boolean isChipKickRequired)
	{
		if (isChipKickRequired)
		{
			double distance = passTrajectory.getTravelLinesInterceptable().stream()
					.map(iLine -> iLine.supportVector().distanceTo(passingBot.getPos())).findFirst().orElse(0.);
			
			return bots.stream()
					.filter(bot -> bot.getPos().subtractNew(passingBot.getPos()).getLength2() >= distance)
					.collect(Collectors.toList());
		}
		return bots;
	}
	
	
	/**
	 * calc simplified slackTime of bot to ball trajectory
	 *
	 * @param ballTrajectory
	 * @param botTrajectory
	 * @param target
	 * @return
	 */
	private static double calcBotSlackTime(final BangBangTrajectory2D botTrajectory,
			final ABallTrajectory ballTrajectory,
			final IVector2 target)
	{
		IVector2 interception = botTrajectory.getFinalDestination();
		double ballToInterception = ballTrajectory.getTimeByPos(interception);
		double ballToTarget = ballTrajectory.getTimeByPos(target);
		double botToInterception = botTrajectory.getTotalTime();
		if ((Double.compare(ballToInterception, Double.POSITIVE_INFINITY) == 0) || (ballToInterception > ballToTarget))
		{
			return botToInterception - ballToTarget;
		}
		return botToInterception - ballToInterception;
		
	}
	
	
	private static BangBangTrajectory2D generateBotTrajectory(final ITrackedBot bot,
			final ABallTrajectory passTrajectory,
			final IVector2 passReceiver)
	{
		ILineSegment passLine = Lines.segmentFromPoints(passTrajectory.getKickPos().getXYVector(), passReceiver);
		IVector2 targetPosition = passLine.closestPointOnLine(bot.getPos());
		if (Geometry.getPenaltyAreaTheir().isPointInShapeOrBehind(targetPosition))
		{
			Optional<IVector2> target = Geometry.getPenaltyAreaTheir().lineIntersections(passLine.toLegacyLine()).stream()
					.min(Comparator.comparingDouble(p -> p.distanceTo(bot.getPos())));
			if (target.isPresent())
			{
				return TrajectoryGenerator.generatePositionTrajectory(bot, target.get());
			}
		}
		return TrajectoryGenerator.generatePositionTrajectory(bot,
				targetPosition);
	}
	
}
