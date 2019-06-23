package edu.tigers.sumatra.wp.kalman.motionModels;

import Jama.Matrix;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.wp.kalman.WPConfig;
import edu.tigers.sumatra.wp.kalman.data.AMotionResult;
import edu.tigers.sumatra.wp.kalman.data.AWPCamObject;
import edu.tigers.sumatra.wp.kalman.data.IControl;
import edu.tigers.sumatra.wp.kalman.data.OmnibotControl_V2;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V2;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;


/**
 */
public abstract class AOmniBot_V2 implements IMotionModel
{
	/** rad/s */
	private static final double	BASE_NO_RPTATION_BORDER			= 0.1;
	/** m/s */
	private static final double	BASE_NO_MOVEMENT_BORDER			= 0.0001;
	
	/** m */
	private static final double	BASE_STDEV_POSITION				= 0.001;
	/** m/s */
	private static final double	BASE_STDEV_VELOCITY				= 0.05;
	/** rad */
	private static final double	BASE_STDEV_MOVEMENT_DIR			= 1000;
	/** rad */
	private static final double	BASE_STDEV_ORIENTATION			= 0.01;
	/** rad/s rotation with movement */
	private static final double	BASE_STDEV_OMEGA					= 0.1;
	/** rad/s rotation w/o lin movement */
	private static final double	BASE_STDEV_ETA						= 0.1;
	
	/** m */
	private static final double	BASE_STDEV_POSITION_MEAS		= 0.001;
	/** rad */
	private static final double	BASE_STDEV_ORIENTATION_MEAS	= 0.01;
	
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_ACCEL				= 5.0;
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_BRAKE_ACCEL		= 5.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_ACCEL			= 50.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_BRAKE_ACCEL	= 50.0;
	private static final double	ACCEL_RAMP_PERCENT_SEC1			= 0.1;
	private static final double	ACCEL_RAMP_PERCENT_SEC2			= 0.7;
	private static final double	ACCEL_RAMP_PERCENT_SEC3			= 0.2;
	
	/** m/s */
	private static final double	BASE_ANGLE_TAKE_VEL				= 0.5;
	private static final double	ANGLE_TAKE_FACTOR					= 0.05;
	
	
	private final double				noRotationBorder;
	private final double				noMovementBorder;
	
	private final double				varPosition;
	private final double				varPositionMeasurement;
	private final double				varVelocity;
	private final double				varOrientation;
	private final double				varMovementDir;
	private final double				varOrientationMeasurement;
	private final double				varOmega;
	private final double				varEta;
	
	private final double				botCtrlMaxAccel;
	private final double				botCtrlMaxBrakeAccel;
	private final double				botCtrlMaxAngAccel;
	private final double				botCtrlMaxAngBrakeAccel;
	
	private final double				angleTakeVel;
	
	
	AOmniBot_V2()
	{
		noRotationBorder = BASE_NO_RPTATION_BORDER * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal;
		noMovementBorder = BASE_NO_MOVEMENT_BORDER * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		varPosition = Math.pow(BASE_STDEV_POSITION * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varPositionMeasurement = Math.pow(BASE_STDEV_POSITION_MEAS * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		varVelocity = Math.pow(BASE_STDEV_VELOCITY * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varOrientation = Math.pow(BASE_STDEV_ORIENTATION, 2);
		varMovementDir = Math.pow(BASE_STDEV_MOVEMENT_DIR, 2);
		varOrientationMeasurement = Math.pow(BASE_STDEV_ORIENTATION_MEAS, 2);
		varOmega = Math.pow(BASE_STDEV_OMEGA * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, 2);
		varEta = Math.pow(BASE_STDEV_ETA * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, 2);
		
		botCtrlMaxAccel = BASE_CRTL_MAX_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxBrakeAccel = BASE_CRTL_MAX_BRAKE_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxAngAccel = BASE_CRTL_MAX_ANG_ACCEL * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		botCtrlMaxAngBrakeAccel = BASE_CRTL_MAX_ANG_BRAKE_ACCEL
				* WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		angleTakeVel = BASE_ANGLE_TAKE_VEL * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTstate(final Matrix state, final double dt)
	{
		final Matrix a = new Matrix(7, 7);
		
		final double movAng = state.get(3, 0);
		final double v = state.get(4, 0);
		final double omega = state.get(5, 0);
		
		final double sinMovAng = Math.sin(movAng);
		final double cosMovAng = Math.cos(movAng);
		
		// x
		a.set(0, 0, 1.0);
		// y
		a.set(1, 1, 1.0);
		
		// orientation
		a.set(2, 2, 1.0);
		// ori + omega * dt
		a.set(2, 5, dt);
		// ori + eta * dt
		// a.set(2, 6, dt);
		
		// moveAngle
		a.set(3, 3, 1.0);
		// moveAngle + omega * dt
		a.set(3, 5, dt);
		
		// v
		a.set(4, 4, 1.0);
		// omega
		a.set(5, 5, 1.0);
		// eta
		a.set(6, 6, 1.0);
		
		if ((Math.abs(omega) < getNoRotationBorder()) || (Math.abs(v) < noMovementBorder))
		{
			// straight movement
			
			// x + vx * dt
			a.set(0, 4, cosMovAng * dt);
			// y + vy * dt
			a.set(1, 4, sinMovAng * dt);
		} else
		{
			// circular movement
			
			// s: Kreissehne, v: bogenlänge aka. linear vel
			double s = (v / omega) * Math.sin(omega);
			double f = s / v;
			
			a.set(0, 4, cosMovAng * f * dt);
			a.set(1, 4, sinMovAng * f * dt);
		}
		
		return a;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTnoise(final Matrix state, final double dt)
	{
		return Matrix.identity(7, 7);
	}
	
	
	@Override
	public Matrix getDynamicsCovariance(final Matrix state, final double dt)
	{
		final Matrix q = new Matrix(7, 7);
		q.set(0, 0, varPosition);
		q.set(1, 1, varPosition);
		q.set(2, 2, varOrientation);
		q.set(3, 3, varMovementDir);
		q.set(4, 4, varVelocity);
		q.set(5, 5, varOmega);
		q.set(6, 6, varEta);
		return q;
	}
	
	
	@Override
	public AMotionResult generateMotionResult(final int id, final Matrix state, final boolean onCam)
	{
		final double x = state.get(0, 0);
		final double y = state.get(1, 0);
		final double orientation = normalizeAngle(state.get(2, 0));
		final double movementAngle = normalizeAngle(state.get(3, 0));
		final double v = state.get(4, 0);
		final double omega = state.get(5, 0);
		final double eta = state.get(6, 0);
		final double confidence = 1.0;
		return new RobotMotionResult_V2(x, y, orientation, movementAngle, v, omega, eta, confidence, onCam);
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
		final Matrix s = new Matrix(7, 1);
		// pos
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		// orientation
		s.set(2, 0, measurement.get(2, 0));
		// movement dir
		s.set(3, 0, measurement.get(2, 0));
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
			final OmnibotControl_V2 ctrl = (OmnibotControl_V2) control;
			final Matrix u = new Matrix(6, 1);
			
			final double v = Math.sqrt((ctrl.vt * ctrl.vt) + (ctrl.vo * ctrl.vo));
			final double a = Math.sqrt((ctrl.at * ctrl.at) + (ctrl.ao * ctrl.ao));
			u.set(0, 0, v);
			u.set(1, 0, ctrl.omega);
			u.set(2, 0, ctrl.eta);
			// if (state.get(4, 0) > 100)
			// {
			u.set(3, 0, Math.atan2(ctrl.vo, ctrl.vt) + state.get(2, 0));
			// } else
			// {
			// u.set(3, 0, state.get(3, 0));
			// }
			if (ctrl.useAcc)
			{
				u.set(4, 0, a);
			} else
			{
				u.set(4, 0, Double.MAX_VALUE);
			}
			u.set(5, 0, Double.MAX_VALUE);
			return u;
		}
		return null;
	}
	
	
	@Override
	public Matrix generateCovarianceMatrix(final Matrix state)
	{
		final Matrix p = new Matrix(7, 7);
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
		Matrix a = Matrix.identity(3, 7);
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
		// double angle = angleIn;
		// angle = angle / (2.0 * Math.PI);
		// angle = angle - (int) angle;
		// if (angle < 0)
		// {
		// angle = 1 + angle;
		// }
		// if (angle < 0.5)
		// {
		// angle = 2 * angle * Math.PI;
		// } else
		// {
		// angle = -2 * (1 - angle) * Math.PI;
		// }
		// return angle;
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
		// final double angleDif = (angle1 - angle2) % (2 * Math.PI);
		// if (angleDif > Math.PI)
		// {
		// return angleDif - (2 * Math.PI);
		// }
		// if (angleDif < -Math.PI)
		// {
		// return angleDif + (2 * Math.PI);
		// }
		// return angleDif;
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
		final Matrix covar = new Matrix(7, 7);
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
	
	
	@Override
	public Matrix getDynamicsState(final Matrix fullState)
	{
		return fullState;
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
	
	
	/**
	 * @return the noRotationBorder
	 */
	public double getNoRotationBorder()
	{
		return noRotationBorder;
	}
}
