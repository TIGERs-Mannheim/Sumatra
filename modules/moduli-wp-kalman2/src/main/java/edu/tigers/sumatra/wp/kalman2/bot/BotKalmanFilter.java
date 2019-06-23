/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2.bot;

import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotKalmanFilter implements ProcessModel, MeasurementModel, IKalmanFilter
{
	private final int				num_derivatives	= 3;
	private final int				num_controls		= 1;
	private final int				num_states;
										
										
	/** state transition */
	private final RealMatrix	a;
	/** control input */
	private final RealMatrix	b;
	/** measurement */
	private final RealMatrix	h;
	/** process noise cov */
	private final RealMatrix	q;
	/** measurement noise cov */
	private final RealMatrix	r;
										
	/** error cov */
	private RealMatrix			p;
										
	private RealVector			state;
	private RealVector			statePrediction;
	private RealVector			u						= null;
	private long					curTimestamp;
										
										
	/**
	 * @param num_states
	 */
	public BotKalmanFilter(final int num_states)
	{
		this.num_states = num_states;
		a = new Array2DRowRealMatrix(num_states * num_derivatives, num_states * num_derivatives);
		b = new Array2DRowRealMatrix(num_states * num_derivatives, num_states * num_controls);
		h = new Array2DRowRealMatrix(num_states, num_states * num_derivatives);
		q = new Array2DRowRealMatrix(num_states * num_derivatives, num_states * num_derivatives);
		r = new Array2DRowRealMatrix(num_states, num_states);
		p = new Array2DRowRealMatrix(num_states * num_derivatives, num_states * num_derivatives);
	}
	
	
	/**
	 * @param initState
	 * @param timestamp
	 */
	public void init(final RealVector initState, final long timestamp)
	{
		assert initState.getDimension() >= num_states;
		assert (initState.getDimension() % num_derivatives) == 0;
		state = new ArrayRealVector(num_states * num_derivatives);
		state.setSubVector(0, initState);
		statePrediction = state;
		curTimestamp = timestamp;
		
		updateMatrices(0);
	}
	
	
	/**
	 * @param covState
	 * @param covMeas
	 */
	public void setUncertainties(final double[][] covState,
			final double[] covMeas)
	{
		for (int i = 0; i < num_states; i++)
		{
			r.setEntry(i, i, covMeas[i]);
		}
		
		
		for (int d = 0; d < num_states; d++)
		{
			r.setEntry(d, d, covMeas[d]);
			
			for (int j = 0; j < num_derivatives; j++)
			{
				int m = d + (j * num_states);
				q.setEntry(m, m, covState[d][j]);
				
				p.setEntry(m, m, 1);
			}
		}
	}
	
	
	private void updateMatrices(final double dt)
	{
		for (int i = 0; i < (num_states * num_derivatives); i++)
		{
			a.setEntry(i, i, 1);
		}
		for (int i = 0; i < (num_states * (num_derivatives - 1)); i++)
		{
			a.setEntry(i, i + num_states, dt);
		}
		for (int i = 0; i < (num_states * (num_derivatives - 2)); i++)
		{
			a.setEntry(i, i + (num_states * 2), dt);
		}
		
		for (int i = 0; i < num_states; i++)
		{
			b.setEntry(i + (2 * num_states), i, 1);
		}
		
		for (int i = 0; i < num_states; i++)
		{
			h.setEntry(i, i, 1);
		}
	}
	
	
	/**
	 * @param meas
	 * @param timestamp
	 */
	@Override
	public void correct(final RealVector meas, final long timestamp)
	{
		double dt = (timestamp - curTimestamp) / 1e9;
		if (dt < 0)
		{
			return;
		}
		
		updateMatrices(dt);
		KalmanFilter kf = new KalmanFilter(this, this);
		kf.correct(meas);
		kf.predict(u);
		state = kf.getStateEstimationVector();
		p = kf.getErrorCovarianceMatrix();
		curTimestamp = timestamp;
	}
	
	
	/**
	 * @param timestamp
	 */
	@Override
	public void predict(final long timestamp)
	{
		double dt = (timestamp - curTimestamp) / 1e9;
		if (dt <= 0)
		{
			statePrediction = state;
			return;
		}
		if (dt > 1)
		{
			throw new IllegalArgumentException("Time step too large: " + dt);
		}
		
		double ddt = 0.002;
		updateMatrices(ddt);
		KalmanFilter kf = new KalmanFilter(this, this);
		while (dt > ddt)
		{
			kf.predict(u);
			dt -= ddt;
		}
		updateMatrices(dt);
		kf.predict(u);
		statePrediction = kf.getStateEstimationVector();
	}
	
	
	@Override
	public RealMatrix getMeasurementMatrix()
	{
		return h;
	}
	
	
	@Override
	public RealMatrix getMeasurementNoise()
	{
		return r;
	}
	
	
	@Override
	public RealMatrix getStateTransitionMatrix()
	{
		return a;
	}
	
	
	@Override
	public RealMatrix getControlMatrix()
	{
		return b;
	}
	
	
	@Override
	public RealMatrix getProcessNoise()
	{
		return q;
	}
	
	
	@Override
	public RealVector getInitialStateEstimate()
	{
		return state;
	}
	
	
	@Override
	public RealMatrix getInitialErrorCovariance()
	{
		return p;
	}
	
	
	/**
	 * @return the state
	 */
	@Override
	public RealVector getState()
	{
		return state;
	}
	
	
	/**
	 * @return the statePrediction
	 */
	public RealVector getStatePrediction()
	{
		return statePrediction;
	}
	
	
	/**
	 * @param u the u to set
	 */
	public void setU(final RealVector u)
	{
		this.u = u;
	}
	
}
