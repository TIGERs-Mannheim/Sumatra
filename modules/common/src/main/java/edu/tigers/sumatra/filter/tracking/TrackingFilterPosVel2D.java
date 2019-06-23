/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.filter.tracking;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import edu.tigers.sumatra.filter.kf.KalmanFilter;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Simple tracking filter based on a linear Kalman filter.
 * State vector includes position and velocity.
 * Only position can be measured.
 * 
 * @author AndreR
 */
public class TrackingFilterPosVel2D extends KalmanFilter
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
	public TrackingFilterPosVel2D(final IVector2 initialPos, final double covariance, final double modelErr,
			final double measErr, final long timestamp)
	{
		super(4, 2, 1);
		
		stateEstimation.setSubVector(0, initialPos.toRealVector());
		errorCovariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(covariance);
		
		measurementMatrix.setEntry(0, 0, 1);
		measurementMatrix.setEntry(1, 1, 1);
		
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
	public TrackingFilterPosVel2D(final RealVector initialState, final double covariance, final double modelErr,
			final double measErr, final long timestamp)
	{
		super(4, 2, 1);
		
		Validate.isTrue(initialState.getDimension() == 4);
		
		stateEstimation = initialState.copy();
		errorCovariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(covariance);
		
		measurementMatrix.setEntry(0, 0, 1);
		measurementMatrix.setEntry(1, 1, 1);
		
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
	public TrackingFilterPosVel2D(final RealVector initialState, final RealMatrix initialCovariance,
			final double modelErr, final double measErr, final long timestamp)
	{
		super(4, 2, 1);
		
		Validate.isTrue(initialState.getDimension() == 4, "State vector size must be 4");
		Validate.isTrue(initialCovariance.isSquare(), "Covariance matrix must be square");
		Validate.isTrue(initialCovariance.getRowDimension() == 4, "Covariance matrix size must be 4x4");
		
		stateEstimation = initialState.copy();
		errorCovariance = initialCovariance.copy();
		
		measurementMatrix.setEntry(0, 0, 1);
		measurementMatrix.setEntry(1, 1, 1);
		
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
	public TrackingFilterPosVel2D(final TrackingFilterPosVel2D orig, final long timestamp)
	{
		super(orig);
		
		setModelError(orig.modelError);
		lastTimestamp = timestamp;
	}
	
	
	public void setMeasurementError(final double error)
	{
		measurementNoiseCovariance.setEntry(0, 0, error);
		measurementNoiseCovariance.setEntry(1, 1, error);
	}
	
	
	public void setModelError(final double error)
	{
		modelError = error;
	}
	
	
	public void setPosition(final IVector2 pos)
	{
		stateEstimation.setEntry(0, pos.x());
		stateEstimation.setEntry(1, pos.y());
	}
	
	
	public void setVelocity(final IVector2 vel)
	{
		stateEstimation.setEntry(2, vel.x());
		stateEstimation.setEntry(3, vel.y());
	}
	
	
	/**
	 * Reset internal state covariance to a specific value.
	 * 
	 * @param covariance
	 */
	public void resetCovariance(final double covariance)
	{
		errorCovariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(covariance);
		errorCovariance.setEntry(2, 2, covariance * covariance);
		errorCovariance.setEntry(3, 3, covariance * covariance);
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
	public void correct(final IVector2 position)
	{
		correct(position.toRealVector());
	}
	
	
	public long getLastTimestamp()
	{
		return lastTimestamp;
	}
	
	
	public IVector2 getPositionEstimate()
	{
		return Vector2.fromReal(stateEstimation.getSubVector(0, 2));
	}
	
	
	/**
	 * Get position estimate at a specific time.
	 * Uses simple extrapolation with constant velocity model.
	 * 
	 * @param timestamp time in nanoseconds
	 * @return
	 */
	public IVector2 getPositionEstimate(final long timestamp)
	{
		IVector2 p = getPositionEstimate();
		IVector2 v = getVelocityEstimate();
		double dt = (timestamp - lastTimestamp) * 1e-9;
		
		return p.addNew(v.multiplyNew(dt));
	}
	
	
	/**
	 * Optimal process noise error if we assume white noise on acceleration with zero mean.
	 *
	 * @param m 4x4 matrix
	 * @param dt
	 * @param error
	 */
	public static void getOptimalProcessNoise(final RealMatrix m, final double dt, final double error)
	{
		double sigma = SumatraMath.sqrt((3.0 * error) / dt) / dt;
		double dt3 = (1.0 / 3.0) * dt * dt * dt * sigma * sigma;
		double dt2 = (1.0 / 2.0) * dt * dt * sigma * sigma;
		double dt1 = dt * sigma * sigma;
		m.setEntry(0, 0, dt3);
		m.setEntry(0, 2, dt2);
		
		m.setEntry(1, 1, dt3);
		m.setEntry(1, 3, dt2);
		
		m.setEntry(2, 0, dt2);
		m.setEntry(2, 2, dt1);
		
		m.setEntry(3, 1, dt2);
		m.setEntry(3, 3, dt1);
	}
	
	
	public IVector2 getPositionUncertainty()
	{
		return Vector2.fromXY(SumatraMath.sqrt(errorCovariance.getEntry(0, 0)),
				SumatraMath.sqrt(errorCovariance.getEntry(1, 1)));
	}
	
	
	public IVector2 getPositionInnovation()
	{
		return Vector2.fromReal(innovation);
	}
	
	
	public IVector2 getVelocityEstimate()
	{
		return Vector2.fromReal(stateEstimation.getSubVector(2, 2));
	}
	
	
	private void updateMatrices(final double dt)
	{
		transitionMatrix.setEntry(0, 0, 1);
		transitionMatrix.setEntry(0, 2, dt);
		
		transitionMatrix.setEntry(1, 1, 1);
		transitionMatrix.setEntry(1, 3, dt);
		
		transitionMatrix.setEntry(2, 2, 1);
		
		transitionMatrix.setEntry(3, 3, 1);
		
		getOptimalProcessNoise(processNoiseCovariance, dt, modelError);
	}
	
	
	public IVector2 getVelocityUncertainty()
	{
		return Vector2.fromXY(SumatraMath.sqrt(errorCovariance.getEntry(2, 2)),
				SumatraMath.sqrt(errorCovariance.getEntry(3, 3)));
	}
}
