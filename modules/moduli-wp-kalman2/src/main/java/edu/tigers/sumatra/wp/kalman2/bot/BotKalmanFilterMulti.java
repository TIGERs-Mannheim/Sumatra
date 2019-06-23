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
public class BotKalmanFilterMulti implements IBotFilter
{
	
	private final BotKalmanFilter	filter;
	private long						curTimestamp;
											
											
	/**
	 * @param initState
	 * @param timestamp
	 */
	public BotKalmanFilterMulti(final IVector3 initState, final long timestamp)
	{
		filter = new BotKalmanFilter(3);
		RealVector state = new ArrayRealVector(9);
		state.setEntry(0, initState.get(0));
		state.setEntry(1, initState.get(1));
		state.setEntry(2, initState.get(2));
		filter.init(state, timestamp);
		
		double covPos = 0.01;
		double covVel = 1000;
		double covAcc = 100;
		double covVision = 0.1;
		
		double covOri = 1;
		double covAvel = 1;
		double covAacc = 1;
		double covOriVision = 1;
		
		final double[][] covState = new double[3][];
		
		covState[0] = new double[] { covPos, covVel, covAcc };
		covState[1] = new double[] { covPos, covVel, covAcc };
		covState[2] = new double[] { covOri, covAvel, covAacc };
		final double[] covMeas = new double[] { covVision, covVision, covOriVision };
		
		filter.setUncertainties(covState, covMeas);
		curTimestamp = timestamp;
	}
	
	
	@Override
	public void update(final IVector3 pos, final long timestamp)
	{
		RealVector meas = new ArrayRealVector(pos.toArray());
		filter.correct(meas, timestamp);
		curTimestamp = timestamp;
	}
	
	
	@Override
	public void predict(final long timestamp)
	{
		filter.predict(timestamp);
	}
	
	
	@Override
	public void setControl(final IVector3 control)
	{
		RealVector u = new ArrayRealVector(control.toArray());
		filter.setU(u);
	}
	
	
	@Override
	public IVector3 getPos()
	{
		Vector3 p = new Vector3(filter.getStatePrediction().getSubVector(0, 3).toArray());
		return p;
	}
	
	
	@Override
	public IVector3 getVel()
	{
		Vector3 p = new Vector3(filter.getStatePrediction().getSubVector(3, 3).toArray());
		return p;
	}
	
	
	@Override
	public IVector3 getAcc()
	{
		Vector3 p = new Vector3(filter.getStatePrediction().getSubVector(6, 3).toArray());
		return p;
	}
	
	
	@Override
	public IVector3 getCurAcc()
	{
		Vector3 p = new Vector3(filter.getState().getSubVector(6, 3).toArray());
		return p;
	}
	
	
	@Override
	public IVector3 getCurVel()
	{
		Vector3 p = new Vector3(filter.getState().getSubVector(3, 3).toArray());
		return p;
	}
	
	
	@Override
	public IVector3 getCurPos()
	{
		Vector3 p = new Vector3(filter.getState().getSubVector(0, 3).toArray());
		return p;
	}
	
	
	@Override
	public long getCurTimestamp()
	{
		return curTimestamp;
	}
	
}
