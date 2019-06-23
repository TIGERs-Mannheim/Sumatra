/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;


/**
 * This is a immutable representation of a goal.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Goal
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private final double		size;
	private final Vector2f	goalCenter;
	private final Vector2f	goalPostLeft;
	private final Vector2f	goalPostRight;
	
	private final Vector2	goalPostLeftBack;
	private final Vector2	goalPostRightBack;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param size
	 * @param goalCenter
	 * @param depth
	 */
	public Goal(final double size, final IVector2 goalCenter, final double depth)
	{
		this.size = size;
		this.goalCenter = new Vector2f(goalCenter);
		
		goalPostLeft = new Vector2f(goalCenter.x(), goalCenter.y() + (size / 2.0));
		goalPostRight = new Vector2f(goalCenter.x(), goalCenter.y() - (size / 2.0));
		
		goalPostLeftBack = goalPostLeft.addNew(AVector2.X_AXIS.scaleToNew(depth));
		goalPostRightBack = goalPostRight.addNew(AVector2.X_AXIS.scaleToNew(depth));
		
	}
	
	
	/**
	 * isPointInShape with a margin around the shape
	 * 
	 * @param point
	 * @param point2
	 * @param margin
	 * @return
	 */
	public boolean isLineCrossingGoal(final IVector2 point, final IVector2 point2, final double margin)
	{
		try
		{
			if (GeoMath.distanceBetweenLineSegments(goalPostLeft, goalPostLeftBack, point, point2) < margin)
			{
				return true;
			}
			if (GeoMath.distanceBetweenLineSegments(goalPostLeftBack, goalPostRightBack, point, point2) < margin)
			{
				return true;
			}
			if (GeoMath.distanceBetweenLineSegments(goalPostRight, goalPostRightBack, point, point2) < margin)
			{
				return true;
			}
		} catch (MathException err)
		{
			return false;
		}
		return false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the size of the goal.
	 */
	public double getSize()
	{
		return size;
	}
	
	
	/**
	 * @return the vector of the goal.
	 */
	public Vector2f getGoalCenter()
	{
		return goalCenter;
	}
	
	
	/**
	 * @return the postion of the left goal post.
	 */
	public Vector2f getGoalPostLeft()
	{
		return goalPostLeft;
		
	}
	
	
	/**
	 * @return the postion of the right goal post.
	 */
	public Vector2f getGoalPostRight()
	{
		return goalPostRight;
	}
	
	
}
