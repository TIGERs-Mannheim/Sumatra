/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Provides some generic methods to the defense.
 */
public final class DefenseMath
{
	private DefenseMath()
	{
		// Hide public constructor
	}


	/**
	 * Calculates a point to protect the goal from a threat.
	 * Field boundaries are not checked!
	 *
	 * @param threatPos The threat to protect the defBoundary from
	 * @param halfWidth distance that covers half the area
	 * @return
	 */
	public static IVector2 calculateGoalDefPoint(
			IVector2 threatPos,
			double halfWidth
	)
	{
		IVector2 goalBisector = Geometry.getGoalOur().bisection(threatPos);
		return LineMath.stepAlongLine(threatPos, goalBisector, calculateGoalDefDistance(threatPos, halfWidth));
	}


	/**
	 * Calculates a the distance from the threat where to protect a line (like the goal) from the threat.
	 * Field boundaries are not checked!
	 *
	 * @param threatPos The threat to protect the defBoundary from
	 * @param halfWidth distance that covers half the area
	 * @return
	 */
	public static double calculateGoalDefDistance(
			IVector2 threatPos,
			double halfWidth
	)
	{
		IVector2 goalBisector = Geometry.getGoalOur().bisection(threatPos);
		double angleBallLeftGoal = Geometry.getGoalOur().getLeftPost().subtractNew(threatPos).angleToAbs(
				goalBisector.subtractNew(threatPos)).orElse(0.0);

		double distBall2DefPoint = angleBallLeftGoal > 0 ? halfWidth / SumatraMath.tan(angleBallLeftGoal) : 0.0;
		return Math.min(distBall2DefPoint, threatPos.distanceTo(goalBisector));
	}


	/**
	 * Get a line segment on which a threat can be protected by a defender
	 *
	 * @param threatLine       the threat line to protect
	 * @param marginToThreat   distance to keep from bot center to threat pos
	 * @param marginToPenArea  distance to keep to penArea
	 * @param maxGoOutDistance max distance to go out from goal center
	 * @return the line segment
	 */
	public static ILineSegment getProtectionLine(
			final ILineSegment threatLine,
			final double marginToThreat,
			final double marginToPenArea,
			final double maxGoOutDistance
	)
	{
		IVector2 goal = threatLine.getPathEnd();
		IVector2 threat = Geometry.getField().nearestPointInside(threatLine.getPathStart());
		IVector2 threatWithMargin = LineMath.stepAlongLine(threat, goal, marginToThreat);

		IPenaltyArea penArea = Geometry.getPenaltyAreaOur()
				.withMargin(marginToPenArea)
				.withRoundedCorners(marginToPenArea);

		var threatLineOnPenArea = goal.nearestToOpt(
				penArea.intersectPerimeterPath(Lines.halfLineFromPoints(goal, threat))
		).orElseGet(() -> penArea.projectPointOnToPenaltyAreaBorder(threat));

		double distThreat2Goal = threatWithMargin.distanceTo(goal);
		double distPenArea2Goal = threatLineOnPenArea.distanceTo(goal);

		IVector2 start;
		IVector2 end;

		if (distPenArea2Goal > maxGoOutDistance || distThreat2Goal < distPenArea2Goal)
		{
			start = threatLineOnPenArea;
			end = threatLineOnPenArea;
		} else if (distThreat2Goal > maxGoOutDistance)
		{
			start = LineMath.stepAlongLine(goal, threatWithMargin, maxGoOutDistance);
			end = threatLineOnPenArea;
		} else
		{
			start = threatWithMargin;
			end = threatLineOnPenArea;
		}
		return Lines.segmentFromPoints(start, end);
	}
}
