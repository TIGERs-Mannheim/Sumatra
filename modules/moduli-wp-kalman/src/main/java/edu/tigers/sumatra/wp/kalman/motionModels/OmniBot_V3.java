package edu.tigers.sumatra.wp.kalman.motionModels;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.kalman.WPConfig;
import edu.tigers.sumatra.wp.kalman.data.AMotionResult;
import edu.tigers.sumatra.wp.kalman.data.AWPCamObject;
import edu.tigers.sumatra.wp.kalman.data.IControl;
import edu.tigers.sumatra.wp.kalman.data.OmnibotControl_V3;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V3;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 */
public class OmniBot_V3 implements IMotionModel
{
	/** m */
	private static final double	BASE_STDEV_POSITION				= 0.001;
	/** m/s */
	private static final double	BASE_STDEV_VELOCITY				= 0.05;
	/** m/s^2 */
	private static final double	BASE_STDEV_ACC						= 3;
	/** rad */
	private static final double	BASE_STDEV_ORIENTATION			= 0.01;
	/** rad/s */
	private static final double	BASE_STDEV_ORI_VEL				= 0.1;
	/** rad/s^2 */
	private static final double	BASE_STDEV_ORI_ACC				= 1;
	
	/** m */
	private static final double	BASE_STDEV_POSITION_MEAS		= 0.03;
	/** rad */
	private static final double	BASE_STDEV_ORIENTATION_MEAS	= 0.2;
	
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_ACCEL				= 3.0;
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_BRAKE_ACCEL		= 3.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_ACCEL			= 5.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_BRAKE_ACCEL	= 5.0;
	private static final double	ACCEL_RAMP_PERCENT_SEC1			= 0.1;
	private static final double	ACCEL_RAMP_PERCENT_SEC2			= 0.7;
	private static final double	ACCEL_RAMP_PERCENT_SEC3			= 0.2;
	
	/** m/s */
	private static final double	BASE_ANGLE_TAKE_VEL				= 0.5;
	private static final double	ANGLE_TAKE_FACTOR					= 0.05;
	
	
	private final double				varPosition;
	private final double				varPositionMeasurement;
	private final double				varVelocity;
	private final double				varAcc;
	private final double				varOrientation;
	private final double				varOriVel;
	private final double				varOriAcc;
	private final double				varOrientationMeasurement;
	
	private final double				botCtrlMaxAccel;
	private final double				botCtrlMaxBrakeAccel;
	private final double				botCtrlMaxAngAccel;
	private final double				botCtrlMaxAngBrakeAccel;
	
	private final double				angleTakeVel;
	
	
	/**
	 * 
	 */
	public OmniBot_V3()
	{
		varPosition = Math.pow(BASE_STDEV_POSITION * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varPositionMeasurement = Math.pow(BASE_STDEV_POSITION_MEAS * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varVelocity = Math.pow(BASE_STDEV_VELOCITY * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varAcc = Math.pow(BASE_STDEV_ACC * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varOrientation = Math.pow(BASE_STDEV_ORIENTATION, 2);
		varOriVel = Math.pow(BASE_STDEV_ORI_VEL, 2);
		varOrientationMeasurement = Math.pow(BASE_STDEV_ORIENTATION_MEAS, 2);
		varOriAcc = Math.pow(BASE_STDEV_ORI_ACC * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, 2);
		
		botCtrlMaxAccel = BASE_CRTL_MAX_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxBrakeAccel = BASE_CRTL_MAX_BRAKE_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxAngAccel = BASE_CRTL_MAX_ANG_ACCEL * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		botCtrlMaxAngBrakeAccel = BASE_CRTL_MAX_ANG_BRAKE_ACCEL
				* WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		angleTakeVel = BASE_ANGLE_TAKE_VEL * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
	}
	
	
	@Override
	public Matrix dynamics(final Matrix state, final Matrix control, final double dt, final MotionContext context)
	{
		
		double x = state.get(0, 0);
		double y = state.get(1, 0);
		double w = state.get(2, 0);
		double vx = state.get(3, 0);
		double vy = state.get(4, 0);
		double vw = state.get(5, 0);
		double ax = state.get(6, 0);
		double ay = state.get(7, 0);
		double aw = state.get(8, 0);
		
		if (control == null)
		{
			// dynamics
			x = x + (vx * dt);
			y = y + (vy * dt);
			w = w + (vw * dt);
		} else
		{
			double ux = control.get(0, 0);
			double uy = control.get(1, 0);
			double uw = control.get(2, 0);
			// double ax = control.get(3, 0);
			// double ay = control.get(4, 0);
			// double aw = control.get(5, 0);
			
			double a = getBotCtrlMaxAccel();
			double aBrake = getBotCtrlMaxBrakeAccel();
			double aAng = getBotCtrlMaxAngAccel();
			double aAngBrake = getBotCtrlMaxAngBrakeAccel();
			// double axy = Math.sqrt((ax * ax) + (ay * ay));
			// a = Math.min(axy, a);
			// aBrake = Math.min(axy, aBrake);
			// aAng = Math.min(aw, aAng);
			// aAngBrake = Math.min(aw, aAngBrake);
			
			// dynamics
			// x = (x + (vx * dt)) + (0.5 * dt * dt * ax);
			// y = (y + (vy * dt)) + (0.5 * dt * dt * ay);
			// w = (w + (vw * dt)) + (0.5 * dt * dt * aw);
			x = (x + (vx * dt));
			y = (y + (vy * dt));
			w = (w + (vw * dt));
			
			// vx = vx + (ax * dt);
			// vy = vy + (ay * dt);
			// vw = vw + (aw * dt);
			
			vx = estimateVelocity(vx, ux, dt, a, aBrake);
			vy = estimateVelocity(vy, uy, dt, a, aBrake);
			vw = estimateVelocity(vw, uw, dt, aAng, aAngBrake);
		}
		
		// create return object
		final Matrix f = new Matrix(9, 1);
		f.set(0, 0, x);
		f.set(1, 0, y);
		f.set(2, 0, w);
		f.set(3, 0, vx);
		f.set(4, 0, vy);
		f.set(5, 0, vw);
		f.set(6, 0, ax);
		f.set(7, 0, ay);
		f.set(8, 0, aw);
		return f;
	}
	
	
	@Override
	public void estimateControl(final IFilter bot, final AMotionResult os, final CamRobot newBot, final CamRobot lastbot,
			final double dt)
	{
		RobotMotionResult_V3 newState = (RobotMotionResult_V3) bot.getPrediction(bot.getTimestamp());
		RobotMotionResult_V3 oldState = (RobotMotionResult_V3) os;
		
		
		final double oldX = oldState.x;
		final double oldY = oldState.y;
		final double oldW = oldState.orientation;
		
		final double newX = newState.x;
		final double newY = newState.y;
		final double newW = newState.orientation;
		
		// Determine new v_x and v_y
		final double dX = (newX - oldX);
		final double dY = (newY - oldY);
		final double dW = (AngleMath.difference(newW, oldW));
		
		IVector2 vxy = new Vector2(dX, dY).multiply(1 / dt);
		// double vx = (dX / dt);
		// double vy = (dY / dt);
		double vw = (dW / dt);
		
		// kreissehne
		// double s = vxy.getLength();
		// double angleDiff = Math.abs(vw);
		// if (angleDiff > 1e-5)
		// {
		// // bogenlänge
		// double b = (s * angleDiff) / Math.sin(angleDiff);
		// vxy = vxy.multiplyNew(b / s);
		// vxy = vxy.turnNew(-vw);
		// }
		
		IVector2 velMeas = newBot.getPos().subtractNew(new Vector2(oldState.x, oldState.y)).multiply(1 / dt);
		IVector2 vel = new Vector2(newState.x, newState.y).subtract(new Vector2(oldState.x, oldState.y)).multiply(1 / dt);
		IVector2 acc = velMeas.subtractNew(vel).multiply(1 / dt);
		double ax = acc.x();
		double ay = acc.y();
		double aw = 0;
		
		bot.setControl(new OmnibotControl_V3(vxy.x(), vxy.y(), vw, ax, ay, aw));
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTstate(final Matrix state, final double dt)
	{
		final Matrix a = new Matrix(9, 9);
		
		// pos
		a.set(0, 0, 1.0);
		a.set(1, 1, 1.0);
		a.set(2, 2, 1.0);
		
		// pos -> vel
		a.set(0, 3, dt);
		a.set(1, 4, dt);
		a.set(2, 5, dt);
		
		// pos -> acc
		a.set(0, 6, 0.5 * dt * dt);
		a.set(1, 7, 0.5 * dt * dt);
		a.set(2, 8, 0.5 * dt * dt);
		
		// vel
		a.set(3, 3, 1.0);
		a.set(4, 4, 1.0);
		a.set(5, 5, 1.0);
		
		// vel -> acc
		a.set(3, 6, dt);
		a.set(4, 7, dt);
		a.set(5, 8, dt);
		
		// acc
		a.set(6, 6, 1.0);
		a.set(7, 7, 1.0);
		a.set(8, 8, 1.0);
		
		return a;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTnoise(final Matrix state, final double dt)
	{
		return Matrix.identity(9, 9);
	}
	
	
	@Override
	public Matrix getDynamicsCovariance(final Matrix state, final double dt)
	{
		final Matrix q = new Matrix(9, 9);
		q.set(0, 0, varPosition);
		q.set(1, 1, varPosition);
		q.set(2, 2, varOrientation);
		q.set(3, 3, varVelocity);
		q.set(4, 4, varVelocity);
		q.set(5, 5, varOriVel);
		q.set(6, 6, varAcc);
		q.set(7, 7, varAcc);
		q.set(8, 8, varOriAcc);
		return q;
	}
	
	
	@Override
	public AMotionResult generateMotionResult(final int id, final Matrix state, final boolean onCam)
	{
		final double x = state.get(0, 0);
		final double y = state.get(1, 0);
		final double w = normalizeAngle(state.get(2, 0));
		final double vx = state.get(3, 0);
		final double vy = state.get(4, 0);
		final double vw = state.get(5, 0);
		final double ax = state.get(6, 0);
		final double ay = state.get(7, 0);
		final double aw = state.get(8, 0);
		final double confidence = 1.0;
		return new RobotMotionResult_V3(x, y, w, vx, vy, vw, ax, ay, aw, confidence, onCam);
	}
	
	
	@Override
	public Matrix generateMeasurementMatrix(final AWPCamObject observation, final Matrix state)
	{
		final WPCamBot obs = (WPCamBot) observation;
		final Matrix o = new Matrix(3, 1);
		o.set(0, 0, obs.x);
		o.set(1, 0, obs.y);
		if (state == null)
		{
			o.set(2, 0, obs.orientation);
		} else
		{
			double curOrientation = state.get(2, 0);
			// this will avoid jumps from curState to new observation
			double corOri = determineContinuousAngle(curOrientation, obs.orientation);
			o.set(2, 0, corOri);
		}
		return o;
	}
	
	
	@Override
	public Matrix generateStateMatrix(final Matrix measurement, final Matrix control)
	{
		final Matrix s = new Matrix(9, 1);
		// pos
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		// orientation
		s.set(2, 0, measurement.get(2, 0));
		
		if (control != null)
		{
			// v
			s.set(4, 0, control.get(0, 0));
			// omega
			s.set(5, 0, control.get(1, 0));
			// eta
			s.set(6, 0, control.get(2, 0));
		}
		return s;
	}
	
	
	@Override
	public Matrix updateStateOnNewControl(final IControl control, final Matrix state)
	{
		return state;
	}
	
	
	@Override
	public Matrix updateCovarianceOnNewControl(final IControl control, final Matrix covariance)
	{
		// covariance.set(3, 0, 0.0);
		// covariance.set(3, 1, 0.0);
		// covariance.set(3, 2, 0.0);
		// covariance.set(3, 3, 1.0);
		// covariance.set(3, 4, 0.0);
		// covariance.set(3, 5, 0.0);
		// covariance.set(3, 6, 0.0);
		//
		// covariance.set(0, 3, 0.0);
		// covariance.set(1, 3, 0.0);
		// covariance.set(2, 3, 0.0);
		// covariance.set(4, 3, 0.0);
		// covariance.set(5, 3, 0.0);
		// covariance.set(6, 3, 0.0);
		return covariance;
	}
	
	
	@Override
	public Matrix generateControlMatrix(final IControl control, final Matrix state)
	{
		if (control != null)
		{
			final OmnibotControl_V3 ctrl = (OmnibotControl_V3) control;
			final Matrix u = new Matrix(6, 1);
			
			u.set(0, 0, ctrl.getVx());
			u.set(1, 0, ctrl.getVy());
			u.set(2, 0, ctrl.getVw());
			
			u.set(3, 0, ctrl.getAx());
			u.set(4, 0, ctrl.getAy());
			u.set(5, 0, ctrl.getAw());
			return u;
		}
		return null;
	}
	
	
	@Override
	public Matrix generateCovarianceMatrix(final Matrix state)
	{
		final Matrix p = new Matrix(9, 9);
		return p;
	}
	
	
	@Override
	public int extractObjectID(final AWPCamObject observation)
	{
		if (observation instanceof WPCamBot)
		{
			return ((WPCamBot) observation).id;
		}
		throw new IllegalStateException("observation could not be casted");
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
	public Matrix getMeasurementJacobianWRTstate(final Matrix meas)
	{
		Matrix a = Matrix.identity(3, 9);
		return a;
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTnoise(final Matrix state)
	{
		return Matrix.identity(3, 3);
	}
	
	
	@Override
	public Matrix getMeasurementCovariance(final Matrix measurement)
	{
		final Matrix r = new Matrix(3, 3);
		r.set(0, 0, varPositionMeasurement);
		r.set(1, 1, varPositionMeasurement);
		r.set(2, 2, varOrientationMeasurement);
		return r;
	}
	
	
	protected double normalizeAngle(final double angleIn)
	{
		return AngleMath.normalizeAngle(angleIn);
	}
	
	
	protected double determineContinuousAngle(final double oldAngle, final double newAngle)
	{
		// project new orientation next to old one
		// thus values greater than PI are possible but the handling gets easier
		// standard: just a small rotation
		double dAngle = newAngle - normalizeAngle(oldAngle);
		// rotation clockwise over Pi-border
		if (dAngle > Math.PI)
		{
			dAngle = dAngle - (2 * Math.PI);
		}
		// rotation counter-clockwise over Pi-border
		else if (dAngle < -Math.PI)
		{
			dAngle = dAngle + (2 * Math.PI);
		}
		return oldAngle + dAngle;
	}
	
	
	protected double determineAngleDifference(final double angle1, final double angle2)
	{
		return AngleMath.difference(angle1, angle2);
	}
	
	
	/**
	 * @param current
	 * @param target
	 * @param dt
	 * @param maxAcc
	 * @param maxBrake
	 * @return
	 */
	public double estimateVelocity(double current, double target, double dt, final double maxAcc,
			final double maxBrake)
	{
		double vel = 0.0;
		if ((Math.signum(current) == Math.signum(target)) || (current == 0.0) || (target == 0.0))
		{
			// just accelerating or braking
			
			boolean negative = false;
			if ((current < 0.0) || (target < 0.0))
			{
				negative = true;
				current = -current;
				target = -target;
			}
			
			if (current < target)
			{
				// accelerating
				final double accEndT = target / maxAcc;
				final double currentFactor = current / target;
				double tCurrent = 0.0;
				if (currentFactor < ACCEL_RAMP_PERCENT_SEC1)
				{
					tCurrent = current * (((1.0 / 3.0) * accEndT) / (ACCEL_RAMP_PERCENT_SEC1 * target));
				} else if (currentFactor < (ACCEL_RAMP_PERCENT_SEC1 + ACCEL_RAMP_PERCENT_SEC2))
				{
					tCurrent = ((current - (ACCEL_RAMP_PERCENT_SEC1 * target))
							* (((1.0 / 3.0) * accEndT) / ((ACCEL_RAMP_PERCENT_SEC2) * target)))
							+ (accEndT / 3.0);
				} else
				{
					tCurrent = ((current - (ACCEL_RAMP_PERCENT_SEC1 * target) - (ACCEL_RAMP_PERCENT_SEC2 * target))
							* (((1.0 / 3.0) * accEndT) / (ACCEL_RAMP_PERCENT_SEC3 * target)))
							+ ((2 * accEndT) / 3.0);
				}
				
				final double tFinal = tCurrent + dt;
				if (tFinal < ((1.0 / 3.0) * accEndT))
				{
					vel = ((ACCEL_RAMP_PERCENT_SEC1 * target) / ((1.0 / 3.0) * accEndT)) * tFinal;
				} else if (tFinal < ((2.0 / 3.0) * accEndT))
				{
					vel = ((((ACCEL_RAMP_PERCENT_SEC2 * target) / ((1.0 / 3.0) * accEndT)) * tFinal)
							- (ACCEL_RAMP_PERCENT_SEC2 * target))
							+ (ACCEL_RAMP_PERCENT_SEC1 * target);
				} else if (tFinal < accEndT)
				{
					vel = (((ACCEL_RAMP_PERCENT_SEC3 * target) / ((1.0 / 3.0) * accEndT))
							* (tFinal - ((2.0 / 3.0) * accEndT)))
							+ ((1 - ACCEL_RAMP_PERCENT_SEC3) * target);
				} else
				{
					vel = target;
				}
			} else
			{
				// braking
				vel = current - (maxBrake * dt);
				if (vel < target)
				{
					vel = target;
				}
			}
			
			if (negative)
			{
				vel = -vel;
			}
		} else
		{
			// first brake, than accelerate
			final double tToHalt = Math.abs(current) / maxBrake;
			if (tToHalt >= dt)
			{
				// we only brake in our dt
				vel = estimateVelocity(current, 0.0, dt, maxAcc, maxBrake);
			} else
			{
				// we brake to 0 and accelerate then.
				// here we only have to handle the acceleration.
				dt = dt - tToHalt;
				vel = estimateVelocity(0.0, target, dt, maxAcc, maxBrake);
			}
		}
		
		return vel;
	}
	
	
	@Override
	public Matrix getStateOnNoObservation(final Matrix state)
	{
		state.set(3, 0, state.get(2, 0));
		state.set(4, 0, 0.0);
		state.set(5, 0, 0.0);
		state.set(6, 0, 0.0);
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
		return state;
	}
	
	
	/**
	 * @return the ANGLE_TAKE_FACTOR
	 */
	public double getAngleTakeFactor()
	{
		return ANGLE_TAKE_FACTOR;
	}
	
	
	/**
	 * @return the angleTakeVel
	 */
	public double getAngleTakeVel()
	{
		return angleTakeVel;
	}
	
	
	/**
	 * @return the botCtrlMaxBrakeAccel
	 */
	public double getBotCtrlMaxBrakeAccel()
	{
		return botCtrlMaxBrakeAccel;
	}
	
	
	/**
	 * @return the botCtrlMaxAccel
	 */
	public double getBotCtrlMaxAccel()
	{
		return botCtrlMaxAccel;
	}
	
	
	/**
	 * @return the botCtrlMaxAngBrakeAccel
	 */
	public double getBotCtrlMaxAngBrakeAccel()
	{
		return botCtrlMaxAngBrakeAccel;
	}
	
	
	/**
	 * @return the botCtrlMaxAngAccel
	 */
	public double getBotCtrlMaxAngAccel()
	{
		return botCtrlMaxAngAccel;
	}
	
	
	@Override
	public Matrix getDynamicsState(final Matrix fullState)
	{
		return fullState;
	}
}
