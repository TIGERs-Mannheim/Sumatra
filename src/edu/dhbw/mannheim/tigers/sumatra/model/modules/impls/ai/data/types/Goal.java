/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;


/**
 * 
 * This is a immutable representation of a goal.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Goal
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float		size;
	private final Vector2f	goalCenter;
	private final Vector2f  goalPostLeft;
	private final Vector2f  goalPostRight;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Goal(float size, IVector2 goalCenter)
	{
		this.size = size;
		this.goalCenter = new Vector2f(goalCenter);
		
		this.goalPostLeft = new Vector2f(goalCenter.x(), goalCenter.y() + size / 2);
		this.goalPostRight = new Vector2f(goalCenter.x(), goalCenter.y() - size / 2);
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
