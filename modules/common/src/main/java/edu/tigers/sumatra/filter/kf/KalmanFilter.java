/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.filter.kf;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


/**
 * Implementation of a Kalman filter to estimate the state <i>x<sub>k</sub></i>
 * of a discrete-time controlled process that is governed by the linear
 * stochastic difference equation:
 *
 * <pre>
 * <i>x<sub>k</sub></i> = <b>A</b><i>x<sub>k-1</sub></i> + <b>B</b><i>u<sub>k-1</sub></i> + <i>w<sub>k-1</sub></i>
 * </pre>
 *
 * with a measurement <i>x<sub>k</sub></i> that is
 *
 * <pre>
 * <i>z<sub>k</sub></i> = <b>H</b><i>x<sub>k</sub></i> + <i>v<sub>k</sub></i>.
 * </pre>
 * <p>
 * The random variables <i>w<sub>k</sub></i> and <i>v<sub>k</sub></i> represent
 * the process and measurement noise and are assumed to be independent of each
 * other and distributed with normal probability (white noise).
 * Code taken from Apache Commons Math KalmanFilter.
 * 
 * @author AndreR
 */
public class KalmanFilter
{
	/** The internal state estimation vector, equivalent to x hat. */
	protected RealVector			stateEstimation;
	/** The error covariance matrix, equivalent to P. */
	protected RealMatrix			errorCovariance;
	/** Innovation from the last correction step */
	protected RealVector			innovation;
	
	/** A */
	protected final RealMatrix	transitionMatrix;
	/** B */
	protected final RealMatrix	controlMatrix;
	/** Q */
	protected final RealMatrix	processNoiseCovariance;
	/** H */
	protected final RealMatrix	measurementMatrix;
	/** R */
	protected final RealMatrix	measurementNoiseCovariance;
	
	
	protected KalmanFilter(final int numStates, final int numMeasurements, final int numControl)
	{
		stateEstimation = new ArrayRealVector(numStates);
		innovation = new ArrayRealVector(numMeasurements);
		
		transitionMatrix = new Array2DRowRealMatrix(numStates, numStates);
		errorCovariance = new Array2DRowRealMatrix(numStates, numStates);
		processNoiseCovariance = new Array2DRowRealMatrix(numStates, numStates);
		
		controlMatrix = new Array2DRowRealMatrix(numStates, numControl);
		
		measurementMatrix = new Array2DRowRealMatrix(numMeasurements, numStates);
		measurementNoiseCovariance = new Array2DRowRealMatrix(numMeasurements, numMeasurements);
	}
	
	
	protected KalmanFilter(final KalmanFilter orig)
	{
		stateEstimation = orig.stateEstimation.copy();
		innovation = orig.innovation.copy();
		
		transitionMatrix = orig.transitionMatrix.copy();
		errorCovariance = orig.errorCovariance.copy();
		processNoiseCovariance = orig.processNoiseCovariance.copy();
		
		controlMatrix = orig.controlMatrix.copy();
		
		measurementMatrix = orig.measurementMatrix.copy();
		measurementNoiseCovariance = orig.measurementNoiseCovariance.copy();
	}
	
	
	/**
	 * Predict the internal state estimation one time step ahead.
	 * Convenience function if no control input is used.
	 */
	protected void predict()
	{
		predict(null);
	}
	
	
	/**
	 * Predict the internal state estimation one time step ahead.
	 *
	 * @param u the control vector
	 */
	protected void predict(final RealVector u)
	{
		// project the state estimation ahead (a priori state)
		// xHat(k)- = A * xHat(k-1) + B * u(k-1)
		stateEstimation = transitionMatrix.operate(stateEstimation);
		
		// add control input if it is available
		if (u != null)
		{
			stateEstimation = stateEstimation.add(controlMatrix.operate(u));
		}
		
		// project the error covariance ahead
		// P(k)- = A * P(k-1) * A' + Q
		errorCovariance = transitionMatrix.multiply(errorCovariance)
				.multiply(transitionMatrix.transpose())
				.add(processNoiseCovariance);
	}
	
	
	/**
	 * Correct the current state estimate with an actual measurement.
	 *
	 * @param z the measurement vector
	 */
	protected void correct(final RealVector z)
	{
		// S = H * P(k) * H' + R
		RealMatrix s = measurementMatrix.multiply(errorCovariance)
				.multiply(measurementMatrix.transpose())
				.add(measurementNoiseCovariance);
		
		// Inn = z(k) - H * xHat(k)-
		innovation = z.subtract(measurementMatrix.operate(stateEstimation));
		
		// calculate gain matrix
		// K(k) = P(k)- * H' * (H * P(k)- * H' + R)^-1
		// K(k) = P(k)- * H' * S^-1
		
		// instead of calculating the inverse of S we can rearrange the formula,
		// and then solve the linear equation A x X = B with A = S', X = K' and B = (H * P)'
		
		// K(k) * S = P(k)- * H'
		// S' * K(k)' = H * P(k)-'
		RealMatrix kalmanGain = new QRDecomposition(s).getSolver()
				.solve(measurementMatrix.multiply(errorCovariance.transpose()))
				.transpose();
		
		// update estimate with measurement z(k)
		// xHat(k) = xHat(k)- + K * Inn
		stateEstimation = stateEstimation.add(kalmanGain.operate(innovation));
		
		// update covariance of prediction error
		// P(k) = (I - K * H) * P(k)-
		RealMatrix identity = MatrixUtils.createRealIdentityMatrix(kalmanGain.getRowDimension());
		errorCovariance = identity.subtract(kalmanGain.multiply(measurementMatrix)).multiply(errorCovariance);
	}
	
	
	public RealVector getStateEstimate()
	{
		return stateEstimation;
	}
	
	
	public RealMatrix getCovarianceMatrix()
	{
		return errorCovariance;
	}
	
	
	public RealVector getInnovation()
	{
		return innovation;
	}
}
