/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory.flat;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.trajectory.IFlatBallConsultant;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Value;
import lombok.With;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.pow;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
@Value
public class FlatBallConsultant implements IFlatBallConsultant
{
	@With
	BallParameters parameters;


	@Override
	public double getInitVelForTime(final double endVel, final double time)
	{
		// assume endVel is in sliding phase
		double initialVel = endVel - 1e-3 * parameters.getAccSlide() * time;
		double timeSwitchUnderAssumption = (initialVel * getKSwitch() - initialVel)
				/ (1e-3 * parameters.getAccSlide());
		if (timeSwitchUnderAssumption < time)
		{
			initialVel = (1e-3 * parameters.getAccSlide() * endVel
					- 1e-3 * parameters.getAccRoll() * 1e-3 * parameters.getAccSlide() * time)
					/ (1e-3 * (parameters.getAccSlide() - parameters.getAccRoll()) * getKSwitch()
					+ 1e-3 * parameters.getAccRoll());
		}
		return initialVel;
	}


	@Override
	public double getInitVelForDist(final double distance, final double endVel)
	{
		double initialVel = sqrt(pow(endVel, 2) - 2e-6 * parameters.getAccSlide() * distance);
		double switchVel = initialVel * getKSwitch();
		if (switchVel > endVel)
		{
			double root1 = (1e-3 * parameters.getAccSlide() * pow(endVel, 2))
					/ (1e-3 * parameters.getAccSlide() * pow(getKSwitch(), 2)
					- 1e-3 * parameters.getAccRoll() * pow(getKSwitch(), 2)
					+ 1e-3 * parameters.getAccRoll());
			double root2 = (2e-9 * parameters.getAccRoll() * parameters.getAccSlide() * distance)
					/ (1e-3 * parameters.getAccSlide() * pow(getKSwitch(), 2)
					- 1e-3 * parameters.getAccRoll() * pow(getKSwitch(), 2)
					+ 1e-3 * parameters.getAccRoll());
			initialVel = sqrt(root1 - root2);
		}
		return initialVel;
	}


	@Override
	public double getTimeForKick(final double distance, final double kickVel)
	{
		return FlatBallTrajectory
				.fromKick(parameters, Vector2f.ZERO_VECTOR, Vector2.fromXY(1e3, 0).multiplyNew(kickVel),
						Vector2f.ZERO_VECTOR)
				.getTimeByDist(distance);
	}


	@Override
	public double getVelForKickByTime(final double kickSpeed, final double travelTime)
	{
		return FlatBallTrajectory
				.fromKick(parameters, Vector2f.ZERO_VECTOR, Vector2.fromXY(1e3, 0).multiplyNew(kickSpeed),
						Vector2f.ZERO_VECTOR)
				.getVelByTime(travelTime)
				.getLength2();
	}


	@Override
	public double getInitVelForTimeDist(final double distance, final double time)
	{
		double initialVel;
		double as = parameters.getAccSlide();
		double ar = parameters.getAccRoll();
		double c = getKSwitch();
		double t = time;
		double s = distance;
		double det = ((pow(as, 2) - ar * as) * pow(c, 2) + ar * as) * pow(t, 2)
				+ ((2 * ar - 2 * as) * pow(c, 2) + (4 * as - 4 * ar) * c - 2 * as + 2 * ar) * s;
		if (det >= 0)
		{
			double v01 = (as * sqrt(det)
					+ ((pow(as, 2) - ar * as) * c + ar * as) * t)
					/ ((as - ar) * pow(c, 2) + (2 * ar - 2 * as) * c + as - ar);
			double v02 = -(as * sqrt(det)
					+ ((ar * as - pow(as, 2)) * c - ar * as) * t)
					/ ((as - ar) * pow(c, 2) + (2 * ar - 2 * as) * c + as - ar);
			initialVel = max(v01, v02);
		} else
		{
			initialVel = s / t - .5 * ar * t;
		}
		return 1e-3 * initialVel;
	}


	private double getKSwitch()
	{
		// this is only valid for zero initial spin!
		return 1.0 / (1.0 + parameters.getInertiaDistribution());
	}
}
