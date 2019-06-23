/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sampler.velocity;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VelocityXywContinousSampler implements IVelocityXywSampler
{
	private static final double	EPS			= 0.01;
	
	private final double				angleMin		= 0;
	private final double				angleMax		= AngleMath.PI_TWO;
	private final double				angleStep	= AngleMath.PI_TWO / 72;
	
	private final List<IVector3>	velocities	= new ArrayList<>();
	
	private int							i				= 0;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public VelocityXywContinousSampler()
	{
		// double[] speeds = new double[] { 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0, 2.2, 2.4 };
		// double[] aVels = new double[] { -20, -10, -5, -1, 1, 5, 10, 20 };
		
		double[] speeds = new double[] { 0.5, 1.0, 1.5, 1.75 };
		// double[] speeds = new double[] {};
		// double[] speeds = new double[] { 0.1, 0.5, 1.0, 1.5 };
		double[] aVels = new double[] {};
		
		// List<Double> speeds = new ArrayList<>();
		// for (double s = 0.1; s < 2.5; s += 0.1)
		// {
		// speeds.add(s);
		// }
		
		for (double angle = angleMin; angle < (angleMax - EPS); angle += angleStep)
		{
			for (double speed : speeds)
			{
				IVector2 xyVel = new Vector2(angle).scaleTo(speed);
				IVector3 vel = new Vector3(xyVel, 0);
				velocities.add(vel);
			}
		}
		for (double aVel : aVels)
		{
			velocities.add(new Vector3(0, 0, aVel));
		}
	}
	
	
	@Override
	public IVector3 getNextVelocity()
	{
		i = i % velocities.size();
		return velocities.get(i++);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	@Override
	public int getNeededSamples()
	{
		return velocities.size();
	}
	
	
	@Override
	public boolean hasNext()
	{
		return i < velocities.size();
	}
}
