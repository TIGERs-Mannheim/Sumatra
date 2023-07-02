/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;

import java.awt.Color;
import java.awt.geom.Arc2D;


/**
 * Calculate a position on normal blocking arc that covers the goal in the best manner from posToCover
 */

public class NormalBlockDestinationCalculator extends AKeeperDestinationCalculator
{
	@Configurable(comment = "Lower value -> rounder shape, higher value -> straighter shape (Values below 0.5 will be clamped)", defValue = "1.0")
	private static double normalBlockShapeFactor = 1.0;

	static
	{
		ConfigRegistration.registerClass("skills", NormalBlockDestinationCalculator.class);
	}


	@Override
	public IVector2 calcDestination()
	{
		var radius = Geometry.getGoalOur().getWidth() * Math.max(normalBlockShapeFactor, 0.5);
		var keeperArc = getNormalBlockKeeperArc(radius);
		var defaultDefPosition = TriangleMath.bisector(getPosToCover(), Geometry.getGoalOur().getLeftPost(),
				Geometry.getGoalOur().getRightPost());
		var polarPos = getPolarPosFromThreatLine(keeperArc, getPosToCover(), defaultDefPosition);
		getShapes().get(ESkillShapesLayer.KEEPER_DEFLECTION_ADAPTION)
				.add(new DrawableCircle(Circle.createCircle(getPosFromPolarPos(keeperArc, polarPos), 50), Color.BLUE));
		var polarPosAdapted = adaptPolarPosForDeflections(keeperArc, getPosToCover(), polarPos);
		return getPosFromPolarPos(keeperArc, polarPosAdapted);
	}


	/**
	 * Creates an Arc, where the keeper is allowed to move on during normal block
	 *
	 * @param circularRadius
	 * @return
	 */
	private IArc getNormalBlockKeeperArc(double circularRadius)
	{
		var leftPost = Geometry.getGoalOur().getLeftPost();
		var rightPost = Geometry.getGoalOur().getRightPost();

		var center = Vector2.fromX(-Math.sqrt(
				circularRadius * circularRadius - 0.25 * Geometry.getGoalOur().getWidth() * Geometry.getGoalOur()
						.getWidth()) + Geometry.getGoalOur().getCenter().x());

		var leftAngle = leftPost.subtractNew(center).getAngle();
		var rightAngle = rightPost.subtractNew(center).getAngle();
		var rotation = AngleMath.diffAbs(leftAngle, rightAngle);
		var keeperArc = Arc.createArc(Vector2.fromX(center.x() + Geometry.getBotRadius()), circularRadius, rightAngle,
				rotation);
		getShapes().get(ESkillShapesLayer.KEEPER).add(new DrawableArc(keeperArc, Color.WHITE).setArcType(Arc2D.OPEN));

		return keeperArc;
	}


	/**
	 * For defending goals in the short corner, a much greater deflection angle is necessary than in the long corner
	 * This method will adapt the keeper position for this effect
	 *
	 * @param keeperArc
	 * @param posToCover
	 * @param polarPos
	 * @return
	 */
	private double adaptPolarPosForDeflections(IArc keeperArc, IVector2 posToCover, double polarPos)
	{
		var shapes = getShapes().get(ESkillShapesLayer.KEEPER_DEFLECTION_ADAPTION);
		var unadaptedPos = getPosFromPolarPos(keeperArc, polarPos);

		var leftPostBisector = TriangleMath.bisector(unadaptedPos, Geometry.getGoalOur().getLeftPost(), posToCover);
		var rightPosBisector = TriangleMath.bisector(unadaptedPos, posToCover, Geometry.getGoalOur().getRightPost());

		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, rightPosBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, leftPostBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				unadaptedPos, Vector2.fromPoints(unadaptedPos, posToCover).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getLeftPost()).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getRightPost()).scaleToNew(250)),
				Color.BLUE).setStrokeWidth(5));

		var keeperLockingDirection = Vector2.fromPoints(unadaptedPos, posToCover).getAngle();
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

		return getPolarPosFromThreatLine(keeperArc, posToCover, unadaptedPos.addNew(offset));
	}


	/**
	 * Project the threatLine from posToCover to wantedDefensivePos onto the keeperArc
	 * It also takes care of limiting the movement of the keeper to the keeperArc
	 *
	 * @param keeperArc
	 * @param posToCover
	 * @param wantedDefensivePos
	 * @return
	 */
	private double getPolarPosFromThreatLine(IArc keeperArc, IVector2 posToCover, IVector2 wantedDefensivePos)
	{
		var polarPosAtGoalCorner = (posToCover.y() > 0) ? -keeperArc.getStartAngle() : keeperArc.getStartAngle();
		if (wantedDefensivePos.x() > posToCover.x())
		{
			return polarPosAtGoalCorner;
		}

		var threatLine = Lines.halfLineFromPoints(posToCover, wantedDefensivePos);
		var intersections = keeperArc.intersectPerimeterPath(threatLine);
		return posToCover.nearestToOpt(intersections)
				.map(posOnKeeperArc -> posOnKeeperArc.subtractNew(keeperArc.center()).getAngle())
				.orElse(polarPosAtGoalCorner);
	}


	private IVector2 getPosFromPolarPos(IArc keeperArc, double polarPos)
	{
		return keeperArc.center().addNew(Vector2.fromAngleLength(polarPos, keeperArc.radius()));
	}


}
