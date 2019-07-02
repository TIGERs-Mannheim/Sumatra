/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class AngleRaterMath
{
	
	private AngleRaterMath()
	{
		// Hide constructor
	}
	
	
	/**
	 * Generates a triangle using the given points
	 * 
	 * @param endLeft
	 * @param endRight
	 * @param extend
	 * @param start
	 * @return
	 */
	public static ITriangle getTriangle(final IVector2 endLeft, final IVector2 endRight, final boolean extend,
			final IVector2 start)
	{
		IVector2 left = endLeft;
		IVector2 right = endRight;
		
		if (extend)
		{
			IVector2 leftLine = endLeft.subtractNew(start);
			IVector2 rightLine = endRight.subtractNew(start);
			
			double goalDepth = Math.max(Geometry.getGoalOur().getDepth(), Geometry.getGoalTheir().getDepth());
			
			double extensionLeft = goalDepth
					/ AngleMath.cos(Vector2.fromX(leftLine.x()).angleToAbs(leftLine).orElse(0.0));
			double extensionRight = goalDepth
					/ AngleMath.cos(Vector2.fromX(rightLine.x()).angleToAbs(rightLine).orElse(0.0));
			
			
			left = endLeft.addNew(leftLine.scaleToNew(extensionLeft));
			right = endRight.addNew(rightLine.scaleToNew(extensionRight));
		}
		
		return Triangle.fromCorners(start, left, right);
	}
}
