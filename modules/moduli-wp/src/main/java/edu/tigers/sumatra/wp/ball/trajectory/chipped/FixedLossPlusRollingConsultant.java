/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.prediction.IChipBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FixedLossPlusRollingConsultant implements IChipBallConsultant
{
	private double chipAngle = AngleMath.deg2rad(45);
	
	private final FixedLossPlusRollingParameters params;
	
	
	/**
	 * Constructor.
	 * 
	 * @param params
	 */
	public FixedLossPlusRollingConsultant(final FixedLossPlusRollingParameters params)
	{
		this.params = params;
	}
	
	
	@Override
	public IVector2 absoluteKickVelToVector(final double vel)
	{
		return Vector2.fromXY(Math.cos(chipAngle) * vel, Math.sin(chipAngle) * vel);
	}
	
	
	@Override
	public double botVelocityToChipFartherThanMaximumDistance(final double distance, final int numTouchdowns,
			double absMaxVel)
	{
		double partVelz = AngleMath.cos(chipAngle) * absMaxVel * 1000;
		double partVelxy = AngleMath.sin(chipAngle) * absMaxVel * 1000;
		IVector3 maxVel = Vector3.fromXYZ(partVelxy, 0, partVelz);
		List<IVector2> touchdowns = BallFactory
				.createTrajectoryFromKick(Vector2.ZERO_VECTOR, maxVel, true)
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
		return Math.abs(AngleMath.sin(chipAngle) * initialVel - partVelxy / 1000);
	}
	
	
	public void setChipAngle(final double chipAngle)
	{
		this.chipAngle = AngleMath.deg2rad(chipAngle);
	}
	
	
	@Override
	public FixedLossPlusRollingConsultant withChipAngle(final double chipAngle)
	{
		setChipAngle(chipAngle);
		return this;
	}
	
	
	@Override
	public double getInitVelForDistAtTouchdown(final double distance, final int numTouchdown)
	{
		final double g = 9810;
		
		double f = 0.0;
		for (int i = 0; i <= numTouchdown; i++)
		{
			f += Math.pow(params.getChipDampingXY(), i) * Math.pow(params.getChipDampingZ(), i);
		}
		
		double denom = f * Math.cos(chipAngle) * Math.sin(chipAngle);
		Validate.isTrue(denom > 0);
		
		return Math.sqrt((distance * g * 0.5) / denom) * 0.001;
	}
	
	
	@Override
	public double getInitVelForPeakHeight(final double height)
	{
		final double g = 9810;
		
		// initial z velocity in [m/s]
		double velZ = Math.sqrt(2.0 * g * height) * 0.001;
		
		return velZ / Math.sin(chipAngle);
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
		double tHeight = -(Math.sqrt((velZ * velZ) - (2.0 * g * heightInM)) - velZ) / g;
		
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
		double tHeight = (Math.sqrt((velZ * velZ) - (2.0 * g * heightInM)) + velZ) / g;
		
		return kickVel.x() * tHeight * 1000.0;
	}
}
