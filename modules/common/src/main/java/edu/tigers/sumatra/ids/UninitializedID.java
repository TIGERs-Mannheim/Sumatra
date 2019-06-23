/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
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
	/**
	 * Instead of creating a new instance, use the field from AObjectID
	 */
	public UninitializedID()
	{
		super(AObjectID.UNINITIALIZED_ID);
	}
}
