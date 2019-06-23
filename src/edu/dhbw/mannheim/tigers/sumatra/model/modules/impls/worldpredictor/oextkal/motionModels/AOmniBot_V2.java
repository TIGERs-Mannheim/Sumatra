package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.OmnibotControl_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBot;


/**
 */
public abstract class AOmniBot_V2 implements IMotionModel
{
	// :::>>> (export)
	/** rad/s */
	private static final double	BASE_NO_RPTATION_BORDER			= 0.1;
	/** m/s */
	private static final double	BASE_NO_MOVEMENT_BORDER			= 0.0001;
	
	/** m */
	private static final double	BASE_STDEV_POSITION				= 0.02;
	/** m/s */
	private static final double	BASE_STDEV_VELOCITY				= 0.00005;
	/** rad */
	private static final double	BASE_STDEV_ORIENTATION			= 0.08;
	/** rad/s */
	private static final double	BASE_STDEV_ANG_VELOCITY			= 1.0;
	
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_ACCEL				= 1.0;
	/** m/s^2 */
	private static final double	BASE_CRTL_MAX_BRAKE_ACCEL		= 3.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_ACCEL			= 10.0;
	/** rad/s^2 */
	private static final double	BASE_CRTL_MAX_ANG_BRAKE_ACCEL	= 15.0;
	private static final double	ACCEL_RAMP_PERCENT_SEC1			= 0.1;
	private static final double	ACCEL_RAMP_PERCENT_SEC2			= 0.7;
	private static final double	ACCEL_RAMP_PERCENT_SEC3			= 0.2;
	
	/** m/s */
	private static final double	BASE_ANGLE_TAKE_VEL				= 0.5;
	private static final double	ANGLE_TAKE_FACTOR					= 0.05;
	// :::<<<
	
	private final double				noRotationBorder;
	private final double				noMovementBorder;
	
	private final double				varPosition;
	private final double				varVelocity;
	private final double				varOrientation;
	private final double				varAngVelocity;
	
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
		varVelocity = Math.pow(BASE_STDEV_VELOCITY * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		varOrientation = Math.pow(BASE_STDEV_ORIENTATION, 2);
		varAngVelocity = Math.pow(BASE_STDEV_ANG_VELOCITY * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, 2);
		
		botCtrlMaxAccel = BASE_CRTL_MAX_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxBrakeAccel = BASE_CRTL_MAX_BRAKE_ACCEL * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		botCtrlMaxAngAccel = BASE_CRTL_MAX_ANG_ACCEL * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		botCtrlMaxAngBrakeAccel = BASE_CRTL_MAX_ANG_BRAKE_ACCEL * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		angleTakeVel = BASE_ANGLE_TAKE_VEL * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
	}
	
	
	// --------------------------------------------------------------------------
	// --- interface methods ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public Matrix sample(Matrix state, Matrix control)
	{
		// TODO WP: implement if using particlefilter with robots
		return null;
	}
	
	
	@Override
	public double transitionProbability(Matrix stateNew, Matrix stateOld, Matrix control)
	{
		// TODO WP: implement if using particlefilter with robots
		return 1;
	}
	
	
	@Override
	public double measurementProbability(Matrix state, Matrix measurement, double dt)
	{
		// TODO WP: implement if using particlefilter with robots
		return 1;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTstate(Matrix state, double dt)
	{
		final double movAng = state.get(3, 0);
		final double v = state.get(4, 0);
		final double omega = state.get(5, 0);
		
		final double sinMovAng = Math.sin(movAng);
		final double cosMovAng = Math.cos(movAng);
		
		final Matrix a = new Matrix(7, 7);
		if ((Math.abs(omega) < getNoRotationBorder()) || (Math.abs(v) < noMovementBorder))
		{
			// straight movement
			a.set(0, 0, 1.0);
			a.set(0, 3, -v * sinMovAng * dt);
			a.set(0, 4, cosMovAng * dt);
			a.set(1, 1, 1.0);
			a.set(1, 3, v * cosMovAng * dt);
			a.set(1, 4, sinMovAng * dt);
		} else
		{
			// circular movement
			final double r = v / omega;
			final double rotatedMovAng = movAng + (omega * dt);
			final double sinRot = Math.sin(rotatedMovAng);
			final double cosRot = Math.cos(rotatedMovAng);
			
			a.set(0, 0, 1.0);
			a.set(0, 3, r * (cosRot - cosMovAng));
			a.set(0, 4, (sinRot - sinMovAng) / omega);
			a.set(0, 5, (v * (-sinRot + sinMovAng + (cosRot * omega * dt))) / (omega * omega));
			a.set(1, 1, 1.0);
			a.set(1, 3, r * (sinRot - sinMovAng));
			a.set(1, 4, (-cosRot + cosMovAng) / omega);
			a.set(1, 5, (v * ((cosRot - cosMovAng) + (sinRot * omega * dt))) / (omega * omega));
		}
		a.set(2, 2, 1.0);
		a.set(2, 5, dt);
		a.set(2, 6, dt);
		a.set(3, 3, 1.0);
		a.set(3, 5, dt);
		a.set(4, 4, 1.0);
		a.set(5, 5, 1.0);
		a.set(6, 6, 1.0);
		return a;
	}
	
	
	@Override
	public Matrix getDynamicsJacobianWRTnoise(Matrix state, double dt)
	{
		return Matrix.identity(7, 7);
	}
	
	
	@Override
	public Matrix getDynamicsCovariance(Matrix state, double dt)
	{
		final Matrix q = new Matrix(7, 7);
		q.set(0, 0, Math.pow(getBotCtrlMaxBrakeAccel() * (dt * dt), 2));
		q.set(1, 1, Math.pow(getBotCtrlMaxBrakeAccel() * (dt * dt), 2));
		q.set(2, 2, Math.pow(getBotCtrlMaxAngBrakeAccel() * (dt * dt), 2));
		q.set(3, 3, Math.pow(getBotCtrlMaxAngBrakeAccel() * (dt * dt), 2));
		q.set(4, 4, Math.pow(getBotCtrlMaxBrakeAccel() * dt, 2));
		q.set(5, 5, Math.pow(getBotCtrlMaxAngBrakeAccel() * dt, 2));
		q.set(6, 6, Math.pow(getBotCtrlMaxAngBrakeAccel() * dt, 2));
		return q;
	}
	
	
	@Override
	public AMotionResult generateMotionResult(int id, Matrix state, boolean onCam)
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
	public Matrix generateMeasurementMatrix(AWPCamObject observation, Matrix state)
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
			o.set(2, 0, determineContinuousAngle(state.get(2, 0), obs.orientation));
		}
		return o;
	}
	
	
	@Override
	public Matrix generateStateMatrix(Matrix measurement, Matrix control)
	{
		final Matrix s = new Matrix(7, 1);
		s.set(0, 0, measurement.get(0, 0));
		s.set(1, 0, measurement.get(1, 0));
		s.set(2, 0, measurement.get(2, 0));
		s.set(3, 0, measurement.get(2, 0));
		s.set(4, 0, control.get(0, 0));
		s.set(5, 0, control.get(1, 0));
		s.set(6, 0, control.get(2, 0));
		return s;
	}
	
	
	@Override
	public Matrix updateStateOnNewControl(IControl control, Matrix state)
	{
		// OmnibotControl_V2 ctrl = (OmnibotControl_V2) control;
		// double theta = Math.atan2(ctrl.vo, ctrl.vt);
		// double movAng = state.get(2,0)+theta;
		// state.set(3, 0, movAng);
		return state;
	}
	
	
	@Override
	public Matrix updateCovarianceOnNewControl(IControl control, Matrix covariance)
	{
		covariance.set(3, 0, 0.0);
		covariance.set(3, 1, 0.0);
		covariance.set(3, 2, 0.0);
		covariance.set(3, 3, 1.0);
		covariance.set(3, 4, 0.0);
		covariance.set(3, 5, 0.0);
		covariance.set(3, 6, 0.0);
		
		covariance.set(0, 3, 0.0);
		covariance.set(1, 3, 0.0);
		covariance.set(2, 3, 0.0);
		covariance.set(4, 3, 0.0);
		covariance.set(5, 3, 0.0);
		covariance.set(6, 3, 0.0);
		return covariance;
	}
	
	
	@Override
	public Matrix generateControlMatrix(IControl control, Matrix state)
	{
		final OmnibotControl_V2 ctrl = (OmnibotControl_V2) control;
		final Matrix u = new Matrix(4, 1);
		if (control != null)
		{
			final double v = Math.sqrt((ctrl.vt * ctrl.vt) + (ctrl.vo * ctrl.vo));
			u.set(0, 0, v);
			u.set(1, 0, ctrl.omega);
			u.set(2, 0, ctrl.eta);
			u.set(3, 0, Math.atan2(ctrl.vo, ctrl.vt) + state.get(2, 0));
		}
		return u;
	}
	
	
	@Override
	public Matrix generateCovarianceMatrix(Matrix state)
	{
		final Matrix p = new Matrix(7, 7);
		p.set(0, 0, varPosition);
		p.set(1, 1, varPosition);
		p.set(2, 2, varOrientation);
		p.set(3, 3, varOrientation);
		p.set(4, 4, varVelocity);
		p.set(5, 5, varAngVelocity);
		p.set(6, 6, varAngVelocity);
		return p;
	}
	
	
	@Override
	public int extraxtObjectID(AWPCamObject observation)
	{
		if (observation instanceof WPCamBot)
		{
			return ((WPCamBot) observation).id;
		}
		throw new IllegalStateException("observation could not be casted");
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
		return Matrix.identity(3, 7);
	}
	
	
	@Override
	public Matrix getMeasurementJacobianWRTnoise(Matrix state)
	{
		return Matrix.identity(3, 3);
	}
	
	
	@Override
	public Matrix getMeasurementCovariance(Matrix measurement)
	{
		final Matrix r = new Matrix(3, 3);
		r.set(0, 0, varPosition);
		r.set(1, 1, varPosition);
		r.set(2, 2, varOrientation);
		return r;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected double normalizeAngle(double angleIn)
	{
		double angle = angleIn;
		angle = angle / (2 * Math.PI);
		angle = angle - (int) angle;
		if (angle < 0)
		{
			angle = 1 + angle;
		}
		if (angle < 0.5)
		{
			angle = 2 * angle * Math.PI;
		} else
		{
			angle = -2 * (1 - angle) * Math.PI;
		}
		return angle;
	}
	
	
	protected double determineContinuousAngle(double oldAngle, double newAngle)
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
	
	
	protected double determineAngleDifference(double angle1, double angle2)
	{
		final double angleDif = (angle1 - angle2) % (2 * Math.PI);
		if (angleDif > Math.PI)
		{
			return angleDif - (2 * Math.PI);
		}
		if (angleDif < -Math.PI)
		{
			return angleDif + (2 * Math.PI);
		}
		return angleDif;
	}
	
	
	protected double estimateVelocity(double current, double target, double dt, double maxAcc, double maxBrake)
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
					tCurrent = current * (((1 / 3.0) * accEndT) / (ACCEL_RAMP_PERCENT_SEC1 * target));
				} else if (currentFactor < (ACCEL_RAMP_PERCENT_SEC1 + ACCEL_RAMP_PERCENT_SEC2))
				{
					tCurrent = ((current - (ACCEL_RAMP_PERCENT_SEC1 * target)) * (((1 / 3.0) * accEndT) / ((ACCEL_RAMP_PERCENT_SEC2) * target)))
							+ (accEndT / 3.0);
				} else
				{
					tCurrent = ((current - (ACCEL_RAMP_PERCENT_SEC1 * target) - (ACCEL_RAMP_PERCENT_SEC2 * target)) * (((1 / 3.0) * accEndT) / (ACCEL_RAMP_PERCENT_SEC3 * target)))
							+ ((2 * accEndT) / 3.0);
				}
				
				final double tFinal = tCurrent + dt;
				if (tFinal < ((1 / 3.0) * accEndT))
				{
					vel = ((ACCEL_RAMP_PERCENT_SEC1 * target) / ((1 / 3.0) * accEndT)) * tFinal;
				} else if (tFinal < ((2 / 3.0) * accEndT))
				{
					vel = ((((ACCEL_RAMP_PERCENT_SEC2 * target) / ((1 / 3.0) * accEndT)) * tFinal) - (ACCEL_RAMP_PERCENT_SEC2 * target))
							+ (ACCEL_RAMP_PERCENT_SEC1 * target);
				} else if (tFinal < accEndT)
				{
					vel = (((ACCEL_RAMP_PERCENT_SEC3 * target) / ((1 / 3.0) * accEndT)) * (tFinal - ((2 / 3.0) * accEndT)))
							+ ((1 - ACCEL_RAMP_PERCENT_SEC3) * target);
					// - botCtrl_percentThird3*target + (botCtrl_percentThird1+botCtrl_percentThird2)*target;
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
	public Matrix getStateOnNoObservation(Matrix state)
	{
		state.set(3, 0, state.get(2, 0));
		state.set(4, 0, 0.0);
		state.set(5, 0, 0.0);
		state.set(6, 0, 0.0);
		return state;
	}
	
	
	@Override
	public Matrix getCovarianceOnNoObservation(Matrix covariance)
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
	public Matrix getControlOnNoObservation(Matrix control)
	{
		return generateControlMatrix(null, null);
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
