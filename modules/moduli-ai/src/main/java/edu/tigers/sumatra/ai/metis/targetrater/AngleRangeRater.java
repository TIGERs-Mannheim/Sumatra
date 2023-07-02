/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.SumatraMath;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A kick rater based on the {@link AngleRangeGenerator}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AngleRangeRater implements ITargetRater
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@Configurable(comment = "[s] Max time horizon to consider for moving robots", defValue = "2.0")
	private static double maxHorizon = 2.0;

	@Configurable(comment = "[s] The time a robot needs to react to the ball movement", defValue = "0.085")
	private static double timeForBotToReact = 0.085;

	@Configurable(comment = "[rad] The angle that is considered a safe goal. Any higher angle will not improve the score", defValue = "0.3")
	private static double probablyAGoalAngle = 0.3;

	@Configurable(comment = "[s] maxVel that considers opponent bots are reacting", defValue = "1.5")
	private static double maxOpponentReactionVel = 1.5;

	@Configurable(defValue = "CUBIC_REDUCTION")
	private static EHorizonCalculation horizonCalculatorMode = EHorizonCalculation.CUBIC_REDUCTION;

	@Configurable(comment = "[0-1] How good can the opponents use time before kick to improve the position", defValue = "0.2")
	private static double timeBeforeReactionUsageFactor = 0.2;

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
	 * 3. Only consider timeToKick for bots within the obstacleReactionShape
	 */
	@Setter
	private double timeToKick = 0;

	@Setter
	private I2DShape obstacleReactionShape = Geometry.getField();

	@Setter
	private double opponentConsideredReactingVel = maxOpponentReactionVel;

	@Setter
	private double noneOptimalDriveFactor = 1.0;

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
		return rateMultiple(origin).stream().max(Comparator.comparing(IRatedTarget::getScore));
	}


	@Override
	public List<IRatedTarget> rateMultiple(IVector2 origin)
	{
		movingObstacleGen.setMaxHorizon(maxHorizon);
		movingObstacleGen.setTimeForBotToReact(timeForBotToReact);
		movingObstacleGen.setHorizonCalculation(horizonCalculatorMode);
		movingObstacleGen.setNoneOptimalDriveFactor(noneOptimalDriveFactor);
		movingObstacleGen.setTimeBeforeReactionUsageFactor(timeBeforeReactionUsageFactor);

		lastContext.botTimeToReact =
				obstacles.stream().collect(Collectors.toMap(Function.identity(), this::getTimeForBotToReact));

		var reactions = lastContext.botTimeToReact.entrySet().stream().collect(Collectors.toMap(
				a -> a.getKey().getBotId(), Map.Entry::getValue));

		lastContext.origin = origin;
		lastContext.circleObstacles = movingObstacleGen.generateCircles(obstacles, origin, reactions);
		lastContext.uncoveredAngleRanges = angleRangeGenerator
				.findUncoveredAngleRanges(origin, lastContext.circleObstacles);

		return lastContext.uncoveredAngleRanges
				.stream()
				.map(r -> new RatedAngleRange(r, getScoreChanceFromAngle(r.getWidth())))
				.map(r -> createTargetFromRatedAngleRange(origin, r))
				.flatMap(Optional::stream)
				.toList();
	}


	private double getTimeForBotToReact(ITrackedBot trackedBot)
	{
		// only consider bots in obstacleReactionShape
		if (obstacleReactionShape.withMargin(Geometry.getBotRadius() * 4).isPointInShape(trackedBot.getPos()))
		{
			// opponents only react if they are moving, with smooth transition.
			return timeToKick * SumatraMath.relative(trackedBot.getVel().getLength(), 0, opponentConsideredReactingVel);
		}
		return 0;
	}


	private Optional<IRatedTarget> createTargetFromRatedAngleRange(IVector2 start, RatedAngleRange ratedAngleRange)
	{
		return angleRangeGenerator.getPoint(start, ratedAngleRange.angleRange.getOffset())
				.map(target -> RatedTarget
						.ratedRange(target, Math.max(ratedAngleRange.angleRange.getWidth(), 0.0), ratedAngleRange.score));
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
		var reactions = lastContext.drawableReactionTimes();
		var time2KickShape = List.of(new DrawableAnnotation(lastContext.origin, DF.format(timeToKick)));
		return Stream.of(obstacleCircles, ranges, time2KickShape, reactions)
				.flatMap(Collection::stream).map(IDrawableShape.class::cast).toList();
	}


	@Value
	private static class RatedAngleRange
	{
		AngleRange angleRange;
		double score;
	}

	@Data
	private class LastContext
	{
		private IVector2 origin;
		private List<ICircle> circleObstacles;
		private List<AngleRange> uncoveredAngleRanges;
		private Map<ITrackedBot, Double> botTimeToReact;


		public List<IDrawableShape> drawableReactionTimes()
		{
			return botTimeToReact.entrySet().stream().map(
							e -> new DrawableAnnotation(e.getKey().getPos(), String.format("%.2f", e.getValue()),
									Vector2.zero()).setColor(Color.BLUE))
					.map(IDrawableShape.class::cast)
					.toList();
		}


		public List<IDrawableShape> drawableObstacles()
		{
			return circleObstacles.stream()
					.map(c -> new DrawableCircle(c).setColor(new Color(232, 36, 36, 164)).setFill(true))
					.toList();
		}


		public List<IDrawableShape> drawableAngleRanges()
		{
			IVector2 start = angleRangeGenerator.getLineSegment().getPathStart();
			IVector2 end = angleRangeGenerator.getLineSegment().getPathEnd();

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
