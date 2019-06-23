/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2.bot;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotKalmanFilterSingle implements IBotFilter
{
	private final BotKalmanFilter[]	filters	= new BotKalmanFilter[3];
	private long							curTimestamp;
												
												
	/**
	 * @param initState
	 * @param timestamp
	 */
	public BotKalmanFilterSingle(final IVector3 initState, final long timestamp)
	{
		double covPos = 0.01;
		double covVel = 1000;
		double covAcc = 100;
		double covVision = 0.1;
		
		double covOri = 1;
		double covAvel = 500;
		double covAacc = 100;
		double covOriVision = 10;
		
		final double[][] covState = new double[3][];
		
		covState[0] = new double[] { covPos, covVel, covAcc };
		covState[1] = new double[] { covPos, covVel, covAcc };
		covState[2] = new double[] { covOri, covAvel, covAacc };
		final double[] covMeas = new double[] { covVision, covVision, covOriVision };
		
		for (int i = 0; i < 3; i++)
		{
			filters[i] = new BotKalmanFilter(1);
			RealVector state = new ArrayRealVector(3);
			state.setEntry(0, initState.get(i));
			filters[i].init(state, timestamp);
			filters[i].setUncertainties(new double[][] { covState[i] }, new double[] { covMeas[i] });
		}
		curTimestamp = timestamp;
	}
	
	
	@Override
	public void update(final IVector3 pos, final long timestamp)
	{
		for (int i = 0; i < 3; i++)
		{
			RealVector meas = new ArrayRealVector(1);
			meas.setEntry(0, pos.get(i));
			filters[i].correct(meas, timestamp);
		}
		curTimestamp = timestamp;
	}
	
	
	@Override
	public void predict(final long timestamp)
	{
		for (int i = 0; i < 3; i++)
		{
			filters[i].predict(timestamp);
		}
	}
	
	
	@Override
	public void setControl(final IVector3 acc)
	{
		for (int i = 0; i < 3; i++)
		{
			RealVector u = new ArrayRealVector(new double[] { acc.get(i) });
			filters[i].setU(u);
		}
	}
	
	
	@Override
	public IVector3 getPos()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getStatePrediction().getEntry(0));
		}
		return p;
	}
	
	
	@Override
	public IVector3 getVel()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getStatePrediction().getEntry(1));
		}
		return p;
	}
	
	
	@Override
	public IVector3 getAcc()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getStatePrediction().getEntry(2));
		}
		return p;
	}
	
	
	@Override
	public IVector3 getCurPos()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getState().getEntry(0));
		}
		return p;
	}
	
	
	@Override
	public IVector3 getCurVel()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getState().getEntry(1));
		}
		return p;
	}
	
	
	@Override
	public IVector3 getCurAcc()
	{
		Vector3 p = new Vector3();
		for (int i = 0; i < 3; i++)
		{
			p.set(i, filters[i].getState().getEntry(2));
		}
		return p;
	}
	
	
	/**
	 * @return the curTimestamp
	 */
	@Override
	public long getCurTimestamp()
	{
		return curTimestamp;
	}
	
}
