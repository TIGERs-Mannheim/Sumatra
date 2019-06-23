/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.area;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


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
	
	
	private final float		size;
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
	public Goal(final float size, final IVector2 goalCenter, final float depth)
	{
		this.size = size;
		this.goalCenter = new Vector2f(goalCenter);
		
		goalPostLeft = new Vector2f(goalCenter.x(), goalCenter.y() + (size / 2));
		goalPostRight = new Vector2f(goalCenter.x(), goalCenter.y() - (size / 2));
		
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
	public boolean isLineCrossingGoal(final IVector2 point, final IVector2 point2, final float margin)
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
	public float getSize()
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
