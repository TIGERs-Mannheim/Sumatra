/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory.flat;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory.TwoPhaseDynamicVelParameters;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
public class TwoPhaseDynamicVelConsultant implements IStraightBallConsultant
{
	
	private final TwoPhaseDynamicVelParameters parameters;
	
	
	/**
	 * Create a new consultant for the dynamic vel ball model
	 * 
	 * @param parameters
	 */
	public TwoPhaseDynamicVelConsultant(final TwoPhaseDynamicVelParameters parameters)
	{
		this.parameters = parameters;
	}
	
	
	@Override
	public double getInitVelForTime(final double endVel, final double time)
	{
		// assume endVel is in sliding phase
		double initialVel = endVel - 1e-3 * parameters.getAccSlide() * time;
		double timeSwitchUnderAssumption = (initialVel * parameters.getKSwitch() - initialVel)
				/ (1e-3 * parameters.getAccSlide());
		if (timeSwitchUnderAssumption < time)
		{
			initialVel = (1e-3 * parameters.getAccSlide() * endVel
					- 1e-3 * parameters.getAccRoll() * 1e-3 * parameters.getAccSlide() * time)
					/ (1e-3 * (parameters.getAccSlide() - parameters.getAccRoll()) * parameters.getKSwitch()
							+ 1e-3 * parameters.getAccRoll());
		}
		return initialVel;
	}
	
	
	@Override
	public double getInitVelForDist(final double distance, final double endVel)
	{
		double initialVel = sqrt(pow(endVel, 2) - 2e-6 * parameters.getAccSlide() * distance);
		double switchVel = initialVel * parameters.getKSwitch();
		if (switchVel > endVel)
		{
			double root1 = (1e-3 * parameters.getAccSlide() * pow(endVel, 2))
					/ (1e-3 * parameters.getAccSlide() * pow(parameters.getKSwitch(), 2)
							- 1e-3 * parameters.getAccRoll() * pow(parameters.getKSwitch(), 2)
							+ 1e-3 * parameters.getAccRoll());
			double root2 = (2e-9 * parameters.getAccRoll() * parameters.getAccSlide() * distance)
					/ (1e-3 * parameters.getAccSlide() * pow(parameters.getKSwitch(), 2)
							- 1e-3 * parameters.getAccRoll() * pow(parameters.getKSwitch(), 2)
							+ 1e-3 * parameters.getAccRoll());
			initialVel = sqrt(root1 - root2);
		}
		return initialVel;
	}
	
	
	@Override
	public double getTimeForKick(final double distance, final double kickVel)
	{
		return TwoPhaseDynamicVelBallTrajectory
				.fromKick(Vector2.ZERO_VECTOR, Vector3.fromXYZ(1e3, 0, 0).multiplyNew(kickVel), parameters)
				.getTimeByDist(distance);
	}
	
	
	@Override
	public double getInitVelForTimeDist(final double distance, final double time)
	{
		double initialVel;
		double as = parameters.getAccSlide();
		double ar = parameters.getAccRoll();
		double c = parameters.getKSwitch();
		double t = time;
		double s = distance;
		double det = ((pow(as, 2) - ar * as) * pow(c, 2) + ar * as) * pow(t, 2)
						+ ((2 * ar - 2 * as) * pow(c, 2) + (4 * as - 4 * ar) * c - 2 * as + 2 * ar) * s;
		if (det >= 0) {
			double v01 = (as * sqrt(det)
					+ ((pow(as, 2) - ar * as) * c + ar * as) * t) / ((as - ar) * pow(c, 2) + (2 * ar - 2 * as) * c + as - ar);
			double v02 = -(as * sqrt(det)
					+ ((ar * as - pow(as, 2)) * c - ar * as) * t) / ((as - ar) * pow(c, 2) + (2 * ar - 2 * as) * c + as - ar);
			initialVel = max(v01, v02);
		} else {
			initialVel = s / t - .5 * ar * t;
		}
		return 1e-3 * initialVel;
		
	}
}
