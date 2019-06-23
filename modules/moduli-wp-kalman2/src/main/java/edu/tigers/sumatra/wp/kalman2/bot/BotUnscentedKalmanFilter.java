/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2.bot;

import java.util.Arrays;

import edu.tigers.sumatra.filter.ukf.AUnscentedKalmanFilter;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotUnscentedKalmanFilter extends AUnscentedKalmanFilter implements IBotFilter
{
	private double		deltaT	= 0;
	private long		curTimestamp;
	private double[]	predState, curState;
							
							
	/**
	 * @param initState
	 * @param timestamp
	 */
	public BotUnscentedKalmanFilter(final IVector3 initState, final long timestamp)
	{
		super(6, 3, 0);
		setProcessNoise(0.001, 0.001, 0.001, 0.5, 0.5, 0.5);
		setMeasurementNoise(0.01, 0.01, 0.01);
		setState(initState.x(), initState.y(), initState.z(), 0, 0, 0);
		addOrientationComponent(2, 2);
		curTimestamp = timestamp;
		predState = new double[] { initState.x(), initState.y(), initState.z(), 0, 0, 0 };
		curState = predState;
	}
	
	
	@Override
	protected void stateFunction(final double[] stateInOut, final double[] controlIn)
	{
		// non-linear bot model
		double[] pos = Arrays.copyOfRange(stateInOut, 0, 3);
		double[] vel = Arrays.copyOfRange(stateInOut, 3, 6);
		
		// rotate velocity according to angular velocity
		// double rot = vel[2] * deltaT;
		// vel[0] = (vel[0] * Math.cos(rot)) - (vel[1] * Math.sin(rot));
		// vel[1] = (vel[1] * Math.cos(rot)) + (vel[0] * Math.sin(rot));
		
		pos[0] += vel[0] * deltaT;
		pos[1] += vel[1] * deltaT;
		pos[2] += vel[2] * deltaT;
		
		stateInOut[0] = pos[0];
		stateInOut[1] = pos[1];
		stateInOut[2] = pos[2];
		stateInOut[3] = vel[0];
		stateInOut[4] = vel[1];
		stateInOut[5] = vel[2];
	}
	
	
	@Override
	protected void measurementFunction(final double[] stateIn, final double[] controlIn, final double[] measOut)
	{
		measOut[0] = stateIn[0];
		measOut[1] = stateIn[1];
		measOut[2] = stateIn[2];
	}
	
	
	@Override
	public void update(final IVector3 pos, final long timestamp)
	{
		deltaT = (timestamp - curTimestamp) / 1e9;
		if (deltaT <= 0)
		{
			return;
		}
		update(pos.toArray());
		curState = predict();
		curTimestamp = timestamp;
	}
	
	
	@Override
	public void predict(final long timestamp)
	{
		// deltaT = (timestamp - curTimestamp) / 1e9;
		// if (deltaT < 0)
		// {
		// deltaT = 0;
		// }
		// predState = predict();
		predState = curState;
	}
	
	
	@Override
	public void setControl(final IVector3 control)
	{
	}
	
	
	@Override
	public IVector3 getPos()
	{
		return new Vector3(Arrays.copyOfRange(predState, 0, 3));
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return new Vector3(Arrays.copyOfRange(predState, 3, 6));
	}
	
	
	@Override
	public IVector3 getAcc()
	{
		return new Vector3();
	}
	
	
	@Override
	public IVector3 getCurAcc()
	{
		return new Vector3();
	}
	
	
	@Override
	public IVector3 getCurVel()
	{
		return new Vector3(Arrays.copyOfRange(curState, 3, 6));
	}
	
	
	@Override
	public IVector3 getCurPos()
	{
		return new Vector3(Arrays.copyOfRange(curState, 0, 3));
	}
	
	
	@Override
	public long getCurTimestamp()
	{
		return curTimestamp;
	}
	
}
