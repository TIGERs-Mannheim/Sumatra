/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OpponentPenaltyArea implements IPenaltyArea
{
	private final IPenaltyArea penaltyArea;
	
	
	/**
	 * @param radius of the quarter circles
	 * @param frontLineLength length of the front line
	 */
	public OpponentPenaltyArea(final double radius, final double frontLineLength)
	{
		penaltyArea = new PenaltyArea(radius, frontLineLength);
	}
	
	
	private IVector2 mirror(IVector2 point)
	{
		return point.multiplyNew(-1);
	}
	
	
	private ILine mirror(ILine line)
	{
		return Line.fromPoints(mirror(line.getStart()), mirror(line.getEnd()));
	}
	
	
	private ILineSegment mirror(ILineSegment line)
	{
		return Lines.segmentFromPoints(line.getStart(), line.getEnd());
	}
	
	
	@Override
	public OpponentPenaltyArea withMargin(final double margin)
	{
		return new OpponentPenaltyArea(getRadius() + margin, getFrontLineLength());
	}
	
	
	@Override
	public double getPerimeterFrontCurve()
	{
		return penaltyArea.getPerimeterFrontCurve();
	}
	
	
	@Override
	public IVector2 stepAlongPenArea(final double length)
	{
		return mirror(penaltyArea.stepAlongPenArea(length));
	}
	
	
	@Override
	public boolean isPointInShapeOrBehind(final IVector2 point)
	{
		return penaltyArea.isPointInShapeOrBehind(mirror(point));
	}
	
	
	@Override
	public IVector2 getGoalCenter()
	{
		return mirror(penaltyArea.getGoalCenter());
	}
	
	
	@Override
	public ILineSegment getFrontLine()
	{
		return mirror(penaltyArea.getFrontLine());
	}
	
	
	@Override
	public double getRadius()
	{
		return penaltyArea.getRadius();
	}
	
	
	@Override
	public double getFrontLineLength()
	{
		return penaltyArea.getFrontLineLength();
	}
	
	
	@Override
	public double getFrontLineHalfLength()
	{
		return penaltyArea.getFrontLineHalfLength();
	}
	
	
	@Override
	public IArc getArcNeg()
	{
		return penaltyArea.getArcNeg().mirror();
	}
	
	
	@Override
	public IArc getArcPos()
	{
		return penaltyArea.getArcPos().mirror();
	}
	
	
	@Override
	public boolean isBehindPenaltyArea(final IVector2 point)
	{
		return penaltyArea.isBehindPenaltyArea(mirror(point));
	}


	@Override
	public double lengthToPointOnPenArea(final IVector2 point)
	{
		return penaltyArea.lengthToPointOnPenArea(mirror(point));
	}
	
	
	@Override
	public IVector2 stepAlongPenArea(final IVector2 startPoint, final double length)
	{
		return mirror(penaltyArea.stepAlongPenArea(mirror(startPoint), length));
	}
	
	
	@Override
	public IVector2 projectPointOnPenaltyAreaLine(final IVector2 point)
	{
		return mirror(penaltyArea.projectPointOnPenaltyAreaLine(mirror(point)));
	}
	
	
	@Override
	public double getLength()
	{
		return penaltyArea.getLength();
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return penaltyArea.isPointInShape(mirror(point));
	}
	
	
	@Override
	public boolean isIntersectingWithLineSegment(final ILine line)
	{
		return penaltyArea.isIntersectingWithLineSegment(mirror(line));
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return penaltyArea.isPointInShape(mirror(point), margin);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return mirror(penaltyArea.nearestPointOutside(mirror(point)));
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		return mirror(penaltyArea.nearestPointInside(mirror(point)));
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return penaltyArea.isIntersectingWithLine(mirror(line));
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return penaltyArea.lineIntersections(mirror(line)).stream().map(this::mirror).collect(Collectors.toList());
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine)
	{
		return mirror(penaltyArea.nearestPointOutside(mirror(point), mirror(pointToBuildLine)));
	}
	
	
	@Override
	public IRectangle getInnerRectangle()
	{
		return penaltyArea.getInnerRectangle().mirror();
	}
}
