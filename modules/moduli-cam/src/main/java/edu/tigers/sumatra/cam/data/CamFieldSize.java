/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_FieldCicularArc;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * SSL vision field dimensions
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamFieldSize
{
	private final double					fieldLength;
	private final double					fieldWidth;
	private final double					goalWidth;
	private final double					goalDepth;
	private final double					boundaryWidth;
	private final List<CamFieldLine>	fieldLines;
	private final List<CamFieldArc>	fieldArcs;
	
	
	/**
	 * @param field
	 */
	public CamFieldSize(final SSL_GeometryFieldSize field)
	{
		fieldLength = field.getFieldLength();
		fieldWidth = field.getFieldWidth();
		goalWidth = field.getGoalWidth();
		goalDepth = field.getGoalDepth();
		boundaryWidth = field.getBoundaryWidth();
		fieldLines = new ArrayList<>(field.getFieldLinesCount());
		for (SSL_FieldLineSegment lineSegment : field.getFieldLinesList())
		{
			IVector2 p1 = Vector2.fromXY(lineSegment.getP1().getX(), lineSegment.getP1().getY());
			IVector2 p2 = Vector2.fromXY(lineSegment.getP2().getX(), lineSegment.getP2().getY());
			ILine line = Line.fromPoints(p1, p2);
			fieldLines.add(new CamFieldLine(line, lineSegment.getName(), lineSegment.getThickness()));
		}
		fieldArcs = new ArrayList<>(field.getFieldArcsCount());
		for (SSL_FieldCicularArc arc : field.getFieldArcsList())
		{
			IVector2 center = Vector2.fromXY(arc.getCenter().getX(), arc.getCenter().getY());
			ICircle circle = Circle.createCircle(center, arc.getRadius());
			fieldArcs.add(new CamFieldArc(circle, arc.getName(), arc.getThickness(), arc.getA1(), arc.getA2()));
		}
	}
	
	
	/**
	 * @return the fieldLength
	 */
	public final double getFieldLength()
	{
		return fieldLength;
	}
	
	
	/**
	 * @return the fieldWidth
	 */
	public final double getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	/**
	 * @return the goalWidth
	 */
	public final double getGoalWidth()
	{
		return goalWidth;
	}
	
	
	/**
	 * @return the goalDepth
	 */
	public final double getGoalDepth()
	{
		return goalDepth;
	}
	
	
	/**
	 * @return the boundaryWidth
	 */
	public final double getBoundaryWidth()
	{
		return boundaryWidth;
	}
	
	
	/**
	 * @return the fieldLines
	 */
	public final List<CamFieldLine> getFieldLines()
	{
		return fieldLines;
	}
	
	
	/**
	 * Get field rectangle with boundary width.
	 * 
	 * @return
	 */
	public IRectangle getFieldWithBoundary()
	{
		return Rectangle.fromCenter(Vector2f.ZERO_VECTOR, fieldLength + (2 * boundaryWidth),
				fieldWidth + (2 * boundaryWidth));
	}
	
	
	/**
	 * Get field rectangle.
	 * 
	 * @return
	 */
	public IRectangle getField()
	{
		return Rectangle.fromCenter(Vector2f.ZERO_VECTOR, fieldLength, fieldWidth);
	}
	
	
	/**
	 * @return the fieldArcs
	 */
	public List<CamFieldArc> getFieldArcs()
	{
		return fieldArcs;
	}
}
