package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.EFunction;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.WPCamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 */
public class BallMotionModel implements IMotionModel
{
	/** m/s */
	private static final double		baseMinVelocity			= 0.05;
	/** m/s */
	private static final double		baseMaxRollingVelocity	= 2.0;
	
	/** m */
	private static final double		baseStDevPosition			= 0.01;
	/** m/s */
	private static final double		baseStDevVelocity			= 0.001;
	/** m/s^2 */
	private static final double		baseStDevAcceleration	= 0.001;
	
	/** m/s^2 */
	private static final double		baseGravity					= -9.81;
	
	// Particle Filter
	// /** m */
	// private static final double baseStDevSamplePosition = 0.008;
	// /** m/s */
	// private static final double baseStDevSampleVel = 0.1;
	// private final GaussianGenerator genPos;
	// private final GaussianGenerator genVel;
	
	
	private final double					minVelocity;
	private final double					maxRollingVelocity;
	
	private final double					varPosition;
	private final double					varVelocity;
	private final double					varAcceleration;
	
	private final double					aFall;
	
	private final PredictionContext	context;
	
	private final IFunction1D			relVelFunc;
	
	private long							tLastCollision				= 0;
	
	
	/**
	 * @param context
	 */
	public BallMotionModel(final PredictionContext context)
	{
		this.context = context;
		minVelocity = baseMinVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		maxRollingVelocity = baseMaxRollingVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		varPosition = Math.pow(baseStDevPosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varVelocity = Math.pow(baseStDevVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varAcceleration = Math.pow(baseStDevAcceleration * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A, 2);
		
		double gravity = baseGravity * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		aFall = gravity;
		
		// double stDevSamplePosition = baseStDevSamplePosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT;
		// double stDevSampleVel = baseStDevSampleVel * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		// Random rng = new MersenneTwisterRNG();
		// genPos = new GaussianGenerator(0, stDevSamplePosition, rng);
		// genVel = new GaussianGenerator(0, stDevSampleVel, rng);
		
		double vMaxRel = 500;
		relVelFunc = new RelVelFunction(vMaxRel);
	}
	
	
	@Override
	public Matrix dynamics(final Matrix state, final Matrix control, final double dt)
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
		IVector2 dir = new Vector2(vx, vy);
		
		double acc = AIConfig.getBallModel().getAcc() * 1000;
		
		if ((z > 0.0) || (vz > 0.0) || (az > 0.0))
		{
			// flying ball
			ax = 0.0;
			ay = 0.0;
			az = aFall;
		} else
		{
			if ((v != 0) && ((v + (acc * dt)) >= 0))
			{
				ax = (acc * dir.x()) / v;
				ay = (acc * dir.y()) / v;
				az = 0.0;
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
		x = (x + (vx * dt)) + (0.5 * dt * dt * ax);
		y = (y + (vy * dt)) + (0.5 * dt * dt * ay);
		z = (z + (vz * dt)) + (0.5 * dt * dt * az);
		
		if (z <= 0.0)
		{
			z = 0.0;
			vz = 0.0;
			az = 0.0;
		}
		
		// calculate ball's velocity from current acceleration
		vx = vx + (dt * ax);
		vy = vy + (dt * ay);
		vz = vz + (dt * az);
		
		
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
		
		Matrix corState = handleCollision(state, newState, dt);
		return corState;
	}
	
	
	private Matrix handleCollision(final Matrix state, final Matrix newState, final double dt)
	{
		if (newState.get(2, 0) > 50)
		{
			// ball is flying
			return newState;
		}
		
		final IVector2 ballPos = new Vector2(newState.get(0, 0), newState.get(1, 0));
		final IVector2 ballVel = new Vector2(newState.get(3, 0), newState.get(4, 0));
		final Matrix corState = newState.copy();
		
		List<IFilter> bots = new ArrayList<IFilter>(context.getBlueBots().size() + context.getYellowBots().size());
		bots.addAll(context.getBlueBots().values());
		bots.addAll(context.getYellowBots().values());
		
		float goalx = ((AIConfig.getGeometry().getFieldLength() / 2) - AIConfig.getGeometry().getBallRadius())
				+ AIConfig.getGeometry().getGoalDepth();
		float goaly = AIConfig.getGeometry().getGoalSize() / 2;
		
		float fieldx = ((AIConfig.getGeometry().getFieldLength() / 2) - AIConfig.getGeometry().getBallRadius());
		float fieldy = (((AIConfig.getGeometry().getFieldWidth() / 2) + AIConfig.getGeometry().getBoundaryWidth()) - AIConfig
				.getGeometry().getBallRadius());
		
		if ((Math.abs(ballPos.x()) > fieldx) &&
				(Math.abs(ballPos.y()) < fieldy))
		{
			tLastCollision = SumatraClock.nanoTime();
		}
		
		// goal collision
		if ((Math.abs(ballPos.x()) > goalx) &&
				(Math.abs(ballPos.x()) < (goalx + 200)) &&
				(Math.abs(ballPos.y()) < goaly))
		{
			IVector2 outVel = ballCollision(ballVel, new Vector2(-Math.signum(ballPos.x()), 0));
			corState.set(0, 0, state.get(0, 0));
			corState.set(1, 0, state.get(1, 0));
			corState.set(3, 0, outVel.x());
			corState.set(4, 0, outVel.y());
			tLastCollision = SumatraClock.nanoTime();
			return corState;
		}
		
		// bot collisions
		for (IFilter f : bots)
		{
			RobotMotionResult_V2 mr = (RobotMotionResult_V2) f.getLookahead(0);
			IVector3 pos = new Vector3(mr.x, mr.y, mr.orientation);
			Circle botHull = new Circle(pos.getXYVector(), AIConfig.getGeometry().getBotRadius()
					+ AIConfig.getGeometry().getBallRadius());
			
			// possible kick
			IVector2 kickerPos = AiMath.getBotKickerPos(pos.getXYVector(), pos.z(),
					Geometry.getCenter2DribblerDistDefault());
			if (kickerPos.equals(ballPos, 100))
			{
				tLastCollision = SumatraClock.nanoTime();
			}
			
			if (botHull.isPointInShape(ballPos))
			{
				// ball is within bot, but allow it to be in front of kicker
				float r = AIConfig.getGeometry().getBotRadius();
				float h = r - Geometry.getCenter2DribblerDistDefault();
				float theta = (float) Math.acos((h - r) / -r);
				IVector2 center2Ball = ballPos.subtractNew(botHull.center());
				float angleDiff = Math.abs(AngleMath.getShortestRotation(center2Ball.getAngle(), pos.z()));
				
				// sum of angles:
				float gamma = AngleMath.PI_HALF - theta;
				float beta = theta - angleDiff;
				float alpha = AngleMath.PI - beta - gamma;
				float a = botHull.radius();
				// Sinussatz
				float c = (float) ((a * Math.sin(gamma)) / Math.sin(alpha));
				float lenCenter2Kicker = c;
				
				IVector2 collisionNormal = null;
				if (angleDiff > theta)
				{
					// ball is not in front of kicker
					collisionNormal = center2Ball.normalizeNew();
				} else if (center2Ball.getLength2() <= lenCenter2Kicker)
				{
					// ball is in front of kicker and inside bot
					collisionNormal = new Vector2(pos.z());
				}
				
				if ((collisionNormal != null) && !ballVel.isZeroVector())
				{
					IVector2 outVel = ballCollision(ballVel, collisionNormal);
					
					corState.set(0, 0, state.get(0, 0));
					corState.set(1, 0, state.get(1, 0));
					corState.set(3, 0, outVel.x());
					corState.set(4, 0, outVel.y());
					tLastCollision = SumatraClock.nanoTime();
					return corState;
				}
			}
		}
		
		return corState;
	}
	
	
	private IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal)
	{
		if (ballVel.isZeroVector())
		{
			return ballVel;
		}
		float velInfAngle = AngleMath.normalizeAngle(ballVel.getAngle() + AngleMath.PI);
		float velAngleDiff = AngleMath.getShortestRotation(velInfAngle, collisionNormal.getAngle());
		// TODO damping
		float damping = 0.1f;
		IVector2 outVel = new Vector2(collisionNormal).turn(velAngleDiff).scaleTo(
				ballVel.getLength2() * (1 - damping));
		return outVel;
	}
	
	
	@Override
	public Matrix sample(final Matrix state, final Matrix control)
	{
		throw new IllegalStateException();
		// final double x = state.get(0, 0);
		// final double y = state.get(1, 0);
		// final double z = state.get(2, 0);
		// final double vx = state.get(3, 0);
		// final double vy = state.get(4, 0);
		// final double vz = state.get(5, 0);
		// final double ax = state.get(6, 0);
		// final double ay = state.get(7, 0);
		// final double az = state.get(8, 0);
		//
		// final Matrix newState = new Matrix(9, 1);
		// newState.set(0, 0, (x + genPos.nextValue()));
		// newState.set(1, 0, (y + genPos.nextValue()));
		// newState.set(2, 0, z);
		// newState.set(3, 0, (vx + genVel.nextValue()));
		// newState.set(4, 0, (vy + genVel.nextValue()));
		// newState.set(5, 0, vz);
		// newState.set(6, 0, ax);
		// newState.set(7, 0, ay);
		// newState.set(8, 0, az);
		// return newState;
	}
	
	
	@Override
	public double transitionProbability(final Matrix stateNew, final Matrix stateOld, final Matrix control)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public double measurementProbability(final Matrix state, final Matrix measurement, final double dt)
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
	public Matrix getDynamicsJacobianWRTstate(final Matrix state, final double dt)
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
	public Matrix getDynamicsJacobianWRTnoise(final Matrix state, final double dt)
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
		// return Matrix.identity(9, 9);
	}
	
	
	@Override
	public Matrix getDynamicsCovariance(final Matrix state, final double dt)
	{
		final Matrix q = new Matrix(9, 9);
		double vx = state.get(3, 0);
		double vy = state.get(4, 0);
		
		// sigma: max change within one dt,
		// 2*sigma=95% range in data, so divide by 2 below
		
		float vel = (float) Math.sqrt((vx * vx) + (vy * vy));
		float relVel = relVelFunc.eval(vel);
		double collisionInfluence = 1 - Math.min(1, (SumatraClock.nanoTime() - tLastCollision) / 7e7);
		// IVector2 dir = getAverageVelDir();
		// vx = dir.x();
		// vy = dir.y();
		
		// at max vel of 8m/2, position can change by 8m in one second
		double pSigmaDef = 5;
		double pSigmaMinWidth = 0.01;
		pSigmaMinWidth += collisionInfluence * (pSigmaDef - pSigmaMinWidth);
		double pSigmaWidth = pSigmaMinWidth + ((1 - relVel) * (pSigmaDef - pSigmaMinWidth));
		double pSigmaLength = pSigmaDef;
		Matrix pCov = createCovarianceMatrix(vx, vy, pSigmaWidth, pSigmaLength);
		copyCovMatrix(q, pCov, 0);
		
		// the velocity can change from 0 to 8m/s within one dt (Ball kicked).
		double vSigmaMax = 700;
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
		q.set(2, 2, 0);
		q.set(5, 5, 0);
		q.set(8, 8, 0);
		
		
		// CSVExporter pexporter = new CSVExporter("data/ball/model/pcov", false, true);
		// pexporter.addValues(ExportDataContainer.toNumberList(pCov));
		// pexporter.close();
		// CSVExporter vexporter = new CSVExporter("data/ball/model/vcov", false, true);
		// vexporter.addValues(ExportDataContainer.toNumberList(vCov));
		// vexporter.close();
		// CSVExporter aexporter = new CSVExporter("data/ball/model/acov", false, true);
		// aexporter.addValues(ExportDataContainer.toNumberList(aCov));
		// aexporter.close();
		// CSVExporter sexporter = new CSVExporter("data/ball/model/state", false, true);
		// sexporter.addValues(ExportDataContainer.toNumberList(state));
		// sexporter.close();
		
		return q;
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
		final double confidence = 1.0;
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
		final Matrix s = new Matrix(9, 1);
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		s.set(2, 0, measurement.get(2, 0));
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
	public int extraxtObjectID(final AWPCamObject observation)
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
		return Matrix.identity(3, 9);
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
	public Matrix getControlOnNoObservation(final Matrix control)
	{
		return generateControlMatrix(null, null);
	}
	
	
	@Override
	public Matrix statePostProcessing(final Matrix state, final Matrix preState)
	{
		double vx = state.get(3, 0);
		double vy = state.get(4, 0);
		final double v = Math.sqrt((vx * vx) + (vy * vy));
		double vxpre = preState.get(3, 0);
		double vypre = preState.get(4, 0);
		final double vpre = Math.sqrt((vxpre * vxpre) + (vypre * vypre));
		// if ball is getting faster in new state, filter for minVelocity
		if ((vpre < v) && (v < minVelocity))
		{
			Matrix newState = state.copy();
			newState.set(3, 0, 0);
			newState.set(4, 0, 0);
			newState.set(5, 0, 0);
			newState.set(6, 0, 0);
			newState.set(7, 0, 0);
			newState.set(8, 0, 0);
			return newState;
		}
		return state;
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
		public float eval(final float... x)
		{
			double v = Math.max(0, Math.min(vmax, x[0]));
			return (float) relVelFunc.value(v);
		}
		
		
		@Override
		public List<Float> getParameters()
		{
			return null;
		}
		
		
		@Override
		public EFunction getIdentifier()
		{
			return null;
		}
	}
}
