/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 2, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent
public class SpecialMoveCommand
{
	
	private List<IVector2>	movePosition		= new ArrayList<IVector2>();
	private List<Float>		moveTimes			= new ArrayList<Float>();
	// if response Step == 0, dont send any response
	private int					responseStep		= 0;
	private float				forceResponseTime	= 10.0f;
	
	
	/**
	 * @return the movePosition
	 */
	public List<IVector2> getMovePosition()
	{
		return movePosition;
	}
	
	
	/**
	 * @param movePosition the movePosition to set
	 */
	public void setMovePosition(final List<IVector2> movePosition)
	{
		this.movePosition = movePosition;
	}
	
	
	/**
	 * @return the moveTimes
	 */
	public List<Float> getMoveTimes()
	{
		return moveTimes;
	}
	
	
	/**
	 * @param moveTimes the moveTimes to set
	 */
	public void setMoveTimes(final List<Float> moveTimes)
	{
		this.moveTimes = moveTimes;
	}
	
	
	/**
	 * @return the forceResponseTime
	 */
	public float getForceResponseTime()
	{
		return forceResponseTime;
	}
	
	
	/**
	 * @param forceResponseTime the forceResponseTime to set
	 */
	public void setForceResponseTime(final float forceResponseTime)
	{
		this.forceResponseTime = forceResponseTime;
	}
	
	
	/**
	 * @return the responseStep
	 */
	public int getResponseStep()
	{
		return responseStep;
	}
	
	
	/**
	 * @param responseStep the responseStep to set
	 */
	public void setResponseStep(final int responseStep)
	{
		this.responseStep = responseStep;
	}
}