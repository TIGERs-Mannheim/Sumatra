/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import com.sleepycat.persist.model.Persistent;


/**
 * uninitialized object id
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class UninitializedID extends AObjectID
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 4905857112422684746L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Instead of creating a new instance, use the field from AObjectID
	 */
	public UninitializedID()
	{
		super(AObjectID.UNINITIALIZED_ID);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
