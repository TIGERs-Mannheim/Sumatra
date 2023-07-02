/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Find the best direct shot by searching for the biggest free angle on the goal considering the velocity
 * of the opposing defenders.
 */
public class BestDirectShotBallPossessingBot
{

	@Configurable(comment = "Angle which indicates a shot in this angle is a goal in almost every case", defValue = "0.3")
	private static double probablyAGoalAngle = 0.3;

	@Configurable(comment = "opponent shot speed", defValue = "8.0")
	private static double shotVelocityOpponent = 8.0;

	@Configurable(comment = "Factor used to describe how often the opponent reaches its maximum velocity and acceleration", defValue = "0.8")
	private static double opponentAccuracy = 0.8;

	@Configurable(comment = "Delay which a opponent has to recognise and react on an incoming shot", defValue = "0.3")
	private static double opponentReactionTime = 0.3;


	static
	{
		ConfigRegistration.registerClass("metis", BestDirectShotBallPossessingBot.class);
	}


	private BestDirectShotBallPossessingBot()
	{
	}


	/**
	 * @param goal
	 * @param shotPos
	 * @param obstacleBots
	 * @return
	 */
	public static Optional<ValuePoint> getBestShot(Goal goal, IVector2 shotPos, List<ITrackedBot> obstacleBots)
	{
		return getBestShot(goal, shotPos, obstacleBots, new ArrayList<>(), 20, 0.0);
	}


	/**
	 * @param goal
	 * @param shotPos
	 * @param obstacleBots
	 * @param shapes
	 * @param nIterations
	 * @return best direct shot target
	 */
	private static Optional<ValuePoint> getBestShot(Goal goal, IVector2 shotPos, List<ITrackedBot> obstacleBots,
			List<IDrawableShape> shapes, int nIterations, double timeUntilShot)
	{
		double startAngle = Vector2.fromPoints(shotPos, goal.getRightPost()).getAngle();
		double endAngle = Vector2.fromPoints(shotPos, goal.getLeftPost()).getAngle();

		shapes.clear();

		List<CoveredAngle> coveredAngles = getCoveredAngles(shotPos, obstacleBots, nIterations,
				goal, shapes, timeUntilShot);

		if (startAngle < endAngle)
		{
			coveredAngles.add(new CoveredAngle(startAngle, false));
			coveredAngles.add(new CoveredAngle(endAngle, true));
		} else
		{
			coveredAngles.add(new CoveredAngle(endAngle, false));
			coveredAngles.add(new CoveredAngle(startAngle, true));
		}

		List<FreeArea> freeAreas = getFreeAreas(coveredAngles);

		freeAreas.sort(FreeArea.compareByAngle);

		Optional<ValuePoint> bestTarget = getBestTarget(goal, shotPos, freeAreas);

		if (bestTarget.isEmpty() && nIterations > 1)
		{
			bestTarget = getBestShot(goal, shotPos, obstacleBots, shapes, 1, timeUntilShot);
			bestTarget.ifPresent(vp -> vp.setValue(0.0));
		}

		if (nIterations > 1)
		{
			drawFreeShotAreas(goal, shotPos, freeAreas, shapes);
			bestTarget.ifPresent(valuePoint -> drawBestTargetLine(shotPos, valuePoint, shapes));
		}

		return bestTarget;
	}


	private static List<CoveredAngle> getCoveredAngles(final IVector2 shotPos, final List<ITrackedBot> obstacleBots,
			final int nIterations, Goal goal, final List<IDrawableShape> shapes, final double timeUntilShot)
	{
		List<CoveredAngle> coveredAngles = new ArrayList<>();

		IVector2 shotPos2postRight = Vector2.fromPoints(shotPos, goal.getRightPost());
		IVector2 shotPos2postLeft = Vector2.fromPoints(shotPos, goal.getLeftPost());

		double startAngle = shotPos2postRight.getAngle();
		double endAngle = shotPos2postLeft.getAngle();

		double angleWidth = endAngle - startAngle;

		double longerVectorLength = Math.max(shotPos2postLeft.getLength(), shotPos2postRight.getLength());

		double stepSize = longerVectorLength / nIterations;
		double timePerStep = (stepSize / 1000) / shotVelocityOpponent;

		IVector2 stepLeft = shotPos2postLeft.scaleToNew(stepSize);
		double lengthStep = stepLeft.getLength();

		for (int i = 0; i < nIterations; ++i)
		{
			final IArc curArc = Arc.createArc(shotPos, (i + 1) * lengthStep, startAngle, angleWidth);
			double timePassed = i * timePerStep + timeUntilShot;

			List<VaguePosition> vagueBotPositions = new ArrayList<>();
			for (ITrackedBot bot : obstacleBots)
			{
				double radius = Geometry.getBotRadius()
						+ opponentAccuracy * 1000 * Math.max(timePassed - opponentReactionTime, 0)
								* Math.min(bot.getMoveConstraints().getAccMax() * (timePassed - opponentReactionTime),
										bot.getMoveConstraints().getVelMax());

				VaguePosition vaguePosition = new VaguePosition(bot.getPosByTime(timePassed), radius);

				if (vaguePosition.getCenter().x() > shotPos.x() + 2 * Geometry.getBallRadius()
						&& (curArc.withMargin(vaguePosition.getRadius()).isPointInShape(vaguePosition.getCenter())
						|| Lines.segmentFromPoints(curArc.center(),
								curArc.center()
										.addNew(Vector2.fromAngle(curArc.getStartAngle()).scaleTo(curArc.radius())))
						.distanceTo(vaguePosition.getCenter()) <= vaguePosition.getRadius()
						|| Lines.segmentFromPoints(curArc.center(),
								curArc.center().addNew(Vector2.fromAngle(curArc.getStartAngle() + curArc.getRotation())
										.scaleTo(curArc.radius())))
						.distanceTo(vaguePosition.getCenter()) <= vaguePosition.getRadius()))
				{
					vagueBotPositions.add(vaguePosition);
				}
			}

			vagueBotPositions.forEach(vp -> shapes.add(new DrawableCircle(vp.getCenter(), vp.getRadius(), Color.GRAY)));

			coveredAngles.addAll(getCoveredAngles(vagueBotPositions, shotPos));
		}

		return coveredAngles;
	}


	private static void drawBestTargetLine(final IVector2 shotPos, final ValuePoint bestTarget,
			final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableLine(Lines.segmentFromPoints(shotPos, bestTarget)));
	}


	private static Optional<ValuePoint> getBestTarget(final Goal goal, final IVector2 shotPos,
			final List<FreeArea> freeAreas)
	{
		Optional<ValuePoint> bestTarget = Optional.empty();

		if (!freeAreas.isEmpty())
		{
			FreeArea area = freeAreas.get(0);

			ILineSegment goalLine = goal.getLineSegment();
			IHalfLine shotLine = Lines.halfLineFromDirection(shotPos, Vector2.fromAngle(area.getBisectionAngle()));

			var pointInGoal = shotLine.intersect(goalLine).asOptional();

			if (pointInGoal.isPresent())
			{
				ValuePoint bestShootPoint = new ValuePoint(pointInGoal.get(), area.getScoreChance());
				bestTarget = Optional.of(bestShootPoint);
			}
		}

		return bestTarget;
	}


	private static void drawFreeShotAreas(final Goal goal, final IVector2 shotPos,
			final List<FreeArea> freeAreas, final List<IDrawableShape> shapes)
	{
		for (FreeArea freeArea : freeAreas)
		{
			// line to create intersections with vectors from angles
			ILine goalLine = Lines.lineFromPoints(goal.getLeftPost(), goal.getRightPost());

			double scoreChance = freeArea.getScoreChance();

			Color color = new Color((int) ((1 - scoreChance) * 255), 0, (int) (scoreChance * 255), 100);

			IVector2 interceptionWithGoalLeft = goalLine.intersect(
							Lines.lineFromDirection(shotPos, Vector2.fromAngle(freeArea.getStartAngle().getAngle())))
					.asOptional().orElse(Vector2f.ZERO_VECTOR);
			IVector2 interceptionWithGoalRight = goalLine.intersect(
							Lines.lineFromDirection(shotPos, Vector2.fromAngle(freeArea.getEndAngle().getAngle())))
					.asOptional().orElse(Vector2f.ZERO_VECTOR);

			DrawableTriangle triangle = new DrawableTriangle(Triangle
					.fromCorners(shotPos, interceptionWithGoalLeft, interceptionWithGoalRight), color);
			triangle.setFill(true);

			shapes.add(triangle);
		}
	}


	private static List<FreeArea> getFreeAreas(List<CoveredAngle> coveredAngles)
	{
		List<FreeArea> newFreeAreas = new ArrayList<>();

		coveredAngles.sort(Comparator.comparingDouble(CoveredAngle::getAngle));

		CoveredAngle prevEndAngle = null;

		int inNShadows = 1;

		for (final CoveredAngle curAngle : coveredAngles)
		{
			if (curAngle.isStartAngleOfCoveredArea())
			{
				inNShadows += 1;

				if (inNShadows == 1)
				{
					assert prevEndAngle != null;
					newFreeAreas.add(new FreeArea(prevEndAngle.getAngle(), curAngle.getAngle()));
				}

			} else if (curAngle.isShadowEnd())
			{
				inNShadows -= 1;

				if (inNShadows == 0)
				{
					prevEndAngle = curAngle;
				}
			}
		}

		return newFreeAreas;
	}


	private static List<CoveredAngle> getCoveredAngles(final List<VaguePosition> filteredBotPos, final IVector2 shotPos)
	{
		List<CoveredAngle> coveredAngles = new ArrayList<>();

		for (VaguePosition botPos : filteredBotPos)
		{
			ICircle botShape = Circle.createCircle(botPos.getCenter(), botPos.getRadius());

			List<IVector2> tangentsBot = CircleMath.tangentialIntersections(botShape, shotPos);

			tangentsBot.sort(Comparator.comparingDouble(IVector2::getAngle));

			double angleTangentPointA = Vector2.fromPoints(shotPos, tangentsBot.get(0)).getAngle();
			double angleTangentPointB = Vector2.fromPoints(shotPos, tangentsBot.get(1)).getAngle();

			coveredAngles.add(new CoveredAngle(angleTangentPointA,
					angleTangentPointA < angleTangentPointB));
			coveredAngles.add(new CoveredAngle(angleTangentPointB,
					angleTangentPointA >= angleTangentPointB));

		}

		return coveredAngles;
	}


	private static class VaguePosition
	{
		private IVector2 center;
		private double radius;


		VaguePosition(IVector2 center, double radius)
		{
			this.center = center;
			this.radius = radius;
		}


		public IVector2 getCenter()
		{
			return center;
		}


		public double getRadius()
		{
			return radius;
		}
	}


	private static class FreeArea
	{
		private double startAngle;
		private double endAngle;

		/** */
		public static final Comparator<FreeArea> compareByAngle = new CompareFreeAngles().reversed();


		public FreeArea(double startAngle, double endAngle)
		{
			this.startAngle = startAngle;
			this.endAngle = endAngle;
		}


		public CoveredAngle getStartAngle()
		{
			return new CoveredAngle(startAngle, false);
		}


		public CoveredAngle getEndAngle()
		{
			return new CoveredAngle(endAngle, true);
		}


		public double getAngleDifference()
		{

			return Math.abs(AngleMath.difference(startAngle, endAngle));
		}


		public double getBisectionAngle()
		{
			return (startAngle + endAngle) / 2.0;
		}


		public double getScoreChance()
		{
			return getScoreChanceFromAngle(getAngleDifference());
		}


		private static double getScoreChanceFromAngle(double angle)
		{
			return Math.min(angle / probablyAGoalAngle, 1);
		}


		private static class CompareFreeAngles implements Comparator<FreeArea>
		{

			@Override
			public int compare(final FreeArea o1, final FreeArea o2)
			{
				if (o1.getScoreChance() > o2.getScoreChance())
				{
					return 1;
				} else if (o1.getScoreChance() < o2.getScoreChance())
				{
					return -1;
				} else
				{
					if (o1.getAngleDifference() > o2.getAngleDifference())
					{
						return 1;
					} else if (o1.getAngleDifference() < o2.getAngleDifference())
					{
						return -1;
					}
				}

				return 0;
			}
		}
	}


	private static class CoveredAngle
	{
		private double angle;
		private boolean isStartAngleOfCoveredArea;


		public CoveredAngle(double angle, boolean isStartAngleOfCoveredArea)
		{
			this.angle = angle;
			this.isStartAngleOfCoveredArea = isStartAngleOfCoveredArea;
		}


		public double getAngle()
		{
			return angle;
		}


		public boolean isStartAngleOfCoveredArea()
		{
			return isStartAngleOfCoveredArea;
		}


		public boolean isShadowEnd()
		{
			return !isStartAngleOfCoveredArea;
		}
	}
}
