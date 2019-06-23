/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 15, 2015
 * Author(s): geforce
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author geforce
 */
public interface ITrackedObject
{
	
	/**
	 * @return the pos
	 */
	IVector2 getPos();
	
	
	/**
	 * @return the vel
	 */
	IVector2 getVel();
	
	
	/**
	 * @return the acc
	 */
	IVector2 getAcc();
	
	
	/**
	 * @return id
	 */
	AObjectID getBotId();
	
	
	/**
	 * @return
	 */
	long getTimestamp();
	
}