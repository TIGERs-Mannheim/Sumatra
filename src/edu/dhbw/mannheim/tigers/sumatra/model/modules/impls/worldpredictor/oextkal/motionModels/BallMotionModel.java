package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels;

import java.util.Random;

import org.apache.commons.lang.NotImplementedException;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBall;


/**
 */
public class BallMotionModel implements IMotionModel
{
	// :::>>>
	/** m/s */
	private static final double		baseMinVelocity			= 0.01;
	/** m/s */
	private static final double		baseMaxRollingVelocity	= 10.0;
	/** m/s^2 */
	private static final double		baseAMax						= 0.245;
	
	/** m */
	private static final double		baseStDevPosition			= 0.001;
	/** m/s */
	private static final double		baseStDevVelocity			= 0.5;
	/** m/s^2 */
	private static final double		baseStDevAcceleration	= 2.0;
	
	/** m/s^2 */
	private static final double		baseGravity					= 9.81;
	private static final double		rollingFriction			= 0.025;
	private static final double		slidingFriction			= 0.010;
	
	// Particle Filter
	/** m */
	private static final double		baseStDevSamplePosition	= 0.008;
	/** m/s */
	private static final double		baseStDevSampleVel		= 0.1;
	// :::<<<
	private final GaussianGenerator	genPos;
	private final GaussianGenerator	genVel;
	
	private final double					minVelocity;
	private final double					maxRollingVelocity;
	private final double					aMax;
	
	private final double					varPosition;
	private final double					varVelocity;
	private final double					varAcceleration;
	
	private final double					aRollingFriction;
	private final double					aSlidingFriction;
	private final double					aFall;
	
	
	// --------------------------------------------------------------------------
	// --- constructor ----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BallMotionModel()
	{
		minVelocity = baseMinVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		maxRollingVelocity = baseMaxRollingVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		aMax = baseAMax * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		
		varPosition = Math.pow(baseStDevPosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varVelocity = Math.pow(baseStDevVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varAcceleration = Math.pow(baseStDevAcceleration * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A, 2);
		
		double gravity = baseGravity * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		aRollingFriction = rollingFriction * gravity;
		aSlidingFriction = slidingFriction * gravity;
		aFall = gravity;
		
		double stDevSamplePosition = baseStDevSamplePosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT;
		double stDevSampleVel = baseStDevSampleVel * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		Random rng = new MersenneTwisterRNG();
		genPos = new GaussianGenerator(0, stDevSamplePosition, rng);
		genVel = new GaussianGenerator(0, stDevSampleVel, rng);
	}
	
	
	// --------------------------------------------------------------------------
	// --- interface methods ----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Matrix dynamics(Matrix state, Matrix control, double dt)
	{
		double x = state.get(0, 0);
		double y = state.get(1, 0);
		double z = state.get(2, 0);
		double vx = state.get(3, 0);
		double vy = state.get(4, 0);
		double vz = state.get(5, 0);
		double ax = state.get(6, 0);
		double ay = state.get(7, 0);
		double az = state.get(8, 0);
		
		// velocity
		final double v = Math.sqrt((vx * vx) + (vy * vy));
		
		if ((z > 0.0) || (vz > 0.0) || (az > 0.0))
		{
			// flying ball
			ax = 0.0;
			ay = 0.0;
			az = aFall;
		} else
		{
			// ball on field
			if (v > minVelocity)
			{
				if (v > maxRollingVelocity)
				{
					ax = aSlidingFriction * Math.signum(vx);
					ay = aSlidingFriction * Math.signum(vx);
					az = 0.0;
				} else
				{
					ax = aRollingFriction * Math.signum(vx);
					ay = aRollingFriction * Math.signum(vx);
					az = 0.0;
				}
			} else
			{
				vx = 0.0;
				vy = 0.0;
				vz = 0.0;
				ax = 0.0;
				ay = 0.0;
				az = 0.0;
			}
		}
		
		// update position
		x = (x + (vx * dt)) - (0.5 * dt * dt * ax);
		y = (y + (vy * dt)) - (0.5 * dt * dt * ay);
		z = (z + (vz * dt)) - (0.5 * dt * dt * az);
		
		if (z <= 0.0)
		{
			z = 0.0;
			vz = 0.0;
			az = 0.0;
		}
		
		// calculate ball's velocity from current acceleration
		vx = vx - (dt * ax);
		vy = vy - (dt * ay);
		vz = vz - (dt * az);
		
		
		final Matrix newState = new Matrix(9, 1);
		newState.set(0, 0, x);
		newState.set(1, 0, y);
		newState.set(2, 0, z);
		newState.set(3, 0, vx);
		newState.set(4, 0, vy);
		newState.set(5, 0, vz);
		newState.set(6, 0, ax);
		newState.set(7, 0, ay);
		newState.set(8, 0, az);
		return newState;
	}
	
	
	@Override
	public Matrix sample(Matrix state, Matrix control)
	{
		final double x = state.get(0, 0);
		final double y = state.get(1, 0);
		final double z = state.get(2, 0);
		final double vx = state.get(3, 0);
		final double vy = state.get(4, 0);
		final double vz = state.get(5, 0);
		final double ax = state.get(6, 0);
		final double ay = state.get(7, 0);
		final double az = state.get(8, 0);
		
		final Matrix newState = new Matrix(9, 1);
		newState.set(0, 0, (x + genPos.nextValue()));
		newState.set(1, 0, (y + genPos.nextValue()));
		// (z+Math.abs(genPos.nextValue()/2)));
		newState.set(2, 0, z);
		newState.set(3, 0, (vx + genVel.nextValue()));
		newState.set(4, 0, (vy + genVel.nextValue()));
		// (vz+genVel.nextValue()));
		newState.set(5, 0, vz);
		newState.set(6, 0, ax);
		newState.set(7, 0, ay);
		newState.set(8, 0, az);
		return newState;
	}
	
	
	@Override
	public double transitionProbability(Matrix stateNew, Matrix stateOld, Matrix control)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public double measurementProbability(Matrix state, Matrix measurement, double dt)
	{
		final Matrix p = getDynamicsCovariance(state, 1.0);
		
		// calculate matrices for comparing of possible measurement with predicted state
		final Matrix hState = measurementDynamics(state);
		final Matrix h = getMeasurementJacobianWRTstate(state);
		final Matrix c = h.times(p).times(h.transpose());
		final Matrix d = measurement.minus(hState);
		
		// determine likelihood of measurement
		double likelihood = 1.0;
		for (int i = 0; i < d.getRowDimension(); i++)
		{
			final double factor = Math.exp(-(d.get(i, 0) * d.get(i, 0)) / (2 * c.get(i, i)));
			likelihood *= factor;
		}
		return likelihood;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTstate(Matrix state, double dt)
	{
		final Matrix a = Matrix.identity(9, 9);
		a.set(0, 0, 1.0);
		a.set(0, 3, dt);
		a.set(0, 6, 0.5 * dt * dt);
		a.set(1, 1, 1.0);
		a.set(1, 4, dt);
		a.set(1, 7, 0.5 * dt * dt);
		a.set(2, 2, 1.0);
		a.set(2, 5, dt);
		a.set(2, 8, 0.5 * dt * dt);
		a.set(3, 3, 1.0);
		a.set(3, 6, dt);
		a.set(4, 4, 1.0);
		a.set(4, 7, dt);
		a.set(5, 5, 1.0);
		a.set(5, 8, dt);
		return a;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTnoise(Matrix state, double dt)
	{
		return Matrix.identity(9, 9);
	}
	
	
	@Override
	public Matrix getDynamicsCovariance(Matrix state, double dt)
	{
		final Matrix q = new Matrix(9, 9);
		// calculate base noise covariances on distance to nearest robot
		q.set(0, 0, Math.pow(aMax * (dt * dt), 2));
		q.set(1, 1, Math.pow(aMax * (dt * dt), 2));
		q.set(2, 2, Math.pow(aMax * (dt * dt), 2));
		q.set(3, 3, Math.pow(aMax * dt, 2));
		q.set(4, 4, Math.pow(aMax * dt, 2));
		q.set(5, 5, Math.pow(aMax * dt, 2));
		q.set(6, 6, Math.pow(1.0, 2));
		q.set(7, 7, Math.pow(1.0, 2));
		q.set(8, 8, Math.pow(1.0, 2));
		return q;
	}
	
	
	@Override
	public AMotionResult generateMotionResult(int id, Matrix state, boolean onCam)
	{
		final double x = state.get(0, 0);
		final double y = state.get(1, 0);
		final double z = state.get(2, 0);
		final double vx = state.get(3, 0);
		final double vy = state.get(4, 0);
		final double vz = state.get(5, 0);
		final double ax = state.get(6, 0);
		final double ay = state.get(7, 0);
		final double az = state.get(8, 0);
		final double confidence = 1.0;
		return new BallMotionResult(x, y, z, vx, vy, vz, ax, ay, az, confidence, onCam);
	}
	
	
	@Override
	public Matrix generateMeasurementMatrix(AWPCamObject observation, Matrix state)
	{
		final WPCamBall obs = (WPCamBall) observation;
		final Matrix m = new Matrix(3, 1);
		m.set(0, 0, obs.x);
		m.set(1, 0, obs.y);
		m.set(2, 0, obs.z);
		return m;
	}
	
	
	@Override
	public Matrix generateStateMatrix(Matrix measurement, Matrix control)
	{
		// no control for ball
		final Matrix s = new Matrix(9, 1);
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		s.set(2, 0, measurement.get(2, 0));
		return s;
	}
	
	
	@Override
	public Matrix generateControlMatrix(IControl control, Matrix state)
	{
		// no control for ball
		return null;
	}
	
	
	@Override
	public Matrix generateCovarianceMatrix(Matrix state)
	{
		final Matrix p = new Matrix(9, 9);
		p.set(0, 0, varPosition);
		p.set(1, 1, varPosition);
		p.set(2, 2, varPosition);
		p.set(3, 3, varVelocity);
		p.set(4, 4, varVelocity);
		p.set(5, 5, varVelocity);
		p.set(6, 6, varAcceleration);
		p.set(7, 7, varAcceleration);
		p.set(8, 8, varAcceleration);
		return p;
	}
	
	
	@Override
	public int extraxtObjectID(AWPCamObject observation)
	{
		// a ball has no id, so we set it to 0
		return 0;
	}
	
	
	@Override
	public Matrix measurementDynamics(Matrix state)
	{
		final Matrix m = new Matrix(3, 1);
		m.set(0, 0, state.get(0, 0));
		m.set(1, 0, state.get(1, 0));
		m.set(2, 0, state.get(2, 0));
		return m;
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTstate(Matrix state)
	{
		return Matrix.identity(3, 9);
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTnoise(Matrix state)
	{
		return Matrix.identity(3, 3);
	}
	
	
	@Override
	public Matrix getMeasurementCovariance(Matrix measurement)
	{
		return Matrix.identity(3, 3).times(varPosition);
	}
	
	
	@Override
	public Matrix updateStateOnNewControl(IControl control, Matrix state)
	{
		// nothing to do here because the ball has no control
		return state;
	}
	
	
	@Override
	public Matrix updateCovarianceOnNewControl(IControl control, Matrix covariance)
	{
		// nothing to do here because the ball has no control
		return covariance;
	}
	
	
	@Override
	public Matrix getStateOnNoObservation(Matrix state)
	{
		state.set(2, 0, 0.0);
		state.set(3, 0, 0.0);
		state.set(4, 0, 0.0);
		state.set(5, 0, 0.0);
		state.set(6, 0, 0.0);
		state.set(7, 0, 0.0);
		state.set(8, 0, 0.0);
		return state;
	}
	
	
	@Override
	public Matrix getCovarianceOnNoObservation(Matrix covariance)
	{
		final double improbable = 1e10;
		final Matrix covar = new Matrix(9, 9);
		for (int i = 0; i < covar.getRowDimension(); i++)
		{
			for (int j = 0; j < covar.getColumnDimension(); j++)
			{
				covar.set(i, j, improbable);
			}
		}
		return covar;
	}
	
	
	@Override
	public Matrix getControlOnNoObservation(Matrix control)
	{
		return generateControlMatrix(null, null);
	}
	
}
