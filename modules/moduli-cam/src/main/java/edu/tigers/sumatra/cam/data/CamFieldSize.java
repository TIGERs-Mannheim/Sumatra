/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldCircularArc;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;


/**
 * SSL vision field dimensions
 */
@Value
public class CamFieldSize
{
	double fieldLength;
	double fieldWidth;
	double goalWidth;
	double goalDepth;
	double boundaryWidth;
	List<CamFieldLine> fieldLines;
	List<CamFieldArc> fieldArcs;


	public CamFieldSize(final SSL_GeometryFieldSize field)
	{
		fieldLength = field.getFieldLength();
		fieldWidth = field.getFieldWidth();
		goalWidth = field.getGoalWidth();
		goalDepth = field.getGoalDepth();
		boundaryWidth = field.getBoundaryWidth();
		fieldLines = field.getFieldLinesList().stream()
				.map(this::map)
				.collect(Collectors.toUnmodifiableList());
		fieldArcs = field.getFieldArcsList().stream()
				.map(this::map)
				.collect(Collectors.toUnmodifiableList());
	}


	private CamFieldLine map(SSL_FieldLineSegment lineSegment)
	{
		IVector2 p1 = Vector2.fromXY(lineSegment.getP1().getX(), lineSegment.getP1().getY());
		IVector2 p2 = Vector2.fromXY(lineSegment.getP2().getX(), lineSegment.getP2().getY());
		ILine line = Line.fromPoints(p1, p2);
		return new CamFieldLine(lineSegment.getName(), lineSegment.getType(), lineSegment.getThickness(), line);
	}


	private CamFieldArc map(SSL_FieldCircularArc arc)
	{
		var center = Vector2.fromXY(arc.getCenter().getX(), arc.getCenter().getY());
		var circle = Arc.createArc(center, arc.getRadius(), arc.getA1(), arc.getA2() - arc.getA1());
		return new CamFieldArc(arc.getName(), arc.getType(), arc.getThickness(), circle);
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
}
