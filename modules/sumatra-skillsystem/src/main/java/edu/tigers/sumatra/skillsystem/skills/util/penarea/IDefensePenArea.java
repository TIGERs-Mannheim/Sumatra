/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import java.util.List;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Represents a shape around the penalty area used for defender positioning.
 * 
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public interface IDefensePenArea
{
	
	IDefensePenArea withMargin(double margin);
	
	
	IVector2 stepAlongPenArea(IVector2 startPoint, double length);
	
	
	IVector2 stepAlongPenArea(double length);
	
	
	boolean isPointInShape(IVector2 point);
	
	
	IVector2 projectPointOnPenaltyAreaLine(IVector2 point);
	
	
	double lengthToPointOnPenArea(IVector2 point);
	
	
	double getLength();
	
	
	boolean isPointInShape(IVector2 point, double margin);
	
	
	IVector2 nearestPointOutside(IVector2 point, IVector2 pointToBuildLine);
	
	
	IVector2 nearestPointOutside(IVector2 point);
	
	
	List<IVector2> lineIntersections(ILine line);
	
	
	IVector2 nearestPointInside(IVector2 pos);
	
	
	double getFrontLineHalfLength();
}
