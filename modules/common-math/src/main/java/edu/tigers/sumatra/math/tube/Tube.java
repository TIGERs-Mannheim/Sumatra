/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import java.util.List;
import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * tube: consists of two similar half circles, on each end one
 * those circles are connected by two parallel lines with their distance equalling the radius of the circles
 * 
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
@Persistent(version = 1)
public class Tube implements ITube
{
	
	private final IVector2 startCenter;
	private final IVector2 endCenter;
	private final double radius;
	
	
	/**
	 * for DB only
	 */
	@SuppressWarnings("unused")
	private Tube()
	{
		startCenter = Vector2f.ZERO_VECTOR;
		endCenter = Vector2f.ZERO_VECTOR;
		radius = 1;
	}
	
	
	private Tube(final IVector2 startCenter, final IVector2 endCenter, final double radius)
	{
		this.startCenter = startCenter;
		this.endCenter = endCenter;
		this.radius = radius;
	}
	
	
	private Tube(final IVector2 center, final double length, final double radius, final IVector2 direction)
	{
		startCenter = center.addNew(direction.scaleToNew(length / 2));
		endCenter = center.addNew(direction.scaleToNew(-length / 2));
		this.radius = radius;
	}
	
	
	/**
	 * @param startCenter center of first circle
	 * @param endCenter center of second circle
	 * @param radius [mm]
	 * @return
	 */
	public static Tube create(final IVector2 startCenter, final IVector2 endCenter, final double radius)
	{
		
		return new Tube(startCenter, endCenter, radius);
	}
	
	
	/**
	 * @param center middle point between both ends
	 * @param length from one circle center to the other [mm]
	 * @param radius of circles in [mm]
	 * @param direction direction vector of tube
	 * @return
	 */
	public static Tube fromCenter(final IVector2 center, final double length, final double radius,
			final IVector2 direction)
	{
		return new Tube(center, length, radius, direction);
	}
	
	
	/**
	 * tube around LineSegment with given radius [mm]
	 * adds radius to start and endpoint of lineSegment
	 * 
	 * @param lineSegment
	 * @param radius [mm]
	 * @return
	 */
	public static Tube fromLineSegment(final ILineSegment lineSegment, final double radius)
	{
		return new Tube(lineSegment.getStart(), lineSegment.getEnd(), radius);
	}
	
	
	/**
	 * #
	 * 
	 * @param line any line
	 * @param length from one circleCenter to the other in [mm]
	 * @param radius of circle/ tube
	 * @return
	 */
	public static Tube fromLineWithLength(final ILine line, final double length, final double radius)
	{
		return new Tube(line.getStart(), line.getStart().addNew(line.directionVector().scaleToNew(length)), radius);
		
	}
	
	
	/**
	 * #
	 * 
	 * @param line any line
	 * @param radius of circle/ tube in [mm]
	 * @return
	 */
	public static Tube fromLine(final ILine line, final double radius)
	{
		return new Tube(line.getStart(), line.getEnd(), radius);
	}
	
	
	@Override
	public double radius()
	{
		return radius;
	}
	
	
	@Override
	public IVector2 startCenter()
	{
		return startCenter;
	}
	
	
	@Override
	public IVector2 endCenter()
	{
		return endCenter;
	}
	
	
	@Override
	public IVector2 center()
	{
		return Lines.segmentFromPoints(startCenter, endCenter).getCenter();
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		ILineSegment line = Lines.segmentFromPoints(startCenter, endCenter);
		return line.distanceTo(point) < radius;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return withMargin(margin).isPointInShape(point);
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		if (startCenter.equals(endCenter))
		{
			return Circle.createCircle(startCenter, radius).isIntersectingWithLine(line);
		}
		ICircle startCircle = Circle.createCircle(startCenter, radius);
		ICircle endCircle = Circle.createCircle(endCenter, radius);
		ILine tubeLine = Line.fromPoints(startCenter, endCenter);
		Optional<IVector2> intersect = line.intersectionWith(tubeLine);
		boolean isIntersecting = false;
		if (intersect.isPresent())
		{
			isIntersecting = Lines.segmentFromPoints(startCenter, endCenter).isPointOnLine(intersect.get());
		}
		return startCircle.isIntersectingWithLine(line) || endCircle.isIntersectingWithLine(line) || isIntersecting;
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (startCenter.equals(endCenter))
		{
			return Circle.createCircle(startCenter, radius).nearestPointOutside(point);
		}
		IVector2 nearestPointOutside = point;
		ILineSegment tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);
		if (tubeLineSeg.distanceTo(point) < radius)
		{
			IVector2 leadPoint = tubeLineSeg.closestPointOnLine(point);
			if (leadPoint.equals(point))
			{
				IVector2 dir = tubeLineSeg.directionVector().getNormalVector().scaleTo(radius);
				nearestPointOutside = point.addNew(dir);
			} else
			{
				nearestPointOutside = LineMath.stepAlongLine(leadPoint, point, radius);
			}
		}
		return nearestPointOutside;
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		ILineSegment tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);
		if (tubeLineSeg.distanceTo(point) < radius)
		{
			return point;
		}
		IVector2 leadPoint = tubeLineSeg.closestPointOnLine(point);
		return LineMath.stepAlongLine(leadPoint, point, radius - 1);
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		if (startCenter.equals(endCenter))
		{
			return Circle.createCircle(startCenter, radius).lineIntersections(line);
		}
		List<IVector2> intersections;
		ICircle startCircle = Circle.createCircle(startCenter, radius);
		ICircle endCircle = Circle.createCircle(endCenter, radius);
		ILineSegment tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);
		
		intersections = startCircle.lineIntersections(line);
		for (IVector2 v : intersections)
		{
			if (!tubeLineSeg.closestPointOnLine(v).equals(startCenter))
			{
				intersections.remove(v);
			}
		}
		List<IVector2> intersectsAtEnd = endCircle.lineIntersections(line);
		for (IVector2 v : intersectsAtEnd)
		{
			if (tubeLineSeg.closestPointOnLine(v).equals(endCenter))
			{
				intersections.add(v);
			}
		}
		
		IVector2 start1 = startCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(radius));
		IVector2 end1 = endCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(radius));
		IVector2 start2 = startCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(-radius));
		IVector2 end2 = endCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(-radius));
		ILine line1 = Line.fromPoints(start1, end1);
		ILine line2 = Line.fromPoints(start2, end2);
		getLineIntersections(line, line1, intersections);
		getLineIntersections(line, line2, intersections);
		return intersections;
	}
	
	
	private void getLineIntersections(ILine line, ILine lineOfTube, List<IVector2> intersections)
	{
		Optional<IVector2> intersect = lineOfTube.intersectionWith(line);
		if (intersect.isPresent()
				&& Lines.segmentFromPoints(lineOfTube.getStart(), lineOfTube.getEnd()).isPointOnLine(intersect.get())
				&& !intersections.contains(intersect.get()))
		{
			intersections.add(intersect.get());
		}
	}
	
	
	@Override
	public I2DShape withMargin(final double margin)
	{
		return new Tube(startCenter, endCenter, radius + margin);
	}
}
