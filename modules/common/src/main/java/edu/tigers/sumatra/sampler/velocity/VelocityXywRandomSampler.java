/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sampler.velocity;

import java.util.Random;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VelocityXywRandomSampler implements IVelocityXywSampler
{
	private Random rnd = new Random(42);
	private double maxXyVel = 1.5;
	private double maxWVel = 5;
	private static final int NUM_SAMPLES = 50;
	private int i = 0;
	
	
	@Override
	public IVector3 getNextVelocity()
	{
		i++;
		double angle = rnd.nextDouble() * AngleMath.PI_TWO;
		double vel = rnd.nextDouble() * maxXyVel;
		IVector2 xyVel = Vector2.fromAngle(angle).scaleTo(vel);
		double aVel = (rnd.nextGaussian() * maxWVel) / 2.0;
		return Vector3.from2d(xyVel, aVel);
	}
	
	
	@Override
	public int getNeededSamples()
	{
		return NUM_SAMPLES;
	}
	
	
	/**
	 * @param seed
	 */
	public void setSeed(final long seed)
	{
		rnd = new Random(seed);
	}
	
	
	/**
	 * @return the maxXyVel
	 */
	public final double getMaxXyVel()
	{
		return maxXyVel;
	}
	
	
	/**
	 * @param maxXyVel the maxXyVel to set
	 */
	public final void setMaxXyVel(final double maxXyVel)
	{
		this.maxXyVel = maxXyVel;
	}
	
	
	/**
	 * @return the maxWVel
	 */
	public final double getMaxWVel()
	{
		return maxWVel;
	}
	
	
	/**
	 * @param maxWVel the maxWVel to set
	 */
	public final void setMaxWVel(final double maxWVel)
	{
		this.maxWVel = maxWVel;
	}
	
	
	@Override
	public boolean hasNext()
	{
		return i < NUM_SAMPLES;
	}
}
