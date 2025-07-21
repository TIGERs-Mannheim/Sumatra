/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


/**
 * Calculate a position on normal blocking arc that covers the goal in the best manner from posToCover
 */

public class NormalBlockDestinationCalculator extends AKeeperDestinationCalculator
{

	@Override
	public KeeperDestination calcDestination()
	{
		IVector2 goalPost;
		IVector2 usedCornerPoint;
		if (getPosToCover().y() > 0)
		{
			usedCornerPoint = Geometry.getField().getCorner(IRectangle.ECorner.TOP_LEFT);
			goalPost = Geometry.getGoalOur().getLeftPost();
		} else
		{
			usedCornerPoint = Geometry.getField().getCorner(IRectangle.ECorner.BOTTOM_LEFT);
			goalPost = Geometry.getGoalOur().getRightPost();
		}
		var unUsedCornerPoint = Vector2.fromXY(usedCornerPoint.x(), -usedCornerPoint.y());

		createDestinationsIfBallWouldRollTowardsCorner(unUsedCornerPoint, Color.GRAY);
		var destinationsTowardsCorner = createDestinationsIfBallWouldRollTowardsCorner(usedCornerPoint, Color.WHITE);

		var distToGoalTowardsCorner = destinationsTowardsCorner.stream()
				.mapToDouble(pos -> Geometry.getGoalOur().getLine().distanceTo(pos))
				.max().orElse(0);

		var stoppedDestination = projectOntoBisector(getPosToCover());
		var distToGoal = Geometry.getGoalOur().getLine().distanceTo(stoppedDestination);

		var usedDistance = 0.33 * distToGoal + 0.67 * distToGoalTowardsCorner;

		var lineToIntersect = Lines.lineFromDirection(goalPost.addNew(Vector2.fromX(usedDistance)), Vector2.fromY(1));
		var posOnGoal = Geometry.getGoalOur().bisection(getPosToCover());
		var bisector = Lines.lineFromPoints(getPosToCover(), posOnGoal);

		var defensePos = bisector.intersect(lineToIntersect).asOptional().orElse(stoppedDestination);

		defensePos = adaptPosForDeflections(defensePos);
		defensePos = Vector2.fromXY(
				Math.max(defensePos.x(), Geometry.getGoalOur().getCenter().x() + Geometry.getBotRadius()),
				defensePos.y()
		);
		return KeeperDestination.fromDestination(getTBot(), defensePos);
	}


	private List<IVector2> createDestinationsIfBallWouldRollTowardsCorner(IVector2 pointOnSide, Color color)
	{
		var lineTowardsSide = Lines.segmentFromPoints(getPosToCover(), pointOnSide);
		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS).add(new DrawableLine(lineTowardsSide, color));
		var length = lineTowardsSide.getLength();
		var n = (int) Math.ceil(length / Geometry.getBotRadius());
		var destinationsTowardsCorner = SumatraMath.evenDistribution1D(0, lineTowardsSide.getLength(), n).stream()
				.map(lambda -> lineTowardsSide.getPathStart()
						.addNew(lineTowardsSide.directionVector().scaleToNew(lambda)))
				.map(this::projectOntoBisector)
				.toList();

		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS).addAll(
				destinationsTowardsCorner.stream().map(p -> new DrawablePoint(p, color)).toList()
		);
		return destinationsTowardsCorner;
	}


	private IVector2 projectOntoBisector(IVector2 posToDefend)
	{
		var bisector = Lines.lineFromPoints(posToDefend, Geometry.getGoalOur().bisection(posToDefend));
		return Stream.of(
						Geometry.getGoalOur().getLeftPost(),
						Geometry.getGoalOur().getRightPost()
				)
				.map(bisector::closestPointOnPath)
				.min(Comparator.comparingDouble(posToDefend::distanceToSqr))
				.orElse(Vector2.zero());
	}


	/**
	 * For defending goals in the short corner, a much greater deflection angle is necessary than in the long corner
	 * This method will adapt the keeper position for this effect
	 */
	private IVector2 adaptPosForDeflections(IVector2 unadaptedPos)
	{
		var shapes = getShapes().get(ESkillShapesLayer.KEEPER_DEFLECTION_ADAPTION);

		var leftPostBisector = TriangleMath.bisector(unadaptedPos, Geometry.getGoalOur().getLeftPost(), getPosToCover());
		var rightPosBisector = TriangleMath.bisector(unadaptedPos, getPosToCover(), Geometry.getGoalOur().getRightPost());

		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, rightPosBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, leftPostBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, getPosToCover()).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getLeftPost()).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getRightPost()).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));

		var keeperLockingDirection = Vector2.fromPoints(unadaptedPos, getPosToCover()).getAngle();
		var leftBlockingRatio = Vector2.fromPoints(unadaptedPos, leftPostBisector).getAngle();
		var rightBlockingRatio = Vector2.fromPoints(unadaptedPos, rightPosBisector).getAngle();

		var leftBlockingDist =
				AngleMath.sin(AngleMath.diffAbs(keeperLockingDirection, leftBlockingRatio)) * Geometry.getBotRadius();
		var rightBlockingDist =
				AngleMath.sin(AngleMath.diffAbs(keeperLockingDirection, rightBlockingRatio)) * Geometry.getBotRadius();

		var leftToRightVector = Vector2.fromAngle(
				AngleMath.rotateAngle(keeperLockingDirection, AngleMath.PI_HALF, ERotationDirection.CLOCKWISE));


		var offset = leftToRightVector.scaleToNew(leftBlockingDist - rightBlockingDist);
		shapes.add(new DrawableLine(Lines.segmentFromOffset(unadaptedPos, offset), Color.PINK));

		return unadaptedPos.addNew(offset);
	}

}
