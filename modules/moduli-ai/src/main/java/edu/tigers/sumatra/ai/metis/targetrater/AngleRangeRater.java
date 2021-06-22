/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A kick rater based on the {@link AngleRangeGenerator}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AngleRangeRater implements ITargetRater
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@Configurable(comment = "Max time horizon to consider for moving robots", defValue = "2.0")
	private static double maxHorizon = 2.0;

	@Configurable(comment = "The time a robot needs to react to the ball movement", defValue = "0.3")
	private static double timeForBotToReact = 0.3;

	@Configurable(comment = "The angle that is considered a safe goal. Any higher angle will not improve the score", defValue = "0.3")
	private static double probablyAGoalAngle = 0.3;

	static
	{
		ConfigRegistration.registerClass("metis", AngleRangeRater.class);
	}

	private final MovingObstacleGen movingObstacleGen = new MovingObstacleGen();
	private final AngleRangeGenerator angleRangeGenerator;

	@Setter
	private Collection<ITrackedBot> obstacles = Collections.emptyList();

	/**
	 * Take scare when settings this value.
	 * Setting timeToKick is usually too pessimistic, because
	 * 1. The opponent may not detect the redirect, so robots will not react to a goal kick, before the actual kick
	 * 2. The time may get so high that the kick is never possible
	 */
	@Setter
	private double timeToKick = 0;

	@Getter
	private LastContext lastContext = new LastContext();


	/**
	 * Create the angle rater based on a goal
	 *
	 * @param goal
	 * @return
	 */
	public static AngleRangeRater forGoal(Goal goal)
	{
		return new AngleRangeRater(AngleRangeGenerator.forGoal(goal));
	}


	@Override
	public Optional<IRatedTarget> rate(final IVector2 origin)
	{
		movingObstacleGen.setMaxHorizon(maxHorizon);
		movingObstacleGen.setTimeForBotToReact(timeForBotToReact);

		lastContext.origin = origin;
		lastContext.circleObstacles = movingObstacleGen.generateCircles(obstacles, origin, timeToKick);
		lastContext.uncoveredAngleRanges = angleRangeGenerator
				.findUncoveredAngleRanges(origin, lastContext.circleObstacles);

		return lastContext.uncoveredAngleRanges
				.stream()
				.map(r -> new RatedAngleRange(r, getScoreChanceFromAngle(r.getWidth())))
				.max(Comparator.comparing(RatedAngleRange::getScore))
				.flatMap(r -> map(origin, r));
	}


	private Optional<IRatedTarget> map(IVector2 start, RatedAngleRange ratedAngleRange)
	{
		return angleRangeGenerator.getPoint(start, ratedAngleRange.angleRange.getOffset())
				.map(target -> RatedTarget
						.ratedRange(target, ratedAngleRange.angleRange.getWidth(), ratedAngleRange.score));
	}


	private double getScoreChanceFromAngle(double angle)
	{
		return Math.min((angle / probablyAGoalAngle), 1);
	}


	@Override
	public List<IDrawableShape> createDebugShapes()
	{
		if (lastContext.origin == null)
		{
			return Collections.emptyList();
		}
		var obstacleCircles = lastContext.drawableObstacles();
		var ranges = lastContext.drawableAngleRanges();
		var time2KickShape = List.of(new DrawableAnnotation(lastContext.origin, DF.format(timeToKick)));
		return Stream.of(obstacleCircles, ranges, time2KickShape)
				.flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());
	}


	@Value
	private static class RatedAngleRange
	{
		AngleRange angleRange;
		double score;
	}

	@Data
	public class LastContext
	{
		private IVector2 origin;
		private List<ICircle> circleObstacles;
		private List<AngleRange> uncoveredAngleRanges;


		public List<IDrawableShape> drawableObstacles()
		{
			return circleObstacles.stream()
					.map(c -> new DrawableCircle(c).setColor(new Color(232, 36, 36, 164)).setFill(true))
					.collect(Collectors.toUnmodifiableList());
		}


		public List<IDrawableShape> drawableAngleRanges()
		{
			IVector2 start = angleRangeGenerator.getLineSegment().getStart();
			IVector2 end = angleRangeGenerator.getLineSegment().getEnd();

			IVector2 endCenter = TriangleMath.bisector(origin, end, start);
			double baseAngle = endCenter.subtractNew(origin).getAngle();

			List<IDrawableShape> shapes = new ArrayList<>(uncoveredAngleRanges.size());
			for (var range : uncoveredAngleRanges)
			{
				IVector2 p1 = origin.addNew(Vector2.fromAngleLength(baseAngle + range.getRight(), 10000));
				IVector2 p2 = origin.addNew(Vector2.fromAngleLength(baseAngle + range.getLeft(), 10000));
				shapes.add(new DrawableTriangle(Triangle.fromCorners(origin, p1, p2))
						.setColor(new Color(255, 190, 0, 164))
						.setFill(true));
			}

			return shapes;
		}
	}
}
