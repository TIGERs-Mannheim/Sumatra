/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.04.2011
 * Author(s): DanielW
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * collects values associated with a path that the GUI can display
 * This class is deprecated but can not be deleted, because BerkeleyDB needs its existence for old record data
 * 
 * @deprecated
 * @author DanielW
 */
@Persistent
@Deprecated
public class PathGuiFeatures
{
	private Float		virtualVehicle	= null;
	private IVector2	currentMove		= null;
	
	
	/**
	 * @return the virtualVehicle
	 */
	public Float getVirtualVehicle()
	{
		return virtualVehicle;
	}
	
	
	/**
	 * @param virtualVehicle the virtualVehicle to set
	 */
	public void setVirtualVehicle(final Float virtualVehicle)
	{
		this.virtualVehicle = virtualVehicle;
	}
	
	
	/**
	 * @return the currentMove
	 */
	public IVector2 getCurrentMove()
	{
		return currentMove;
	}
	
	
	/**
	 * @param currentMove the currentMove to set
	 */
	public void setCurrentMove(final IVector2 currentMove)
	{
		this.currentMove = currentMove;
	}
}
