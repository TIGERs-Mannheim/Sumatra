package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.movingrobot.MovingRobotParams;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Rate defense threats.
 */
public class DefenseThreatRater
{
	@Configurable(comment = "Weight to rate importance of the opponent redirect angle", defValue = "0.9", unit = EConfigUnit.FACTOR)
	private static double weightRedirectAngle = 0.9;
	@Configurable(comment = "Weight to rate importance of the defense travel angle", defValue = "1.0", unit = EConfigUnit.FACTOR)
	private static double weightTravelAngle = 1.0;
	@Configurable(comment = "Weight to rate importance of the opponent centralization", defValue = "1.0", unit = EConfigUnit.FACTOR)
	private static double weightCentralized = 1.0;
	@Configurable(comment = "Weight to rate importance of opponents being at the boarder of our PenArea", defValue = "1.0", unit = EConfigUnit.FACTOR)
	private static double weightAtPenAreaBoarder = 1.0;
	@Configurable(comment = "Weight to rate importance of pass distance", defValue = "0.5", unit = EConfigUnit.FACTOR)
	private static double weightPassDistance = 0.5;
	@Configurable(comment = "Weight to rate future potential", defValue = "1.0", unit = EConfigUnit.FACTOR)
	private static double weightFuturePotential = 1.0;
	@Configurable(comment = "Weight to rate alternative pass receiver", defValue = "2.0", unit = EConfigUnit.FACTOR)
	private static double weightAlternativePassReceiver = 2.0;

	@Configurable(comment = "Control how much of the danger distance is a DropOff", defValue = "0.4", unit = EConfigUnit.PERCENTAGE)
	private static double dangerDropOffPercentageRedirectAngle = 0.4;
	@Configurable(comment = "Control how much of the danger distance is a DropOff", defValue = "0.6", unit = EConfigUnit.PERCENTAGE)
	private static double dangerDropOffPercentageTravelAngle = 0.6;
	@Configurable(comment = "Control how much of the danger distance is a DropOff", defValue = "0.6", unit = EConfigUnit.PERCENTAGE)
	private static double dangerDropOffPercentageCentralized = 0.6;
	@Configurable(comment = "Control how much of the danger distance is a DropOff", defValue = "0.6", unit = EConfigUnit.PERCENTAGE)
	private static double dangerDropOffPercentageAlternativePassReceiver = 0.6;
	@Getter
	@Configurable(comment = "X position on the field where major danger drop off happens.", defValue = "1000.0", unit = EConfigUnit.DISTANCE_MM)
	private static double dangerDropOffX = 1000.0;
	@Configurable(comment = "Maximum angle where a good redirect is expected, larger angles start to get lucky", defValue = "45.0", unit = EConfigUnit.ANGLE_DEG)
	private static double maxGoodRedirectAngle = 45.0;
	@Configurable(comment = "Maximum angle where a any sort of redirect is expected, larger angles are considered impossible", defValue = "75.0", unit = EConfigUnit.ANGLE_DEG)
	private static double maxBadRedirectAngle = 75.0;
	@Configurable(comment = "Redirect precision - If the goal is wider than this, we expect the opponent to hit it", defValue = "0.3", unit = EConfigUnit.ANGLE_RAD)
	private static double redirectPrecision = 0.3;
	@Configurable(comment = "How much of the distance a pass is covered is counted to be used in the pass distance score", defValue = "0.1", unit = EConfigUnit.PERCENTAGE)
	private static double passDistanceWithinPenAreaUsageFactor = 0.1;

	@Configurable(comment = "Optimal pass distance, longer/shorter passes travel too little distance or for too long", defValue = "2000.0", unit = EConfigUnit.DISTANCE_MM)
	private static double optimalPassDistance = 2000.0;
	@Configurable(comment = "Time we look ahead in the future to predict an opponent's potential", defValue = "1.2", unit = EConfigUnit.TIME_S)
	private static double timeToPredictFuture = 1.2;
	@Configurable(comment = "Num Samples per bot to predict future potential", defValue = "25", unit = EConfigUnit.COUNT)
	private static int numSamplesToPredictFuture = 25;
	@Configurable(comment = "Distance between the PenAreaBorder and where the PenAreaBorderScore takes effect", defValue = "400", unit = EConfigUnit.DISTANCE_MM)
	private static double penAreaDropoff = 400.0;

	@Configurable(comment = "Draw debug shapes", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean drawDebugShapes = false;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseThreatRater.class);
	}

	private final Map<BotID, IVector2> mostDangerousPosPerBot = new HashMap<>();
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private Random rnd;
	private List<IDrawableShape> shapes = new ArrayList<>();


	public double getThreatRatingOfRobot(IVector2 ballPos, ITrackedBot threat, List<ITrackedBot> alternativePassReceiver)
	{
		shapes = new ArrayList<>();
		var rating = getDetailedThreatRatingOfRobot(
				ballPos,
				threat,
				alternativePassReceiver.stream().map(ITrackedBot::getBotId).toList()
		);
		shapes.add(rating.draw(threat.getPos(), 1.2 * Geometry.getBotRadius(), false));
		return rating.score;
	}


	public double getThreatRatingOfPosition(IVector2 ballPos, IVector2 threatPos)
	{
		shapes = new ArrayList<>();
		var rating = getDetailedThreatRatingOfPosition(ballPos, threatPos);
		shapes.add(rating.draw(threatPos, 0, true));
		return rating.score;
	}


	public List<IDrawableShape> drawShapes()
	{
		if (drawDebugShapes)
		{
			for (var angle : SumatraMath.evenDistribution1D(-Math.PI / 2, Math.PI / 2, 25))
			{
				var target = Geometry.getGoalOur().getCenter();
				var threat = target.addNew(Vector2.fromAngleLength(angle, 500));
				var score = calcCentralizedScore(threat);
				var color = colorPicker.getColor(score);
				shapes.add(new DrawableLine(threat, target, color));
				shapes.add(new DrawableAnnotation(threat, String.format("%.2f", score)));
			}
		}

		shapes.add(new DrawableShapeBoundary(
				Geometry.getPenaltyAreaOur().withMargin(3 * Geometry.getBotRadius()),
				Color.PINK
		));
		shapes.add(new DrawableShapeBoundary(
				Geometry.getPenaltyAreaOur().withMargin(3 * Geometry.getBotRadius() + penAreaDropoff), Color.PINK));

		shapes.add(new DrawableLine(
				Vector2.fromXY(dangerDropOffX, -Geometry.getFieldWidth() / 2),
				Vector2.fromXY(dangerDropOffX, Geometry.getFieldWidth() / 2)
		));
		Stream.of(
						0.0,
						dangerDropOffPercentageRedirectAngle,
						dangerDropOffPercentageTravelAngle,
						dangerDropOffPercentageCentralized
				)
				.map(this::calcDangerDistanceMax)
				.map(dist -> new DrawableCircle(Circle.createCircle(Geometry.getGoalOur().getCenter(), dist)))
				.forEach(shapes::add);
		return Collections.unmodifiableList(shapes);
	}


	private CombinedRating getDetailedThreatRatingOfRobot(
			IVector2 ballPos, ITrackedBot threat, List<BotID> alternativePassReceiver)
	{
		rnd = new Random(threat.getTimestamp());
		var opponentLookahead = DefenseConstants.getLookaheadBotThreats(threat.getVel().getLength());
		var threatPos = threat.getPosByTime(opponentLookahead);
		return combineRatings(Stream.concat(
				raterWithoutRobotDependence(ballPos, threatPos),
				raterWithRobotDependence(ballPos, threat, opponentLookahead, alternativePassReceiver)
		));
	}


	private CombinedRating getDetailedThreatRatingOfPosition(
			IVector2 ballPos, IVector2 threatPos)
	{
		return combineRatings(raterWithoutRobotDependence(ballPos, threatPos));
	}


	private CombinedRating combineRatings(Stream<Rater> raters)
	{
		var ratings = raters
				.filter(rater -> rater.weight.get() > 0)
				.map(Rater::rate)
				.toList();
		double weightSum = ratings.stream().mapToDouble(Rating::weight).sum();
		double weightedScoreSum = ratings.stream().mapToDouble(s -> s.score * s.weight).sum();
		return new CombinedRating(weightedScoreSum / weightSum, weightSum, ratings);
	}


	private Stream<Rater> raterWithoutRobotDependence(
			IVector2 ballPos, IVector2 threatPos)
	{
		return Stream.of(
				new Rater(
						"TRAVEL_ANGLE",
						() -> calcTravelAngleScore(ballPos, threatPos),
						() -> weightTravelAngle,
						() -> calcDistanceToGoalFactor(threatPos, dangerDropOffPercentageTravelAngle)
				),
				new Rater(
						"REDIRECT_ANGLE",
						() -> calcRedirectAngleScore(ballPos, threatPos),
						() -> weightRedirectAngle,
						() -> calcDistanceToGoalFactor(threatPos, dangerDropOffPercentageRedirectAngle)
				),
				new Rater(
						"CENTRALIZED",
						() -> calcCentralizedScore(threatPos),
						() -> weightCentralized,
						() -> calcDistanceToGoalFactor(threatPos, dangerDropOffPercentageCentralized)
				),
				new Rater(
						"PEN_AREA_BORDER",
						() -> calcAtBoarderScore(threatPos),
						() -> weightAtPenAreaBoarder,
						() -> 1.0
				),
				new Rater(
						"PASS_DISTANCE",
						() -> calcPassDistanceScore(ballPos, threatPos),
						() -> weightPassDistance,
						() -> 1.0
				)
		);
	}


	private Stream<Rater> raterWithRobotDependence(
			IVector2 ballPos, ITrackedBot threat, double opponentLookahead, List<BotID> alternativePassReceiver)
	{
		return Stream.of(
				new Rater(
						"FUTURE_POTENTIAL",
						() -> calcFuturePotentialScore(ballPos, threat, opponentLookahead),
						() -> weightFuturePotential,
						() -> 1.0
				),
				new Rater(
						"ALT_PASS_RECEIVER",
						() -> calcAlternativePassReceiver(threat, alternativePassReceiver),
						() -> alternativePassReceiver.isEmpty() ? 0 : weightAlternativePassReceiver,
						() -> calcDistanceToGoalFactor(threat.getPos(), dangerDropOffPercentageAlternativePassReceiver)
				)
		);
	}


	private double calcDangerDistanceMin()
	{
		return dangerDropOffX - Geometry.getGoalOur().getCenter().x();
	}


	private double calcDangerDistanceMax(double dangerDropOffPercentage)
	{

		var minDangerDistance = calcDangerDistanceMin();
		var minDangerDistanceInFrontOfPenArea = minDangerDistance - Geometry.getPenaltyAreaDepth();
		return Geometry.getPenaltyAreaDepth() + minDangerDistanceInFrontOfPenArea
				- minDangerDistanceInFrontOfPenArea * dangerDropOffPercentage;
	}


	private double calcDistanceToGoalFactor(IVector2 threatPos, double dangerDropOffPercentage)
	{
		var maxDangerDistance = calcDangerDistanceMax(dangerDropOffPercentage);

		if (threatPos.x() > dangerDropOffX)
		{
			// Really far away - Max 10% danger
			return 0.1 * SumatraMath.relative(threatPos.x(), Geometry.getGoalTheir().getCenter().x(), dangerDropOffX);
		}

		var distThreatToGoal = threatPos.distanceTo(Geometry.getGoalOur().getCenter());
		var minDangerDistance = dangerDropOffX - Geometry.getGoalOur().getCenter().x();
		return 0.1 + 0.9 * SumatraMath.relative(distThreatToGoal, minDangerDistance, maxDangerDistance);
	}


	public double scoreRedirectTriangle(IVector2 ballSource, IVector2 redirectPosition, IVector2 target)
	{
		var incomingAngle = Vector2.fromPoints(redirectPosition, ballSource).getAngle();
		var outgoingAngle = Vector2.fromPoints(redirectPosition, target).getAngle();
		return scoreVolleyAngle(AngleMath.diffAbs(incomingAngle, outgoingAngle));
	}


	private double scoreVolleyAngle(double volleyAngle)
	{
		return SumatraMath.relative(
				volleyAngle,
				AngleMath.deg2rad(maxBadRedirectAngle),
				AngleMath.deg2rad(maxGoodRedirectAngle)
		);
	}


	private double calcRedirectAngleScore(IVector2 ballPos, IVector2 threatPos)
	{
		var angleOpponent = Vector2.fromPoints(threatPos, ballPos).getAngle();
		var angleLeft = Vector2.fromPoints(threatPos, Geometry.getGoalOur().getLeftPost()).getAngle();
		var angleRight = Vector2.fromPoints(threatPos, Geometry.getGoalOur().getRightPost()).getAngle();
		var angleGoal = AngleMath.capAngle(angleOpponent, angleLeft, angleRight);

		var goalAngleRange = AngleMath.diffAbs(angleLeft, angleRight);
		var volleyAngle = AngleMath.diffAbs(angleOpponent, angleGoal) + redirectPrecision / 2;

		return SumatraMath.relative(goalAngleRange, 0, redirectPrecision) * scoreVolleyAngle(volleyAngle);
	}


	private double calcTravelAngleScore(IVector2 ballPos, IVector2 threatPos)
	{
		var opponentGoal = Vector2.fromPoints(threatPos, Geometry.getGoalOur().bisection(threatPos));
		var ballGoal = Vector2.fromPoints(ballPos, Geometry.getGoalOur().bisection(ballPos));
		// How far has the keeper to travel to defend the new position
		return ballGoal.angleToAbs(opponentGoal).orElse(0.0) / Math.PI;
	}


	private double calcCentralizedScore(IVector2 threatPos)
	{
		// Positions in the center are more dangerous
		var opponentGoal = Vector2.fromPoints(threatPos, Geometry.getGoalOur().bisection(threatPos));
		var deadZone = SumatraMath.asin((2 * Geometry.getBotRadius()) / Geometry.getGoalOur().getWidth());
		var angle = Math.abs(opponentGoal.getAngle());
		var factor = SumatraMath.cap(angle / (AngleMath.PI_HALF + deadZone) - 1, 0, 1);
		return 1 - Math.pow(1 - factor, 3);
	}


	private double calcAtBoarderScore(IVector2 threatPos)
	{
		var distance = Geometry.getPenaltyAreaOur().withMargin(3 * Geometry.getBotRadius()).distanceTo(threatPos);
		if (distance <= 0)
		{
			return 1;
		} else
		{
			return SumatraMath.cap(1 - (distance / penAreaDropoff), 0, 1);
		}
	}


	public double calcPassDistanceScore(IVector2 ballThreatPos, IVector2 threatPos)
	{
		var passLine = Lines.segmentFromPoints(ballThreatPos, threatPos);
		var distance = passLine.getLength();

		final double score;
		if (distance < optimalPassDistance)
		{
			var distanceLower = 0.5 * optimalPassDistance;
			if (distance < distanceLower)
			{
				score = 0;
			} else
			{
				score = (distance - distanceLower) / distanceLower;
			}
		} else
		{
			var penAreaIntersections = Geometry.getPenaltyAreaOur().intersectPerimeterPath(passLine);

			double distanceWithoutPenArea;
			if (penAreaIntersections.size() == 2)
			{
				var distanceWithinPenArea = penAreaIntersections.getFirst().distanceTo(penAreaIntersections.getLast());
				distanceWithoutPenArea = SumatraMath.max(
						optimalPassDistance,
						distance - (1 - passDistanceWithinPenAreaUsageFactor) * distanceWithinPenArea
				);
			} else
			{
				distanceWithoutPenArea = distance;
			}

			var distanceUpper = 2 * optimalPassDistance;
			if (distanceWithoutPenArea > distanceUpper)
			{
				score = 0;
			} else
			{
				score = 1 - ((distanceWithoutPenArea - optimalPassDistance) / (distanceUpper - optimalPassDistance));

			}
		}
		return score;
	}


	private double calcAlternativePassReceiver(ITrackedBot threat, List<BotID> alternativePassReceiver)
	{
		return alternativePassReceiver.contains(threat.getBotId()) ? 1 : 0;
	}


	private double calcFuturePotentialScore(IVector2 ballPos, ITrackedBot threat, double opponentLookahead)
	{
		var movingRobot = MovingRobotFactory.stoppingRobot(
				MovingRobotParams.builder()
						.position(threat.getPos())
						.velocity(threat.getVel())
						.vLimit(threat.getMoveConstraints().getVelMax())
						.aLimit(threat.getMoveConstraints().getAccMax())
						.brkLimit(threat.getMoveConstraints().getBrkMax())
						.radius(0)
						.reactionTime(opponentLookahead)
						.build()
		);

		var circle = movingRobot.getMovingHorizon(timeToPredictFuture + opponentLookahead);

		List<IVector2> positionsToCheck = new ArrayList<>();
		if (mostDangerousPosPerBot.containsKey(threat.getBotId())
				&& circle.isPointInShape(mostDangerousPosPerBot.get(threat.getBotId())))
		{
			positionsToCheck.add(mostDangerousPosPerBot.get(threat.getBotId()));
		}
		for (int i = 0; i < numSamplesToPredictFuture; ++i)
		{
			double angle = AngleMath.PI_TWO * rnd.nextDouble();
			double radius = circle.radius() * rnd.nextDouble();
			positionsToCheck.add(circle.center().addNew(Vector2.fromAngleLength(angle, radius)));
		}

		var checkedPositions = positionsToCheck.stream()
				.filter(pos -> Geometry.getField().isPointInShape(pos))
				.filter(pos -> !Geometry.getPenaltyAreaOur().isPointInShape(pos))
				.filter(pos -> !Geometry.getPenaltyAreaTheir().isPointInShape(pos))
				.map(pos -> new TimedPos(pos, TrajectoryGenerator.generatePositionTrajectory(threat, pos).getTotalTime()))
				.map(pos -> new TimedAndScoredPos(pos.pos, pos.time, getDetailedThreatRatingOfPosition(ballPos, pos.pos)))
				.toList();

		var mostDangerousPosOpt = checkedPositions.stream()
				.max(Comparator.comparingDouble(pos -> pos.rating.score));
		if (mostDangerousPosOpt.isEmpty())
		{
			return 0;
		}
		var mostDangerousPos = mostDangerousPosOpt.get();
		if (drawDebugShapes)
		{
			shapes.add(new DrawableCircle(circle, Color.BLACK));
			checkedPositions.forEach(
					pos -> shapes.add(new DrawablePoint(pos.pos, colorPicker.getColor(pos.rating.score))));
			shapes.add(new DrawableCircle(Circle.createCircle(mostDangerousPos.pos, 25), Color.CYAN));
			shapes.add(new DrawableLine(mostDangerousPos.pos, threat.getPos(), Color.CYAN));
			shapes.add(mostDangerousPos.rating.draw(mostDangerousPos.pos, 0, false));
		}

		mostDangerousPosPerBot.put(threat.getBotId(), mostDangerousPos.pos);
		return mostDangerousPos.rating.score;
	}


	private record Rating(double score, double weight, double distanceFactor, Rater rater)
	{

	}

	private record CombinedRating(double score, double weight, List<Rating> sources)
	{
		public IDrawableShape draw(IVector2 position, double xOffset, boolean center)
		{
			var text = sources.stream()
					.map(rating -> String.format(
									"%.2f | %.2f | %.2f | - %s%n",
									rating.score * rating.weight / weight,
									rating.score,
									rating.distanceFactor,
									rating.rater.name()
							)
					)
					.collect(Collectors.joining());

			return new DrawableAnnotation(position, text)
					.withOffset(Vector2.fromX(xOffset))
					.withCenterHorizontally(center)
					.withFontHeight(20);
		}
	}

	private record Rater(String name, Supplier<Double> rater, Supplier<Double> weight, Supplier<Double> distanceFactor)
	{
		Rating rate()
		{
			var factor = distanceFactor.get();
			return new Rating(factor * rater.get(), weight.get(), factor, this);
		}
	}

	private record TimedPos(IVector2 pos, double time)
	{
	}

	private record TimedAndScoredPos(IVector2 pos, double time, CombinedRating rating)
	{
	}
}
