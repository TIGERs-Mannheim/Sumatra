/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_FieldCicularArc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;


/**
 * SSL vision field dimensions
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamFieldSize
{
	private final float					fieldLength;
	private final float					fieldWidth;
	private final float					goalWidth;
	private final float					goalDepth;
	private final float					boundaryWidth;
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
			IVector2 p1 = new Vector2(lineSegment.getP1().getX(), lineSegment.getP1().getY());
			IVector2 p2 = new Vector2(lineSegment.getP2().getX(), lineSegment.getP2().getY());
			ILine line = Line.newLine(p1, p2);
			fieldLines.add(new CamFieldLine(line, lineSegment.getName(), lineSegment.getThickness()));
		}
		fieldArcs = new ArrayList<>(field.getFieldArcsCount());
		for (SSL_FieldCicularArc arc : field.getFieldArcsList())
		{
			IVector2 center = new Vector2(arc.getCenter().getX(), arc.getCenter().getY());
			ICircle circle = new Circle(center, arc.getRadius());
			fieldArcs.add(new CamFieldArc(circle, arc.getName(), arc.getThickness(), arc.getA1(), arc.getA2()));
		}
	}
	
	
	/**
	 * @return the fieldLength
	 */
	public final float getFieldLength()
	{
		return fieldLength;
	}
	
	
	/**
	 * @return the fieldWidth
	 */
	public final float getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	/**
	 * @return the goalWidth
	 */
	public final float getGoalWidth()
	{
		return goalWidth;
	}
	
	
	/**
	 * @return the goalDepth
	 */
	public final float getGoalDepth()
	{
		return goalDepth;
	}
	
	
	/**
	 * @return the boundaryWidth
	 */
	public final float getBoundaryWidth()
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
	
	
}
