/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 2, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.ArrayList;
import java.util.List;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent
public class SpecialMoveCommand
{
	
	private List<IVector2>	movePosition			= new ArrayList<>();
	private List<Double>		moveTimes				= new ArrayList<>();
	// if response Step == 0, dont send any response
	private int					responseStep			= 0;
	private double				forceResponseTime		= 10.0;
	private double				timeUntilPassArrives	= 0.0;
	
	
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
	public List<Double> getMoveTimes()
	{
		return moveTimes;
	}
	
	
	/**
	 * @param moveTimes the moveTimes to set
	 */
	public void setMoveTimes(final List<Double> moveTimes)
	{
		this.moveTimes = moveTimes;
	}
	
	
	/**
	 * @return the forceResponseTime
	 */
	public double getForceResponseTime()
	{
		return forceResponseTime;
	}
	
	
	/**
	 * @param forceResponseTime the forceResponseTime to set
	 */
	public void setForceResponseTime(final double forceResponseTime)
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


	/**
	 * @return the timeUntilPassArrives
	 */
	public double getTimeUntilPassArrives()
	{
		return timeUntilPassArrives;
	}


	/**
	 * @param timeUntilPassArrives the timeUntilPassArrives to set
	 */
	public void setTimeUntilPassArrives(double timeUntilPassArrives)
	{
		this.timeUntilPassArrives = timeUntilPassArrives;
	}
}