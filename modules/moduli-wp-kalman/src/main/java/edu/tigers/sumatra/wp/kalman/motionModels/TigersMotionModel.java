package edu.tigers.sumatra.wp.kalman.motionModels;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.kalman.data.AMotionResult;
import edu.tigers.sumatra.wp.kalman.data.OmnibotControl_V2;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V2;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 *
 */
public class TigersMotionModel extends AOmniBot_V2
{
	
	@Override
	public Matrix dynamics(final Matrix state, final Matrix control, final double dt, final MotionContext context)
	{
		
		double x = state.get(0, 0);
		double y = state.get(1, 0);
		double orient = state.get(2, 0);
		double movAng = state.get(3, 0);
		double v = state.get(4, 0);
		double omega = state.get(5, 0);
		double eta = state.get(6, 0);
		
		
		if (control == null)
		{
			// dynamics
			if (Math.abs(omega) < getNoRotationBorder())
			{
				// straight movement
				x = x + (v * Math.cos(movAng) * dt);
				y = y + (v * Math.sin(movAng) * dt);
			} else
			{
				// circular movement
				final double r = v / omega;
				x = x + (r * (-Math.sin(movAng) + Math.sin(movAng + (omega * dt))));
				y = y + (r * (Math.cos(movAng) - Math.cos(movAng + (omega * dt))));
			}
			orient = orient + ((omega) * dt);
			movAng = movAng + (omega * dt);
		} else
		{
			final double uV = control.get(0, 0);
			double uOmega = control.get(1, 0);
			final double uEta = control.get(2, 0);
			final double uAng = control.get(3, 0);
			// final double uA = control.get(4, 0);
			// final double aO = control.get(5, 0);
			
			double a = getBotCtrlMaxAccel();
			double aBrake = getBotCtrlMaxBrakeAccel();
			double aAng = getBotCtrlMaxAngAccel();
			double aAngBrake = getBotCtrlMaxAngBrakeAccel();
			// a = Math.min(uA, a);
			// aBrake = Math.min(uA, aBrake);
			// aAng = Math.min(aO, aAng);
			// aAngBrake = Math.min(aO, aAngBrake);
			
			// dynamics
			if (Math.abs(omega) < getNoRotationBorder())
			{
				// straight movement
				x = x + (v * Math.cos(movAng) * dt);
				y = y + (v * Math.sin(movAng) * dt);
			} else
			{
				// circular movement
				final double r = v / omega;
				x = x + (r * (-Math.sin(movAng) + Math.sin(movAng + (omega * dt))));
				y = y + (r * (Math.cos(movAng) - Math.cos(movAng + (omega * dt))));
			}
			orient = orient + ((omega) * dt);
			
			double dAng = determineAngleDifference(uAng, movAng);
			if (v > getAngleTakeVel())
			{
				movAng = movAng + (getAngleTakeFactor() * dAng);
			} else
			{
				double p = 1 - (v / getAngleTakeVel());
				double q = 1 - getAngleTakeFactor();
				double o = (p * q) + getAngleTakeFactor();
				movAng = movAng + (o * dAng);
			}
			
			movAng = movAng + (omega * dt);
			// System.out.printf("%.3f %.3f\n", movAng, orient);
			v = estimateVelocity(v, uV, dt, a, aBrake);
			omega = estimateVelocity(omega, uOmega, dt, aAng, aAngBrake);
			eta = estimateVelocity(eta, uEta, dt, aAng, aAngBrake);
			// System.out.printf("%.3f %.3f\n", eta, uEta);
		}
		
		// create return object
		final Matrix f = new Matrix(7, 1);
		f.set(0, 0, x);
		f.set(1, 0, y);
		f.set(2, 0, orient);
		f.set(3, 0, movAng);
		f.set(4, 0, v);
		f.set(5, 0, omega);
		f.set(6, 0, eta);
		return f;
	}
	
	
	/**
	 * @param bot
	 * @param oldState
	 * @param newBot
	 * @param dt
	 */
	@Override
	public void estimateControl(final IFilter bot, final AMotionResult oldState, final CamRobot newBot,
			final CamRobot lastBot,
			final double dt)
	{
		RobotMotionResult_V2 newState = (RobotMotionResult_V2) bot.getPrediction(bot.getTimestamp());
		
		final double oldX = oldState.x;
		final double oldY = oldState.y;
		final double oldTheta = ((RobotMotionResult_V2) (oldState)).orientation;
		
		final double newX = newState.x;
		final double newY = newState.y;
		final double newTheta = newState.orientation;
		
		final double sinOri = Math.sin(oldTheta);
		final double cosOri = Math.cos(oldTheta);
		
		// Determine new v_x and v_y
		final double dX = (newX - oldX);
		final double dY = (newY - oldY);
		
		final double vT = ((cosOri * dX) + (sinOri * dY)) / dt;
		final double vO = ((-sinOri * dX) + (cosOri * dY)) / dt;
		
		// Determine new omega
		double dOmega = AngleMath.difference(newTheta, oldTheta);
		double omega = dOmega / dt;
		
		// Determine new eta
		final double eta = 0.0 / dt;
		
		double velMeas = newBot.getPos().subtractNew(new Vector2(oldState.x, oldState.y)).getLength() / dt;
		double vel = new Vector2(newState.x, newState.y).subtract(new Vector2(oldState.x, oldState.y)).getLength() / dt;
		double acc = (velMeas - vel) / dt;
		final double aT = (cosOri * acc);
		final double aO = (-sinOri * acc);
		
		bot.setControl(new OmnibotControl_V2(vT, vO, omega, eta, aT, aO));
	}
}
