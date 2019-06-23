package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels;

import Jama.Matrix;

public class TigersMotionModel extends AOmniBot_V2 {

	@Override
	public Matrix dynamics(Matrix state, Matrix control, double dt)
	{
		double x			= state.get(0, 0);
		double y			= state.get(1, 0);
		double orient	= state.get(2, 0);
		double movAng	= state.get(3, 0);
		double v			= state.get(4, 0);
		double omega	= state.get(5, 0);
		double eta		= state.get(6, 0);
		
		double u_v		= control.get(0, 0);
		double u_omega	= control.get(1, 0);
		double u_eta	= control.get(2, 0);
		double u_ang   = control.get(3, 0);
		
		// dynamics
		if (Math.abs(omega) < noRotationBorder)
		{
			// straight movement
			x = x + v*Math.cos(movAng)*dt;
			y = y + v*Math.sin(movAng)*dt;
		}
		else
		{
			// circular movement
			double r = v/omega;
			x = x + r*(-Math.sin(movAng) + Math.sin(movAng+omega*dt));
			y = y + r*( Math.cos(movAng) - Math.cos(movAng+omega*dt));
		}
		orient= orient + (omega+eta)*dt;
		
		if (v > angleTakeVel)
			movAng = movAng + angleTakeFactor * determineAngleDifference(u_ang, movAng);
		else
		{
			movAng = movAng + ((1-v/angleTakeVel) * (1-angleTakeFactor) + angleTakeFactor) * determineAngleDifference(u_ang, movAng);
		//	System.out.println((1-v/angleTakeVel) * (1-angleTakeFactor) + angleTakeFactor);
		}
		
		movAng= movAng + omega*dt;
		v		= estimateVelocity(v, u_v, dt, botCtrl_maxAccel, botCtrl_maxBrakeAccel);
		omega	= estimateVelocity(omega, u_omega, dt, botCtrl_maxAngAccel, botCtrl_maxAngBrakeAccel);
		eta	= estimateVelocity(eta, u_eta, dt, botCtrl_maxAngAccel, botCtrl_maxAngBrakeAccel);
		
		// create return object
		Matrix f = new Matrix(7, 1);
		f.set(0, 0, x);
		f.set(1, 0, y);
		f.set(2, 0, orient);
		f.set(3, 0, movAng);
		f.set(4, 0, v);
		f.set(5, 0, omega);
		f.set(6, 0, eta);
		return f;
	}
}
 
