/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.filter.tracking;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import edu.tigers.sumatra.filter.kf.KalmanFilter;


/**
 * Simple tracking filter based on a linear Kalman filter.
 * State vector includes position and velocity.
 * Only position can be measured.
 * 
 * @author AndreR
 */
public class TrackingFilterPosVel1D extends KalmanFilter
{
	private double	modelError;
	
	private long	lastTimestamp;
	
	
	/**
	 * Create tracking filter.
	 * 
	 * @param initialPos
	 * @param covariance initial covariance of all states
	 * @param modelErr model error
	 * @param measErr measurement error
	 * @param timestamp initial timestamp in nanoseconds
	 */
	public TrackingFilterPosVel1D(final double initialPos, final double covariance, final double modelErr,
			final double measErr, final long timestamp)
	{
		super(2, 1, 1);
		
		stateEstimation.setEntry(0, initialPos);
		errorCovariance = MatrixUtils.createRealIdentityMatrix(2).scalarMultiply(covariance);
		
		measurementMatrix.setEntry(0, 0, 1);
		
		setMeasurementError(measErr);
		setModelError(modelErr);
		lastTimestamp = timestamp;
	}
	
	
	/**
	 * Create tracking filter.
	 * 
	 * @param initialState
	 * @param covariance initial covariance of all states
	 * @param modelErr model error
	 * @param measErr measurement error
	 * @param timestamp initial timestamp in nanoseconds
	 */
	public TrackingFilterPosVel1D(final RealVector initialState, final double covariance, final double modelErr,
			final double measErr, final long timestamp)
	{
		super(2, 1, 1);
		
		Validate.isTrue(initialState.getDimension() == 2);
		
		stateEstimation = initialState.copy();
		errorCovariance = MatrixUtils.createRealIdentityMatrix(2).scalarMultiply(covariance);
		
		measurementMatrix.setEntry(0, 0, 1);
		
		setMeasurementError(measErr);
		setModelError(modelErr);
		lastTimestamp = timestamp;
	}
	
	
	/**
	 * Create tracking filter.
	 * 
	 * @param initialState
	 * @param initialCovariance
	 * @param modelErr model error
	 * @param measErr measurement error
	 * @param timestamp initial timestamp in nanoseconds
	 */
	public TrackingFilterPosVel1D(final RealVector initialState, final RealMatrix initialCovariance,
			final double modelErr, final double measErr, final long timestamp)
	{
		super(2, 1, 1);
		
		Validate.isTrue(initialState.getDimension() == 2, "State vector size must be 2");
		Validate.isTrue(initialCovariance.isSquare(), "Covariance matrix must be square");
		Validate.isTrue(initialCovariance.getRowDimension() == 2, "Covariance matrix size must be 2x2");
		
		stateEstimation = initialState.copy();
		errorCovariance = initialCovariance.copy();
		
		measurementMatrix.setEntry(0, 0, 1);
		
		setMeasurementError(measErr);
		setModelError(modelErr);
		lastTimestamp = timestamp;
	}
	
	
	/**
	 * Create tracking filter. Deep copy constructor.
	 * 
	 * @param orig
	 * @param timestamp
	 */
	public TrackingFilterPosVel1D(final TrackingFilterPosVel1D orig, final long timestamp)
	{
		super(orig);
		
		setModelError(orig.modelError);
		lastTimestamp = timestamp;
	}
	
	
	public void setMeasurementError(final double error)
	{
		measurementNoiseCovariance.setEntry(0, 0, error);
	}
	
	
	public void setModelError(final double error)
	{
		modelError = error;
	}
	
	
	/**
	 * Predict the filter estimate to a given timestamp when no new measurement is available.
	 * No correction will be performed.
	 * 
	 * @param timestamp time in nanoseconds
	 */
	public void predict(final long timestamp)
	{
		double dt = (timestamp - lastTimestamp) * 1e-9;
		if (dt <= 0)
		{
			return;
		}
		
		lastTimestamp = timestamp;
		
		updateMatrices(dt);
		
		predict();
	}
	
	
	/**
	 * Correct filter estimate with a new measurement.
	 * 
	 * @param position
	 */
	public void correct(final double position)
	{
		correct(MatrixUtils.createRealVector(new double[] { position }));
	}
	
	
	public long getLastTimestamp()
	{
		return lastTimestamp;
	}
	
	
	public double getPositionEstimate()
	{
		return stateEstimation.getEntry(0);
	}
	
	
	/**
	 * Get position estimate at a specific time.
	 * Uses simple extrapolation with constant velocity model.
	 * 
	 * @param timestamp time in nanoseconds
	 * @return
	 */
	public double getPositionEstimate(final long timestamp)
	{
		double p = getPositionEstimate();
		double v = getVelocityEstimate();
		double dt = (timestamp - lastTimestamp) * 1e-9;
		
		return p + (dt * v);
	}
	
	
	public double getPositionUncertainty()
	{
		return Math.sqrt(errorCovariance.getEntry(0, 0));
	}
	
	
	public double getVelocityUncertainty()
	{
		return Math.sqrt(errorCovariance.getEntry(1, 1));
	}
	
	
	public double getPositionInnovation()
	{
		return innovation.getEntry(0);
	}
	
	
	public double getVelocityEstimate()
	{
		return stateEstimation.getEntry(1);
	}
	
	
	private void updateMatrices(final double dt)
	{
		transitionMatrix.setEntry(0, 0, 1);
		transitionMatrix.setEntry(0, 1, dt);
		transitionMatrix.setEntry(1, 0, 0);
		transitionMatrix.setEntry(1, 1, 1);
		
		// optimal process noise error if we assume white noise on acceleration with zero mean
		double sigma = Math.sqrt((3.0 * modelError) / dt) / dt;
		double dt3 = (1.0 / 3.0) * dt * dt * dt * sigma * sigma;
		double dt2 = (1.0 / 2.0) * dt * dt * sigma * sigma;
		processNoiseCovariance.setEntry(0, 0, dt3);
		processNoiseCovariance.setEntry(0, 1, dt2);
		processNoiseCovariance.setEntry(1, 0, dt2);
		processNoiseCovariance.setEntry(1, 1, dt * sigma * sigma);
	}
}
