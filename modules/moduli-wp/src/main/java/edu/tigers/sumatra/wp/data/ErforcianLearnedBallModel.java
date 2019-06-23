/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2016
 * Author(s): ArneS <arne.sachtler@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.ml.model.ALearnedModel;


/**
 * Learned ball model based on LearnedBallModel.java (quadratic fitting)
 * and ERforce TDP 2016 (two friction phases)
 * 
 * @author ArneS <arne.sachtler@dlr.de>
 */
public class ErforcianLearnedBallModel extends ALearnedModel implements ILearnedBallModel
{
	
	/**
	 * @param base
	 * @param identifier
	 */
	protected ErforcianLearnedBallModel(final String base, final String identifier)
	{
		super(base, identifier);
	}
	
	
	@Override
	public IVector2 getPosByTime(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			final double time)
	{
		return null;
	}
	
	
	@Override
	public double getDistByTime(final IVector2 currentVel, final IVector2 initialVel, final double time)
	{
		return 0;
	}
	
	
	@Override
	public IVector2 getPosByVel(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			final double velocity)
	{
		return null;
	}
	
	
	@Override
	public double getDistByVel(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			final double velocity)
	{
		return 0;
	}
	
	
	@Override
	public double getTimeByDist(final double currentVel, final double initialVel, final double dist)
	{
		return 0;
	}
	
	
	@Override
	public double getTimeByVel(final double currentVel, final double initialVel, final double velocity)
	{
		return 0;
	}
	
	
	@Override
	public double getVelByDist(final double currentVel, final double initialVel, final double dist)
	{
		return 0;
	}
	
	
	@Override
	public double getVelForTime(final double endVel, final double time)
	{
		return 0;
	}
	
	
	@Override
	public double getVelForDist(final double dist, final double endVel)
	{
		return 0;
	}
	
	
	@Override
	protected double[] getDefaultParams()
	{
		return null;
	}
	
}
