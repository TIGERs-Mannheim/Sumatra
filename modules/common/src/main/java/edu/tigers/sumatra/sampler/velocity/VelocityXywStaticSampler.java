/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sampler.velocity;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VelocityXywStaticSampler implements IVelocityXywSampler
{
	private int							sampleIteration	= 0;
	private int							numSamples			= 20;
	
	private final List<IVector3>	velocities			= new ArrayList<>();
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public VelocityXywStaticSampler()
	{
		// velocities.add(new Vector3(2.0, 0, 0));
		
		for (double s = 1.5; s < 2.7; s += 0.1)
		{
			for (int i = 0; i < 3; i++)
			{
				velocities.add(new Vector3(s, 0, 0));
			}
		}
		numSamples = velocities.size();
	}
	
	
	@Override
	public IVector3 getNextVelocity()
	{
		return velocities.get((sampleIteration++) % velocities.size());
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	@Override
	public int getNeededSamples()
	{
		return numSamples;
	}
	
	
	/**
	 * @return the sampleIteration
	 */
	public final int getSampleIteration()
	{
		return sampleIteration;
	}
	
	
	/**
	 * @param sampleIteration the sampleIteration to set
	 */
	public final void setSampleIteration(final int sampleIteration)
	{
		this.sampleIteration = sampleIteration;
	}
	
	
	@Override
	public boolean hasNext()
	{
		return sampleIteration < numSamples;
	}
}
