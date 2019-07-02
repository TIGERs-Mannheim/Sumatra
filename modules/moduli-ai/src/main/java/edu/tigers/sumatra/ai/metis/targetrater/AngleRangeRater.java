/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A kick rater based on the {@link AngleRangeGenerator}
 */
public class AngleRangeRater implements ITargetRater
{
	@Configurable(comment = "Max time horizon to consider for moving robots", defValue = "0.3")
	private static double maxHorizon = 0.3;

	@Configurable(comment = "The time a robot needs to react to the ball movement", defValue = "0.15")
	private static double timeForBotToReact = 0.15;

	@Configurable(comment = "The angle that is considered a safe goal. Any higher angle will not improve the score", defValue = "0.2")
	private static double probablyAGoalAngle = 0.2;

	static
	{
		ConfigRegistration.registerClass("metis", AngleRangeRater.class);
	}

	private AngleRangeGenerator angleRangeGenerator = new AngleRangeGenerator();
	private double timeToKick = 0;


	private AngleRangeRater()
	{
		angleRangeGenerator.setKickSpeed(RuleConstraints.getMaxBallSpeed());
	}


	/**
	 * Create the angle rater based on a goal
	 *
	 * @param goal
	 * @return
	 */
	public static AngleRangeRater forGoal(Goal goal)
	{
		AngleRangeRater angleRangeRater = new AngleRangeRater();
		angleRangeRater.angleRangeGenerator.setExtendTriangle(true);
		angleRangeRater.angleRangeGenerator.setEndLeft(goal.getLeftPost());
		angleRangeRater.angleRangeGenerator.setEndRight(goal.getRightPost());
		return angleRangeRater;
	}


	/**
	 * Create the angle rater based on an line segment
	 *
	 * @param lineSegment
	 * @return
	 */
	public static AngleRangeRater forLineSegment(ILineSegment lineSegment)
	{
		AngleRangeRater angleRangeRater = new AngleRangeRater();
		angleRangeRater.angleRangeGenerator.setEndLeft(lineSegment.getStart());
		angleRangeRater.angleRangeGenerator.setEndRight(lineSegment.getEnd());
		return angleRangeRater;
	}


	@Override
	public Optional<IRatedTarget> rate(final IVector2 origin)
	{
		angleRangeGenerator.setStart(origin);
		angleRangeGenerator.setTimeForBotToReact(timeForBotToReact);
		angleRangeGenerator.setTimeToKick(timeToKick);

		Optional<RatedAngleRange> bestUncoveredAngleRange = angleRangeGenerator.findUncoveredAngleRanges().stream()
				.map(r -> new RatedAngleRange(r, getScoreChanceFromAngle(r.getAngleWidth())))
				.max(Comparator.comparing(RatedAngleRange::getScore));
		if (bestUncoveredAngleRange.isPresent())
		{
			RatedAngleRange range = bestUncoveredAngleRange.get();
			double angleRange = range.angleRange.getAngleWidth();
			IVector2 direction = angleRangeGenerator.getEndRight().subtractNew(angleRangeGenerator.getStart())
					.turn(angleRangeGenerator.getAngleRange().getAngleWidth() / 2 + range.angleRange.getCenterAngle());
			Optional<IVector2> target = Lines.lineFromDirection(angleRangeGenerator.getStart(), direction)
					.intersectLine(
							Lines.lineFromPoints(angleRangeGenerator.getEndLeft(), angleRangeGenerator.getEndRight()));
			return target.map(t -> RatedTarget.ratedRange(t, angleRange, bestUncoveredAngleRange.get().score));
		} else
		{
			return Optional.empty();
		}
	}


	public void setTimeToKick(final double timeToKick)
	{
		this.timeToKick = timeToKick;
	}


	public void setStraightBallConsultant(IStraightBallConsultant ballConsultant)
	{
		angleRangeGenerator.setBallConsultant(ballConsultant);
	}


	public void setObstacles(Collection<ITrackedBot> bots)
	{
		Map<BotID, MovingRobot> movingRobots = new HashMap<>();
		for (ITrackedBot bot : bots)
		{
			MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius());
			movingRobots.put(bot.getBotId(), movingRobot);
		}
		angleRangeGenerator.setMovingRobots(movingRobots);
	}


	public void setExcludedBots(final Set<BotID> excludedBots)
	{
		angleRangeGenerator.setExcludedBots(excludedBots);
	}


	private double getScoreChanceFromAngle(double angle)
	{
		return Math.min(angle / probablyAGoalAngle, 1);
	}

	private static class RatedAngleRange
	{
		AngleRange angleRange;
		double score;


		public RatedAngleRange(final AngleRange angleRange, final double score)
		{
			this.angleRange = angleRange;
			this.score = score;
		}


		public double getScore()
		{
			return score;
		}
	}
}
