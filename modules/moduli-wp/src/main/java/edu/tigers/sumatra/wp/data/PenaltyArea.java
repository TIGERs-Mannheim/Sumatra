/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.shapes.I2DShape;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;


/**
 * Class representing a penalty area
 * The PenaltyArea is built out of one rectangle in the middle, with two quarter circles on the top and the
 * bottom half
 * A little sketch with the given points
 * 
 * <pre>
 *         circlePointUpperNeg   penalty mark   circlePointUpperPos
 *                    v             v             v
 * |------------------+-------------+-------------+------------------|
 * |outer Rectangle ..|[-------front Line--------]|..                |
 * |           ....:  |                           |  :....           |
 * |         .:       |                           |       :.         |
 * |       .:         |                           |         :.       |
 * |      :           |                           |           :      |
 * |     :            |   centre Rectangle        |            :     |
 * |    :             |                           |             :    |
 * |   :              |                           |              :   |
 * |  :   neg-circle  |                           | pos-circle    :  |
 * | :                |                           |                : |
 * |:                 |                           |                 :|
 * +------------------+-------------+-------------+------------------+
 * ^                  ^             ^             ^                  ^
 * |           circleCentreNeg  goal centre  circleCentrePos    circlePointLowerPos
 * circlePointLowerNeg
 * </pre>
 *
 * @author Malte, Frieder, KaiE
 */
public class PenaltyArea implements I2DShape
{
	private static final Logger	log			= Logger.getLogger(PenaltyArea.class.getName());
	
	/** as check when using the margin this value is always added to make the border a part of the area */
	private static final double	DBL_EPSILON	= 1e-7;
	
	
	private final ETeam				owner;
	
	
	private final IVector2			goalCentre;
	private final IVector2			penaltyMark;
	private final IVector2			circleCentreNeg;
	private final IVector2			circleCentrePos;
	private final IVector2			circlePointLowerNeg;
	private final IVector2			circlePointLowerPos;
	private final IVector2			circlePointUpperNeg;
	private final IVector2			circlePointUpperPos;
	private final Rectangle			outerRectangle;
	private final float				sign;
	
	private final Rectangle			centreRect;
	private final Line				frontLine;
	
	
	/**
	 * @param owner
	 */
	public PenaltyArea(final ETeam owner)
	{
		ETeam.assertOneTeam(owner);
		this.owner = owner;
		final double offsetFromCentre = 0.5 * Geometry.getLengthOfPenaltyAreaFrontLine();
		
		sign = (owner == ETeam.TIGERS) ? 1 : -1;
		goalCentre = new Vector2f(-sign * 0.5 * Geometry.getFieldLength(), 0);
		penaltyMark = new Vector2f(goalCentre.x() + (sign * Geometry.getDistanceToPenaltyMark()), goalCentre.y());
		
		circleCentrePos = new Vector2f(goalCentre.x(), goalCentre.y() + offsetFromCentre);
		circleCentreNeg = new Vector2f(goalCentre.x(), goalCentre.y() - offsetFromCentre);
		circlePointLowerNeg = new Vector2f(circleCentreNeg.x(), circleCentreNeg.y() - getRadiusOfPenaltyArea());
		circlePointLowerPos = new Vector2f(circleCentrePos.x(), circleCentrePos.y() + getRadiusOfPenaltyArea());
		circlePointUpperNeg = new Vector2f(circleCentreNeg.x() + (sign * getRadiusOfPenaltyArea()), circleCentreNeg.y());
		circlePointUpperPos = new Vector2f(circleCentrePos.x() + (sign * getRadiusOfPenaltyArea()), circleCentrePos.y());
		
		outerRectangle = new Rectangle(circlePointLowerNeg,
				new Vector2f(circlePointUpperPos.x(), circlePointLowerPos.y()));
		
		centreRect = new Rectangle(circlePointUpperNeg, circleCentrePos);
		frontLine = new Line(circlePointUpperPos, circlePointUpperNeg.subtractNew(circlePointUpperPos));
	}
	
	
	@Override
	public double getArea()
	{
		return centreRect.getArea() + (getRadiusOfPenaltyArea() * getRadiusOfPenaltyArea() * AngleMath.PI_HALF);
	}
	
	
	/**
	 * Circumference of front curve of penalty area
	 * TODO: can be static
	 * 
	 * @return
	 */
	public final double getPerimeterFrontCurve()
	{
		return (getRadiusOfPenaltyArea() * AngleMath.PI) + Geometry.getLengthOfPenaltyAreaFrontLine();
	}
	
	
	private final IVector2 getPointOnCircle(final IVector2 origin, final double radius, final double angle)
	{
		return new Vector2f(origin.x() - (sign * radius * Math.cos(angle)),
				origin.y() + (radius * Math.sin(angle)));
	}
	
	
	/**
	 * @param length in [0,circumference]
	 * @return
	 */
	public IVector2 stepAlongPenArea(final double length)
	{
		final double quaterCircLength = AngleMath.PI_HALF * getRadiusOfPenaltyArea();
		final double frontlineLength = Geometry.getLengthOfPenaltyAreaFrontLine();
		if ((0 <= length) && (length <= quaterCircLength))
		{
			return getPointOnCircle(circleCentrePos, getRadiusOfPenaltyArea(),
					(length / getRadiusOfPenaltyArea()) + (AngleMath.PI_HALF));
		} else if (length < (quaterCircLength + frontlineLength))
		{
			return GeoMath.stepAlongLine(circlePointUpperPos, circlePointUpperNeg, length - quaterCircLength);
		} else if (length < ((quaterCircLength * 2) + frontlineLength))
		{
			return getPointOnCircle(circleCentreNeg, getRadiusOfPenaltyArea(),
					((length - quaterCircLength - frontlineLength) / getRadiusOfPenaltyArea()) + (AngleMath.PI));
		} else
		{
			log.warn("Tried to step too long along penalty area: " + length);
			return circlePointLowerNeg;
		}
		
	}
	
	
	/**
	 * Checks if point in penalty with margin
	 * 
	 * @param point
	 * @param margin
	 * @return
	 */
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		final double correctedMargin = margin + DBL_EPSILON;
		if (outerRectangle.isPointInShape(point, Math.max(0, correctedMargin)))
		{
			final Rectangle marginCentreRect = new Rectangle(
					new Vector2f(circlePointUpperNeg.x() + (sign * correctedMargin), circlePointUpperNeg.y()),
					circleCentrePos);
			
			if (marginCentreRect.isPointInShape(point))
			{
				return true;
			}
			if (GeoMath.distancePPSqr(point, circleCentreNeg) <= Math.pow((getRadiusOfPenaltyArea() + correctedMargin), 2))
			{
				return true;
			}
			if (GeoMath.distancePPSqr(point, circleCentrePos) <= Math.pow((getRadiusOfPenaltyArea() + correctedMargin), 2))
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return nearestPointOutside(point, 0);
	}
	
	
	/**
	 * method like {@link PenaltyArea#nearestPointOutside(IVector2)} but with a margin
	 * 
	 * @param point
	 * @param margin
	 * @return
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final double margin)
	{
		if (!isPointInShape(point, margin))
		{
			return point;
		}
		final double correctedMargin = margin + DBL_EPSILON;
		Rectangle marginRect = new Rectangle(
				new Vector2f(circlePointUpperNeg.x() + (sign * correctedMargin), circlePointUpperNeg.y()), circleCentrePos);
		if (marginRect.isPointInShape(point))
		{
			return new Vector2f(frontLine.supportVector().x() + (sign * margin), point.y());
		}
		final double plen = GeoMath.distancePPSqr(point, circleCentrePos);
		final double nlen = GeoMath.distancePPSqr(point, circleCentreNeg);
		
		final IVector2 circCentre = plen < nlen ? circleCentrePos : circleCentreNeg;
		final double scale = Math.min(plen, nlen);
		if (Math.abs(scale) > DBL_EPSILON)
		{
			return circCentre
					.addNew(point.subtractNew(circCentre)
							.multiply((correctedMargin + getRadiusOfPenaltyArea()) / Math.sqrt(scale)));
		}
		return new Vector2f((plen < nlen ? circlePointLowerPos : circlePointLowerNeg));
	}
	
	
	private IVector2 nearestPointFallbackByBisection(final IVector2 point, final IVector2 pointToBuildLine,
			final double correctedMargin)
	{
		
		IVector2 start = new Vector2f(point);
		IVector2 end = new Vector2f(pointToBuildLine);
		double factor = 0.5;
		boolean skip = false;
		for (int i = 0; i < 1000; ++i)
		{
			IVector2 np = new Vector2f(start.x() + (factor * (end.x() - start.x())),
					start.y() + (factor * (end.y() - start.y())));
			if (!skip)
			{
				if (isPointInShape(end, correctedMargin))
				{
					factor = 2;
					end = np;
					continue;
				}
			}
			factor = 0.5;
			skip = true;
			if (isPointInShape(np, correctedMargin))
			{
				start = np;
			} else
			{
				end = np;
			}
			if (start.equals(end, 0.01))
			{
				break;
			}
		}
		return start;
		
	}
	
	
	/**
	 * Creates nearest Point outside of shape that is the closest to the current point
	 * Three possibilities:
	 * 
	 * <pre>
	 * 1) point out of field and not in penalty -> Point is moved into field (not quite part of penalty area but useful)
	 * 2) point and pointToBuildLine is the same and within the area behaves like
	 * {@link PenaltyArea#nearestPointOutside(IVector2,double)}
	 * 3) point and pointToBuildLine create a line that uses {@link PenaltyArea#lineIntersections(ILine)} to get all
	 * intersection points these points are reduced to the one with the shortest distance to point
	 * </pre>
	 * 
	 * @param point
	 * @param pointToBuildLine
	 * @param margin
	 * @return
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine, final double margin)
	{
		final double correctedMargin = margin + DBL_EPSILON;
		
		/** 1) */
		if (!isPointInShape(point, margin))
		{
			if (!Geometry.getField().isPointInShape(point, correctedMargin))
			{
				final double newX = Math.signum(point.x()) * Math.min(Math.abs(point.x()), 0.5 * Geometry.getFieldLength());
				final double newY = Math.signum(point.y()) * Math.min(Math.abs(point.y()), 0.5 * Geometry.getFieldWidth());
				return new Vector2f(newX, newY);
			}
			return point;
		}
		
		/** 2) */
		if (point.equals(pointToBuildLine, DBL_EPSILON))
		{
			return nearestPointOutside(point, margin);
		}
		
		/** 3) */
		
		ILine p2pline = new Line(point, pointToBuildLine.subtractNew(point));
		
		List<IVector2> intersections = calcLineIntersections(p2pline, correctedMargin);
		if (intersections.isEmpty())
		{
			log.warn("unexpected error with parameter: (" + point + ":" + pointToBuildLine + ":" + margin
					+ ") -> using fallback");
			return nearestPointFallbackByBisection(point, pointToBuildLine, correctedMargin);
		}
		return GeoMath.nearestPointInList(intersections, point);
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		return isLineIntersectingShape(line, 0);
	}
	
	
	/**
	 * Getter of the Intersection of a Line with a Circle through the lead point and pythagoras
	 * 
	 * <pre>
	 *        c1   lead    c2
	 * --------+-----+-----+-> line
	 *          \    |    /
	 *           \   |d  /
	 *     radius \  |  / radius
	 *             \ | /
	 *              \|/
	 *               + circle-centre
	 * </pre>
	 * 
	 * @return c1 and c2
	 */
	private List<IVector2> getLineCircleIntersection(final IVector2 circleCentre, final ILine line, final double radius,
			final double margin)
	{
		/** TODO: can be placed as alternative in {@link ACircle#lineIntersections(ILine)} */
		final IVector2 lead = GeoMath.leadPointOnLine(circleCentre, line);
		final double d = GeoMath.distancePPSqr(lead, circleCentre);
		final double radius_with_margin = (radius + margin) * (radius + margin);
		List<IVector2> result = new ArrayList<IVector2>(2);// a line can only intersect twice
		
		if ((d <= (radius_with_margin + DBL_EPSILON)))
		{
			final double lambda = (radius_with_margin) - (d);
			final double length = GeoMath.distancePPSqr(AVector2.ZERO_VECTOR, line.directionVector());
			final IVector2 direction = line.directionVector().multiplyNew(Math.sqrt(lambda / length));
			final IVector2 c1 = lead.addNew(direction);
			final IVector2 c2 = lead.subtractNew(direction);
			result.add(c1);
			if (lambda >= DBL_EPSILON)
			{
				result.add(c2);
			}
			
		}
		return result;
	}
	
	
	/**
	 * there are three possibilities:
	 * 1) the line is parallel and cuts the circles as it goes right through the area
	 * 2) the line is not parallel and cuts the front-line
	 * 3) the line is equal to the front line and has either inf. intersections or none
	 */
	private List<IVector2> calcLineIntersections(final ILine line, final double margin)
	{
		List<IVector2> result = new ArrayList<IVector2>();
		
		// 1)
		final List<IVector2> negIntersections = getLineCircleIntersection(circleCentreNeg, line, getRadiusOfPenaltyArea(),
				margin);
		for (final IVector2 p : negIntersections)
		{
			if ((p.y() + DBL_EPSILON) < (circleCentreNeg.y() - DBL_EPSILON))
			{
				if (outerRectangle.isPointInShape(p, margin))
				{
					result.add(p);
				}
			}
		}
		final List<IVector2> posIntersections = getLineCircleIntersection(circleCentrePos, line, getRadiusOfPenaltyArea(),
				margin);
		for (final IVector2 p : posIntersections)
		{
			if ((p.y() - DBL_EPSILON) > (circleCentrePos.y() + DBL_EPSILON))
			{
				if (outerRectangle.isPointInShape(p, margin))
				{
					result.add(p);
				}
			}
		}
		
		if (!GeoMath.isLineParallel(frontLine, line))
		{
			ILine marginLine = new Line(frontLine.supportVector().addNew(new Vector2f(sign * margin, 0)),
					frontLine.directionVector());
			
			/**
			 * 2) the line segment is intersected when the coefficient of the front line is within the interval (0,1)
			 * Notice that the epsilon is for the edge-case between the circle and the line as the interval does not
			 * contain 0 or 1
			 **/
			
			final IVector2 pnt = GeoMath.intersectionPointLinePath(line, marginLine.supportVector(),
					marginLine.directionVector());
			if (pnt != null)
			{
				result.add(pnt);
			}
			
		}
		
		// 3)
		if (Math.abs(frontLine.supportVector().x() - line.supportVector().x()) <= (margin + DBL_EPSILON))
		{
			/** if the line is nearly identical to the front line and there has been no intersection -> fallback **/
			if (result.isEmpty())
			{
				final double plen = GeoMath.distancePP(line.supportVector(), circleCentrePos);
				final double nlen = GeoMath.distancePP(line.supportVector(), circleCentreNeg);
				result.add(plen < nlen ? circlePointUpperPos : circlePointUpperNeg);
				result.add(plen < nlen ? circlePointUpperNeg : circlePointUpperPos);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Checks if the list of points returned from {@link PenaltyArea#lineIntersections(ILine)} is empty.
	 * 
	 * @param line
	 * @param margin
	 * @return
	 */
	public boolean isLineIntersectingShape(final ILine line, final double margin)
	{
		return !calcLineIntersections(line, margin).isEmpty();
	}
	
	
	/**
	 * For super implementation (doc) see {@link I2DShape#lineIntersections(ILine)}
	 * This method checks if the given line intersects the front of the penalty area
	 * if the front part is intersected the points are returned.
	 * Currently the back of the penalty area (Goal-line) is not checked as an intersection
	 * point there is omitted.
	 */
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return calcLineIntersections(line, 0);
	}
	
	
	/**
	 * @return the penaltyMark in this penaltyArea
	 */
	public IVector2 getPenaltyMark()
	{
		return penaltyMark;
	}
	
	
	/**
	 * @return the front-line of the penalty area.
	 */
	public final Line getPenaltyAreaFrontLine()
	{
		return frontLine;
	}
	
	
	/**
	 * @return the owner
	 */
	public final ETeam getOwner()
	{
		return owner;
	}
	
	
	/**
	 * @return the radiusOfPenaltyArea
	 *         TODO: can be static
	 */
	public final double getRadiusOfPenaltyArea()
	{
		return Geometry.getDistanceToPenaltyArea();
	}
	
	
	/**
	 * TODO: Can be static
	 * 
	 * @return the lengthOfPenaltyAreaFrontLineHalf
	 */
	public final double getLengthOfPenaltyAreaFrontLineHalf()
	{
		return 0.5 * Geometry.getLengthOfPenaltyAreaFrontLine();
	}
	
	
	/**
	 * @return the penaltyCirclePos
	 */
	public final Circle getPenaltyCirclePos()
	{
		return new Circle(circleCentrePos, getRadiusOfPenaltyArea());
	}
	
	
	/**
	 * @return the penaltyCircleNeg
	 */
	public final Circle getPenaltyCircleNeg()
	{
		return new Circle(circleCentreNeg, getRadiusOfPenaltyArea());
	}
	
	
	/**
	 * @return the penaltyCirclePosCentre
	 */
	public final IVector2 getPenaltyCirclePosCentre()
	{
		return circleCentrePos;
	}
	
	
	/**
	 * @return the penaltyCircleNegCentre
	 */
	public final IVector2 getPenaltyCircleNegCentre()
	{
		return circleCentreNeg;
	}
	
	
	/**
	 * @return the penaltyRectangle
	 */
	public final Rectangle getPenaltyRectangle()
	{
		return centreRect;
	}
	
	
	/**
	 * @return the goalCentre
	 */
	public final IVector2 getGoalCenter()
	{
		return goalCentre;
	}
	
	
	/**
	 * @return the outer penalty area box
	 */
	public final Rectangle getOuterArea()
	{
		return outerRectangle;
	}
	
	
	/**
	 * @return the negative frontline point
	 */
	public final IVector2 getCirclePointUpperNeg()
	{
		return circlePointUpperNeg;
	}
	
	
	/**
	 * @return the positive frontline point
	 */
	public final IVector2 getCirclePointUpperPos()
	{
		return circlePointUpperPos;
	}
	
}