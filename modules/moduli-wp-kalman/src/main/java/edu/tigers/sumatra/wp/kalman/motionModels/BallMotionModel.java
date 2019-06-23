package edu.tigers.sumatra.wp.kalman.motionModels;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.functions.EFunction;
import edu.tigers.sumatra.functions.IFunction1D;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.statistics.CollectorVectorAvg;
import edu.tigers.sumatra.wp.ball.BallAction;
import edu.tigers.sumatra.wp.ball.BallCollisionModel;
import edu.tigers.sumatra.wp.ball.BallDynamicsModelSimple;
import edu.tigers.sumatra.wp.ball.BallState;
import edu.tigers.sumatra.wp.ball.IAction;
import edu.tigers.sumatra.wp.ball.IBallCollisionModel;
import edu.tigers.sumatra.wp.ball.IBallDynamicsModel;
import edu.tigers.sumatra.wp.ball.IState;
import edu.tigers.sumatra.wp.ball.collision.ICollisionState;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.kalman.WPConfig;
import edu.tigers.sumatra.wp.kalman.data.AMotionResult;
import edu.tigers.sumatra.wp.kalman.data.AWPCamObject;
import edu.tigers.sumatra.wp.kalman.data.BallMotionResult;
import edu.tigers.sumatra.wp.kalman.data.IControl;
import edu.tigers.sumatra.wp.kalman.data.WPCamBall;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 */
public class BallMotionModel implements IMotionModel
{
	@SuppressWarnings("unused")
	private static final Logger			log							= Logger.getLogger(BallMotionModel.class.getName());
	
	private static final int				STATE_SIZE					= 16;
	private static final int				STATE_DYNAMICS_SIZE		= 9;
	
	/** m/s */
	private static final double			baseMinVelocity			= 0.05;
	/** m/s */
	private static final double			baseMaxRollingVelocity	= 2.0;
	
	/** m */
	private static final double			baseStDevPosition			= 0.01;
	/** m/s */
	private static final double			baseStDevVelocity			= 0.001;
	/** m/s^2 */
	private static final double			baseStDevAcceleration	= 0.001;
	
	private final double						minVelocity;
	private final double						maxRollingVelocity;
	
	private final double						varPosition;
	private final double						varVelocity;
	private final double						varAcceleration;
	
	
	private final IFunction1D				relVelFunc;
	
	private final IBallDynamicsModel		ballDynamicsModel			= new BallDynamicsModelSimple(
			Geometry.getBallModel().getAcc());
	private final IBallCollisionModel	ballCollisionModel		= new BallCollisionModel();
	
	// TODO this should be moved to state somehow. atm, this model is called mulitple times for the time steps into
	// future
	private static final int				VEL_BUFFER_SIZE			= 5;
	private IVector2[]						measuredVelBuffer			= new IVector2[VEL_BUFFER_SIZE];
	private int									measuredVelBufferIdx		= 0;
	
	
	/**
	 */
	public BallMotionModel()
	{
		minVelocity = baseMinVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		maxRollingVelocity = baseMaxRollingVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		varPosition = Math.pow(baseStDevPosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varVelocity = Math.pow(baseStDevVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varAcceleration = Math.pow(baseStDevAcceleration * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A, 2);
		
		// double stDevSamplePosition = baseStDevSamplePosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT;
		// double stDevSampleVel = baseStDevSampleVel * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		// Random rng = new MersenneTwisterRNG();
		// genPos = new GaussianGenerator(0, stDevSamplePosition, rng);
		// genVel = new GaussianGenerator(0, stDevSampleVel, rng);
		
		for (int i = 0; i < VEL_BUFFER_SIZE; i++)
		{
			measuredVelBuffer[i] = AVector2.ZERO_VECTOR;
		}
		
		double vMaxRel = 500;
		relVelFunc = new RelVelFunction(vMaxRel);
	}
	
	
	@Override
	public Matrix dynamics(final Matrix state, final Matrix control, final double dt, final MotionContext context)
	{
		IState preState = getStateFromMatrix(state);
		IVector3 acc = ballCollisionModel.getTorqueAcc(preState, context);
		IAction action = new BallAction(acc);
		IState postState = ballDynamicsModel.dynamics(preState, action, dt, context);
		
		ICollisionState colState = ballCollisionModel.processCollision(preState, postState, dt, context);
		IVector3 impulse = colState.getVel().addNew(ballCollisionModel.getImpulse(colState, context));
		
		Matrix m = getMatrixFromState(state, colState, impulse);
		
		if (colState.getCollision().isPresent())
		{
			m.set(9, 0, 0);
		} else
		{
			// cooldown of confidence
			double confidence = m.get(9, 0);
			confidence = Math.max(0, Math.min(1, confidence + (dt / 0.1)));
			m.set(9, 0, confidence);
		}
		
		return m;
	}
	
	
	private IState getStateFromMatrix(final Matrix state)
	{
		IVector3 pos = new Vector3(state.get(0, 0), state.get(1, 0), state.get(2, 0));
		IVector3 vel = new Vector3(state.get(3, 0), state.get(4, 0), state.get(5, 0)).multiply(1e-3);
		IVector3 acc = new Vector3(state.get(6, 0), state.get(7, 0), state.get(8, 0)).multiply(1e-3);
		IVector3 accTorque = new Vector3(state.get(10, 0), state.get(11, 0), state.get(12, 0)).multiply(1e-3);
		return new BallState(pos, vel, acc, accTorque);
	}
	
	
	private Matrix getMatrixFromState(final Matrix base, final IState state, final IVector3 impulse)
	{
		Matrix m = base.copy();
		m.set(0, 0, state.getPos().x());
		m.set(1, 0, state.getPos().y());
		m.set(2, 0, state.getPos().z());
		m.set(3, 0, state.getVel().x() * 1000);
		m.set(4, 0, state.getVel().y() * 1000);
		m.set(5, 0, state.getVel().z() * 1000);
		m.set(6, 0, state.getAcc().x() * 1000);
		m.set(7, 0, state.getAcc().y() * 1000);
		m.set(8, 0, state.getAcc().z() * 1000);
		m.set(10, 0, state.getAccFromTorque().x() * 1000);
		m.set(11, 0, state.getAccFromTorque().y() * 1000);
		m.set(12, 0, state.getAccFromTorque().z() * 1000);
		m.set(13, 0, impulse.x() * 1000);
		m.set(14, 0, impulse.y() * 1000);
		m.set(15, 0, impulse.z() * 1000);
		return m;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTstate(final Matrix state, final double dt)
	{
		final Matrix a = Matrix.identity(STATE_DYNAMICS_SIZE, STATE_DYNAMICS_SIZE);
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
	public Matrix getDynamicsJacobianWRTnoise(final Matrix state, final double dt)
	{
		final Matrix a = Matrix.identity(STATE_DYNAMICS_SIZE, STATE_DYNAMICS_SIZE);
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
	public Matrix getDynamicsCovariance(final Matrix state, final double dt)
	{
		final Matrix q = new Matrix(STATE_DYNAMICS_SIZE, STATE_DYNAMICS_SIZE);
		double vx = state.get(3, 0); // + state.get(13, 0);
		double vy = state.get(4, 0); // + state.get(14, 0);
		
		// sigma: max change within one dt,
		// 2*sigma=95% range in data, so divide by 2 below
		
		
		double vel = Math.sqrt((vx * vx) + (vy * vy));
		double relVel = relVelFunc.eval(vel);
		double confidence = state.get(9, 0);
		double collisionInfluence = 1 - confidence;
		
		double velError = Math
				.abs(vel - (Arrays.stream(measuredVelBuffer).collect(new CollectorVectorAvg(2)).getLength()));
		
		double velTol = 800;
		if (velError > velTol)
		{
			collisionInfluence += (velError - velTol) / 3000;
		}
		collisionInfluence = Math.min(1, collisionInfluence);
		
		// at max vel of 8m/2, position can change by 8m in one second
		double pSigmaDef = 5;
		double pSigmaMinWidth = 0.01;
		pSigmaMinWidth += collisionInfluence * (pSigmaDef - pSigmaMinWidth);
		double pSigmaWidth = pSigmaMinWidth + ((1 - relVel) * (pSigmaDef - pSigmaMinWidth));
		double pSigmaLength = pSigmaDef;
		Matrix pCov = createCovarianceMatrix(vx, vy, pSigmaWidth, pSigmaLength);
		copyCovMatrix(q, pCov, 0);
		
		// the velocity can change from 0 to 8m/s within one dt (Ball kicked).
		double vSigmaMax = 400;
		double vSigmaDef = 30;
		double vSigmaMinWidth = 0.1;
		double vSigmaMinLength = vel > maxRollingVelocity ? 10 : 5;
		double vSigmaWidth = vSigmaMinWidth + ((1 - relVel) * (vSigmaDef - vSigmaMinWidth));
		vSigmaWidth += collisionInfluence * (vSigmaMax - vSigmaWidth);
		double vSigmaLength = vSigmaMinLength + ((1 - relVel) * (vSigmaDef - vSigmaMinLength));
		vSigmaLength += collisionInfluence * (vSigmaMax - vSigmaLength);
		Matrix vCov = createCovarianceMatrix(vx, vy, vSigmaWidth, vSigmaLength);
		copyCovMatrix(q, vCov, 3);
		
		double aSigmaMax = 100;
		double aSigmaDef = 1;
		double aSigmaMinWidth = 0.01;
		double aSigmaWidth = aSigmaMinWidth + ((1 - relVel) * (aSigmaDef - aSigmaMinWidth));
		aSigmaWidth += collisionInfluence * (aSigmaMax - aSigmaWidth);
		double aSigmaLength = vel > maxRollingVelocity ? aSigmaMax : aSigmaDef;
		aSigmaLength += collisionInfluence * (aSigmaMax - aSigmaLength);
		Matrix aCov = createCovarianceMatrix(vx, vy, aSigmaWidth, aSigmaLength);
		copyCovMatrix(q, aCov, 6);
		
		
		// trust in z-coordinates
		q.set(2, 2, 1000);
		q.set(5, 5, 0);
		q.set(8, 8, 0);
		
		return q;
	}
	
	
	@Override
	public void newMeasurement(final Matrix measurement, final Matrix state, final double dt)
	{
		Matrix mp = measurement.getMatrix(0, 1, 0, 0);
		Matrix v = mp.minus(state.getMatrix(0, 1, 0, 0)).times(1 / dt);
		measuredVelBuffer[measuredVelBufferIdx] = new Vector2(v.get(0, 0), v.get(1, 0));
		measuredVelBufferIdx = (measuredVelBufferIdx + 1) % VEL_BUFFER_SIZE;
	}
	
	
	private void copyCovMatrix(final Matrix state, final Matrix cov, final int idx)
	{
		for (int i = idx; i < (idx + 2); i++)
		{
			for (int j = idx; j < (idx + 2); j++)
			{
				state.set(i, j, cov.get(i - idx, j - idx));
			}
		}
	}
	
	
	private Matrix createCovarianceMatrix(final double vx, final double vy, final double sigmaWidth,
			final double sigmaLength)
	{
		final double theta;
		if (vx != 0)
		{
			theta = Math.atan(vy / vx);
		} else if (vy != 0)
		{
			// acot(vx/vy) = acot(0) = pi/2
			theta = AngleMath.PI_HALF;
		} else
		{
			theta = 0;
		}
		
		Matrix T = new Matrix(2, 2);
		T.set(0, 0, Math.cos(theta) * sigmaLength);
		T.set(0, 1, -Math.sin(theta) * sigmaWidth);
		T.set(1, 0, Math.sin(theta) * sigmaLength);
		T.set(1, 1, Math.cos(theta) * sigmaWidth);
		
		Matrix cov = T.times(T.transpose());
		return cov;
	}
	
	
	@Override
	public AMotionResult generateMotionResult(final int id, final Matrix state, final boolean onCam)
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
		final double confidence = state.get(9, 0);
		return new BallMotionResult(x, y, z, vx, vy, vz, ax, ay, az, confidence, onCam);
	}
	
	
	@Override
	public Matrix generateMeasurementMatrix(final AWPCamObject observation, final Matrix state)
	{
		final WPCamBall obs = (WPCamBall) observation;
		final Matrix m = new Matrix(3, 1);
		m.set(0, 0, obs.x);
		m.set(1, 0, obs.y);
		m.set(2, 0, obs.z);
		return m;
	}
	
	
	@Override
	public Matrix generateStateMatrix(final Matrix measurement, final Matrix control)
	{
		// no control for ball
		final Matrix s = new Matrix(STATE_SIZE, 1);
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		s.set(2, 0, measurement.get(2, 0));
		s.set(9, 0, 1);
		return s;
	}
	
	
	@Override
	public Matrix generateControlMatrix(final IControl control, final Matrix state)
	{
		// no control for ball
		return null;
	}
	
	
	@Override
	public Matrix generateCovarianceMatrix(final Matrix state)
	{
		final Matrix p = new Matrix(STATE_DYNAMICS_SIZE, STATE_DYNAMICS_SIZE);
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
	public int extractObjectID(final AWPCamObject observation)
	{
		// a ball has no id, so we set it to 0
		return 0;
	}
	
	
	@Override
	public Matrix measurementDynamics(final Matrix state)
	{
		final Matrix m = new Matrix(3, 1);
		m.set(0, 0, state.get(0, 0));
		m.set(1, 0, state.get(1, 0));
		m.set(2, 0, state.get(2, 0));
		return m;
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTstate(final Matrix state)
	{
		return Matrix.identity(3, STATE_DYNAMICS_SIZE);
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTnoise(final Matrix state)
	{
		return Matrix.identity(3, 3);
	}
	
	
	@Override
	public Matrix getMeasurementCovariance(final Matrix measurement)
	{
		return Matrix.identity(3, 3).times(varPosition);
	}
	
	
	@Override
	public Matrix updateStateOnNewControl(final IControl control, final Matrix state)
	{
		// nothing to do here because the ball has no control
		return state;
	}
	
	
	@Override
	public Matrix updateCovarianceOnNewControl(final IControl control, final Matrix covariance)
	{
		// nothing to do here because the ball has no control
		return covariance;
	}
	
	
	@Override
	public Matrix getStateOnNoObservation(final Matrix state)
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
	public Matrix getCovarianceOnNoObservation(final Matrix covariance)
	{
		final double improbable = 1e10;
		final Matrix covar = new Matrix(STATE_DYNAMICS_SIZE, STATE_DYNAMICS_SIZE);
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
	public Matrix getControlOnNoObservation(final Matrix control)
	{
		return generateControlMatrix(null, null);
	}
	
	
	@Override
	public Matrix statePostProcessing(final Matrix dynState, final Matrix preState)
	{
		Matrix newState = preState.copy();
		newState.setMatrix(0, STATE_DYNAMICS_SIZE - 1, 0, 0, dynState);
		double vx = dynState.get(3, 0);
		double vy = dynState.get(4, 0);
		final double v = Math.sqrt((vx * vx) + (vy * vy));
		double vxpre = preState.get(3, 0);
		double vypre = preState.get(4, 0);
		final double vpre = Math.sqrt((vxpre * vxpre) + (vypre * vypre));
		// if ball is getting faster in new state, filter for minVelocity
		if ((vpre < v) && (v < minVelocity))
		{
			newState.set(3, 0, 0);
			newState.set(4, 0, 0);
			newState.set(5, 0, 0);
			newState.set(6, 0, 0);
			newState.set(7, 0, 0);
			newState.set(8, 0, 0);
		}
		
		// if (v > 12000)
		// {
		// log.debug("Filtered high vel: " + v);
		// return preState;
		// }
		
		return newState;
	}
	
	
	@Override
	public Matrix getDynamicsState(final Matrix fullState)
	{
		return fullState.getMatrix(0, STATE_DYNAMICS_SIZE - 1, 0, 0);
	}
	
	private static class RelVelFunction implements IFunction1D
	{
		private final double					vmax;
		private final PolynomialFunction	relVelFunc;
		
		
		/**
		 * @param vmax
		 */
		public RelVelFunction(final double vmax)
		{
			this.vmax = vmax;
			Matrix A = new Matrix(3, 3);
			A.set(0, 0, 0);
			A.set(0, 1, 0);
			A.set(0, 2, 1);
			A.set(1, 0, 2 * vmax);
			A.set(1, 1, 1);
			A.set(1, 2, 0);
			A.set(2, 0, vmax * vmax);
			A.set(2, 1, vmax);
			A.set(2, 2, 0);
			Matrix b = new Matrix(3, 1);
			b.set(0, 0, 0);
			b.set(1, 0, 0);
			b.set(2, 0, 1);
			Matrix x = A.solve(b);
			double[] c = new double[] { x.get(2, 0), x.get(1, 0), x.get(0, 0) };
			relVelFunc = new PolynomialFunction(c);
		}
		
		
		@Override
		public double eval(final double... x)
		{
			double v = Math.max(0, Math.min(vmax, x[0]));
			return relVelFunc.value(v);
		}
		
		
		@Override
		public List<Double> getParameters()
		{
			return null;
		}
		
		
		@Override
		public EFunction getIdentifier()
		{
			return null;
		}
	}
	
	
	@Override
	public void estimateControl(final IFilter bot, final AMotionResult oldState, final CamRobot newBot,
			final CamRobot lastBot, final double dt)
	{
	}
}
