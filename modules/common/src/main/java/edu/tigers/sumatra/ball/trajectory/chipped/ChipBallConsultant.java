/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ball.trajectory.chipped;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.trajectory.IChipBallConsultant;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang.Validate;

import java.util.List;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Value
@AllArgsConstructor
public class ChipBallConsultant implements IChipBallConsultant
{
	double chipAngle;

	@With
	BallParameters params;


	/**
	 * Constructor.
	 *
	 * @param params
	 */
	public ChipBallConsultant(final BallParameters params)
	{
		this.params = params;
		chipAngle = AngleMath.deg2rad(45);
	}


	IVector2 absoluteKickVelToVector(final double vel)
	{
		return Vector2.fromXY(SumatraMath.cos(chipAngle) * vel, SumatraMath.sin(chipAngle) * vel);
	}


	@Override
	public IVector3 speedToVel(double direction, double speed)
	{
		double length = SumatraMath.cos(chipAngle) * speed;
		double z = SumatraMath.sin(chipAngle) * speed;
		IVector2 flat = Vector2.fromAngleLength(direction, length);
		return Vector3.from2d(flat, z);
	}


	@Override
	public double botVelocityToChipFartherThanMaximumDistance(final double distance, final int numTouchdowns,
			final double absMaxVel)
	{
		IVector3 kickVel = speedToVel(0, absMaxVel * 1000);
		List<IVector2> touchdowns = ChipBallTrajectory
				.fromKick(params, Vector2f.ZERO_VECTOR, kickVel, Vector2f.ZERO_VECTOR)
				.getTouchdownLocations();
		if (!touchdowns.isEmpty())
		{
			int touchdown = Math.max(0, Math.min(touchdowns.size() - 1, numTouchdowns - 1));
			if (touchdowns.get(touchdown).getLength2() >= distance)
			{
				return 0;
			}
		}

		double initialVel = getInitVelForDistAtTouchdown(distance, numTouchdowns);
		return Math.abs((SumatraMath.sin(chipAngle) * initialVel) - (kickVel.x() / 1000));
	}


	@Override
	public ChipBallConsultant withChipAngle(final double chipAngle)
	{
		return new ChipBallConsultant(AngleMath.deg2rad(chipAngle), params);
	}


	@Override
	public double getInitVelForDistAtTouchdown(final double distance, final int numTouchdown)
	{
		final double g = 9810;

		double f = 0.0;
		for (int i = 0; i <= numTouchdown; i++)
		{
			final double dampXY;
			if (i == 0)
			{
				dampXY = 1.0;
			} else
			{
				dampXY = params.getChipDampingXYFirstHop() * Math.pow(params.getChipDampingXYOtherHops(), i - 1.0);
			}

			f += dampXY * Math.pow(params.getChipDampingZ(), i);
		}

		double denom = f * SumatraMath.cos(chipAngle) * SumatraMath.sin(chipAngle);
		Validate.isTrue(denom > 0);

		return SumatraMath.sqrt((distance * g * 0.5) / denom) * 0.001;
	}


	@Override
	public double getInitVelForPeakHeight(final double height)
	{
		final double g = 9810;

		// initial z velocity in [m/s]
		double velZ = SumatraMath.sqrt(2.0 * g * height) * 0.001;

		return velZ / SumatraMath.sin(chipAngle);
	}


	@Override
	public double getMinimumDistanceToOverChip(final double initVel, final double height)
	{
		final double g = 9.81;
		double heightInM = height * 0.001;
		IVector2 kickVel = absoluteKickVelToVector(initVel);
		double velZ = kickVel.y();

		// maximum height at parabola peak
		double maxHeight = (velZ * velZ) / (2.0 * g);

		if (heightInM > maxHeight)
		{
			return Double.POSITIVE_INFINITY;
		}

		// time where the specified height is reached for the first time
		double tHeight = -(SumatraMath.sqrt((velZ * velZ) - (2.0 * g * heightInM)) - velZ) / g;

		return kickVel.x() * tHeight * 1000.0;
	}


	@Override
	public double getMaximumDistanceToOverChip(final double initVel, final double height)
	{
		final double g = 9.81;
		double heightInM = height * 0.001;
		IVector2 kickVel = absoluteKickVelToVector(initVel);
		double velZ = kickVel.y();

		// maximum height at parabola peak
		double maxHeight = (velZ * velZ) / (2.0 * g);

		if (heightInM > maxHeight)
		{
			return 0;
		}

		// time where the specified height is reached for the second time
		double tHeight = (SumatraMath.sqrt((velZ * velZ) - (2.0 * g * heightInM)) + velZ) / g;

		return kickVel.x() * tHeight * 1000.0;
	}


	@Override
	public double getTimeForKick(final double distance, final double kickSpeed)
	{
		IVector2 xyVect = absoluteKickVelToVector(kickSpeed * 1000);
		IVector3 kickVel = Vector3.fromXYZ(0, xyVect.x(), xyVect.y());
		return ChipBallTrajectory.fromKick(params, Vector2f.ZERO_VECTOR, kickVel, Vector2f.ZERO_VECTOR)
				.getTimeByDist(distance);
	}


	@Override
	public double getVelForKick(final double distance, final double kickSpeed)
	{
		IVector2 xyVect = absoluteKickVelToVector(kickSpeed * 1000);
		IVector3 kickVel = Vector3.fromXYZ(0, xyVect.x(), xyVect.y());
		var chipTraj = ChipBallTrajectory.fromKick(params, Vector2f.ZERO_VECTOR, kickVel, Vector2f.ZERO_VECTOR);
		double time = chipTraj.getTimeByDist(distance);
		return chipTraj.getVelByTime(time).getLength2();
	}


	@Override
	public double getVelForKickByTime(final double kickSpeed, final double travelTime)
	{
		var kickSpeedXy = 1e3 * kickSpeed / Math.sqrt(2);
		var kickVel = Vector3.fromXYZ(0, kickSpeedXy, kickSpeedXy);
		return ChipBallTrajectory.fromKick(params, Vector2f.ZERO_VECTOR, kickVel, Vector2f.ZERO_VECTOR)
				.getVelByTime(travelTime).getLength2();
	}
}
