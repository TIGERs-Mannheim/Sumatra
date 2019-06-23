package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.OmnibotControl_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBot;

public abstract class AOmniBot_V2 implements IMotionModel {
	//:::>>> (export)
	final double base_noRotationBorder	= 0.1;		// rad/s
	final double base_noMovementBorder	= 0.0001;	// m/s
	
	final double base_stDevPosition		= 0.02;		// m
	final double base_stDevVelocity		= 0.00005;	// m/s
	final double base_stDevOrientation	= 0.08;		// rad
	final double base_stDevAngVelocity	= 1.0;		// rad/s
	
	final double base_ctrl_maxAccel		= 1.0;		// m/s^2
	final double base_ctrl_maxBrakeAccel= 3.0;		// m/s^2
	final double base_ctrl_maxAngAccel	= 10.0;		// rad/s^2
	final double base_ctrl_maxAngBrakeAccel = 15.0;	// rad/s^2
	final double accelRamp_percent_sec1	= 0.1;
	final double accelRamp_percent_sec2	= 0.7;
	final double accelRamp_percent_sec3	= 0.2;
	
	final double base_angleTakeVel            = 0.5;  // m/s,
	final double angleTakeFactor = 0.05;
	//:::<<<
	
	final double noRotationBorder;
	final double noMovementBorder;
	
	final double varPosition;
	final double varVelocity;
	final double varOrientation;
	final double varAngVelocity;
	
	final double botCtrl_maxAccel;
	final double botCtrl_maxBrakeAccel;
	final double botCtrl_maxAngAccel;
	final double botCtrl_maxAngBrakeAccel;
	
	final double angleTakeVel;

	AOmniBot_V2()
	{
		this.noRotationBorder = base_noRotationBorder * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal;
		this.noMovementBorder = base_noMovementBorder * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		this.varPosition		= Math.pow(base_stDevPosition * WPConfig.FILTER_CONVERT_M_TO_INTERNAL_UNIT, 2);
		this.varVelocity		= Math.pow(base_stDevVelocity * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, 2);
		this.varOrientation	= Math.pow(base_stDevOrientation, 2);
		this.varAngVelocity	= Math.pow(base_stDevAngVelocity * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, 2);
		
		this.botCtrl_maxAccel 		= base_ctrl_maxAccel * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		this.botCtrl_maxBrakeAccel	= base_ctrl_maxBrakeAccel * WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		this.botCtrl_maxAngAccel	= base_ctrl_maxAngAccel * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		this.botCtrl_maxAngBrakeAccel = base_ctrl_maxAngBrakeAccel * WPConfig.FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ;
		this.angleTakeVel    = base_angleTakeVel * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		}

	// --------------------------------------------------------------------------
	// --- interface methods ----------------------------------------------------
	// --------------------------------------------------------------------------

	@Override
	public Matrix sample(Matrix state, Matrix control)
	{
		//TODO WP: implement if using particlefilter with robots
		return null;
	}

	@Override
	public double transitionProbability(Matrix stateNew, Matrix stateOld, Matrix control)
	{
		//TODO WP: implement if using particlefilter with robots
		return 1;
	}

	@Override
	public double measurementProbability(Matrix state, Matrix measurement, double dt)
	{
		//TODO WP: implement if using particlefilter with robots
		return 1;
	}

	@Override
	public Matrix getDynamicsJacobianWRTstate(Matrix state, double dt)
	{
		double movAng	= state.get(3, 0);
		double v			= state.get(4, 0);
		double omega	= state.get(5, 0);
				
		double sinMovAng	= Math.sin(movAng);
		double cosMovAng	= Math.cos(movAng);
		
		Matrix a = new Matrix(7, 7);
		if (Math.abs(omega) < noRotationBorder || Math.abs(v) < noMovementBorder)
		{
			// straight movement
			a.set(0, 0, 1.0);
			a.set(0, 3, -v*sinMovAng*dt);
			a.set(0, 4, cosMovAng*dt);
			a.set(1, 1, 1.0);
			a.set(1, 3, v*cosMovAng*dt);
			a.set(1, 4, sinMovAng*dt);
		}
		else
		{
			// circular movement
			double r = v/omega;
			double rotatedMovAng = movAng + omega * dt;
			double sinRot = Math.sin(rotatedMovAng);
			double cosRot = Math.cos(rotatedMovAng);
			
			a.set(0, 0, 1.0);
			a.set(0, 3, r*(cosRot-cosMovAng));
			a.set(0, 4, ( sinRot-sinMovAng) / omega);
			a.set(0, 5, (v*(-sinRot+sinMovAng+cosRot*omega*dt)) / (omega*omega));
			a.set(1, 1, 1.0);
			a.set(1, 3, r*(sinRot-sinMovAng));
			a.set(1, 4, (-cosRot+cosMovAng) / omega);
			a.set(1, 5, (v*( cosRot-cosMovAng+sinRot*omega*dt)) / (omega*omega));
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
		Matrix w = Matrix.identity(7, 7);
		return w;
	}

	@Override
	public Matrix getDynamicsCovariance(Matrix state, double dt)
	{
		Matrix q = new Matrix(7, 7);
		q.set(0, 0, Math.pow(botCtrl_maxBrakeAccel*(dt*dt), 2));
		q.set(1, 1, Math.pow(botCtrl_maxBrakeAccel*(dt*dt), 2));
		q.set(2, 2, Math.pow(botCtrl_maxAngBrakeAccel*(dt*dt), 2));
		q.set(3, 3, Math.pow(botCtrl_maxAngBrakeAccel*(dt*dt), 2));
		q.set(4, 4, Math.pow(botCtrl_maxBrakeAccel*dt, 2));
		q.set(5, 5, Math.pow(botCtrl_maxAngBrakeAccel*dt, 2));
		q.set(6, 6, Math.pow(botCtrl_maxAngBrakeAccel*dt, 2));
		return q;
	}

	@Override
	public AMotionResult generateMotionResult(int id, Matrix state, boolean onCam)
	{
		double x = state.get(0, 0);
		double y = state.get(1, 0);
		double orientation = normalizeAngle(state.get(2, 0));
		double movementAngle = normalizeAngle(state.get(3, 0));
		double v = state.get(4, 0);
		double omega = state.get(5, 0);
		double eta = state.get(6, 0);
		double confidence = 1.0;
		return new RobotMotionResult_V2(x, y, orientation, movementAngle, v,
				omega, eta, confidence, onCam);
	}

	@Override
	public Matrix generateMeasurementMatrix(AWPCamObject observation, Matrix state)
	{
		WPCamBot obs = (WPCamBot) observation;
		Matrix o = new Matrix(3,1);
		o.set(0, 0, obs.x);
		o.set(1, 0, obs.y);
		if (state == null)
		{
			o.set(2, 0, obs.orientation);
		}
		else
		{
			o.set(2, 0, determineContinuousAngle(state.get(2, 0), obs.orientation));
		}
		return o;
	}

	@Override
	public Matrix generateStateMatrix(Matrix measurement, Matrix control)
	{
		Matrix s = new Matrix(7,1);
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
//		OmnibotControl_V2 ctrl = (OmnibotControl_V2) control;
//		double theta = Math.atan2(ctrl.vo, ctrl.vt);
//		double movAng = state.get(2,0)+theta;
//		state.set(3, 0, movAng);
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
		OmnibotControl_V2 ctrl = (OmnibotControl_V2) control;
		Matrix u = new Matrix(4,1);
		if (control != null)
		{
			double v = Math.sqrt(ctrl.vt*ctrl.vt + ctrl.vo*ctrl.vo);
			u.set(0, 0, v);
			u.set(1, 0, ctrl.omega);
			u.set(2, 0, ctrl.eta);
			u.set(3, 0, Math.atan2(ctrl.vo, ctrl.vt)+state.get(2, 0));
		}
		return u;
	}

	@Override
	public Matrix generateCovarianceMatrix(Matrix state)
	{
		Matrix p = new Matrix(7,7);
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
		WPCamBot obs = (WPCamBot) observation;
		return obs.id;
	}

	@Override
	public Matrix measurementDynamics(Matrix state)
	{
		Matrix m = new Matrix(3, 1);
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
		Matrix r = new Matrix(3, 3);
		r.set(0, 0, varPosition);
		r.set(1, 1, varPosition);
		r.set(2, 2, varOrientation);
		return r;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected double normalizeAngle(double angle)
	{
		angle = angle / (2*Math.PI);
		angle = angle - (int) angle;
		if (angle < 0)
		{
			angle = 1 + angle;
		}
		if (angle < 0.5)
		{
			angle = 2*angle * Math.PI;
		} else
		{
			angle = -2*(1-angle) * Math.PI;
		}
		return angle;
	}
	
	protected double determineContinuousAngle(double oldAngle, double newAngle)
	{
		// project new orientation next to old one
		// thus values greater than PI are possible but the handling gets easier
		
		double dAngle = newAngle - normalizeAngle(oldAngle);	//standard: just a small rotation
		if (dAngle > Math.PI)				//rotation clockwise over Pi-border
		{
			dAngle = dAngle - 2*Math.PI;
		} 
		else if(dAngle < -Math.PI)			//rotation counter-clockwise over Pi-border
		{
			dAngle = dAngle + 2*Math.PI;
		}
		return oldAngle + dAngle;
	}
	
	protected double determineAngleDifference(double angle1, double angle2)
	{
		double angleDif = (angle1-angle2) % (2*Math.PI);
		if (angleDif > Math.PI)
		{
			return angleDif-2*Math.PI;
		}
		if (angleDif < -Math.PI)
		{
			return angleDif + 2*Math.PI;
		}
		return angleDif;
	}
	
	protected double estimateVelocity(double current, double target, double dt,
			double maxAcc, double maxBrake)
	{
		double vel = 0.0;
		if ((Math.signum(current) == Math.signum(target))
				|| current == 0.0 || target == 0.0)
		{
			// just accelerating or braking
			
			boolean negative = false;
			if (current < 0.0 || target < 0.0)
			{
				negative = true;
				current = -current;
				target = -target;
			}
			
			if (current < target)
			{
				// accelerating
				double accEndT = target/maxAcc;
				double currentFactor = current/target;
				double t_current = 0.0;
				if(currentFactor < accelRamp_percent_sec1)
				{
					t_current = 
						current * (((1/3.0)*accEndT)/(accelRamp_percent_sec1*target));
				}
				else if (currentFactor < accelRamp_percent_sec1+accelRamp_percent_sec2)
				{
					t_current = 
						(current-accelRamp_percent_sec1*target)
							* (((1/3.0)*accEndT)/((accelRamp_percent_sec2)*target)) 
						+ accEndT/3.0;					
				}
				else
				{
					t_current = 
						(current-accelRamp_percent_sec1*target-accelRamp_percent_sec2*target)
							* (((1/3.0)*accEndT)/(accelRamp_percent_sec3*target)) 
						+ 2*accEndT/3.0;
				}
				
				double t_final = t_current + dt;
				if(t_final < (1/3.0)*accEndT)
				{
					vel = ((accelRamp_percent_sec1*target)/((1/3.0)*accEndT))*t_final;
				}
				else if (t_final < (2/3.0)*accEndT)
				{
					vel = ((accelRamp_percent_sec2*target)/((1/3.0)*accEndT))*t_final
						- accelRamp_percent_sec2*target + accelRamp_percent_sec1*target;
				}
				else if (t_final < accEndT)
				{
					vel = ((accelRamp_percent_sec3*target)/((1/3.0)*accEndT))*(t_final-2/3.0*accEndT)
					+ (1-accelRamp_percent_sec3)*target;
					//- botCtrl_percentThird3*target + (botCtrl_percentThird1+botCtrl_percentThird2)*target;
				}
				else
				{
					vel = target;
				}
			}
			else
			{
				// braking
				vel = current - maxBrake*dt;
				if (vel < target)
				{
					vel = target;
				}				
			}
			
			if (negative)
			{
				vel = -vel;
			}
		}
		else
		{
			// first brake, than accelerate
			double t_toHalt = Math.abs(current)/maxBrake;
			if (t_toHalt >= dt)
			{
				// we only brake in our dt
				vel = estimateVelocity(current, 0.0, dt, maxAcc, maxBrake);
			}
			else
			{
				// we brake to 0 and accelerate then. 
				// here we only have to handle the acceleration.
				dt = dt - t_toHalt;
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
		double improbable = 1e10;
		Matrix covar = new Matrix(7,7);
		for (int i=0; i < covar.getRowDimension(); i++)
		{
			for (int j=0; j < covar.getColumnDimension(); j++)
			{
				covar.set(i, j, improbable);
			}
		}
		return covar;
	}

	@Override
	public Matrix getControlOnNoObservation(Matrix control)
	{
		Matrix ctrl = generateControlMatrix(null, null);
		return ctrl;
	}
}
