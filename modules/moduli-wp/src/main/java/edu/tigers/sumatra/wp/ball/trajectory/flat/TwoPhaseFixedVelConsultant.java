/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.pow;

import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory.TwoPhaseFixedVelParameters;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TwoPhaseFixedVelConsultant implements IStraightBallConsultant
{
	private final TwoPhaseFixedVelParameters params;
	
	
	/**
	 * @param params
	 */
	public TwoPhaseFixedVelConsultant(final TwoPhaseFixedVelParameters params)
	{
		this.params = params;
	}
	
	
	@Override
	public double getInitVelForTime(final double endVel, final double time)
	{
		double endVelMM = endVel * 1000.0;
		
		if (endVelMM >= params.getVSwitch())
		{
			return (endVelMM - (params.getAccSlide() * time)) * 0.001;
		}
		
		if ((endVelMM - (time * params.getAccRoll())) > params.getVSwitch())
		{
			// is initVel in sliding phase?
			double rollingTime = (endVelMM - params.getVSwitch()) / params.getAccRoll();
			double slidingTime = time - rollingTime;
			return (params.getVSwitch() - (params.getAccSlide() * slidingTime)) * 0.001;
		}
		
		return (endVelMM - (time * params.getAccRoll())) * 0.001;
	}
	
	
	@Override
	public double getInitVelForDist(final double distance, final double endVel)
	{
		double endVelMM = endVel * 1000.0;
		
		if (endVelMM > params.getVSwitch())
		{
			return (sqrt(pow(endVelMM, 2) - (2 * params.getAccSlide() * distance))) * 0.001;
		}
		
		double determinant = (-((params.getAccSlide() * pow(params.getVSwitch(), 2)) / params.getAccRoll())
				+ pow(params.getVSwitch(), 2)
				+ ((params.getAccSlide() * pow(endVelMM, 2)) / params.getAccRoll()))
				- (2 * params.getAccSlide() * distance);
		
		if (determinant > 0)
		{
			return sqrt(determinant) * 0.001;
		}
		
		// determinant is negative --> no sliding phase
		return (sqrt(pow(endVelMM, 2) - (2 * params.getAccRoll() * distance))) * 0.001;
	}
	
	
	@Override
	public double getTimeForKick(final double distance, final double kickVel)
	{
		return TwoPhaseFixedVelBallTrajectory
				.fromKick(Vector2f.ZERO_VECTOR, Vector2.fromXY(kickVel * 1000, 0), params)
				.getTimeByDist(distance);
	}
	
	
	@Override
	public double getVelForKickByTime(final double kickSpeed, final double travelTime)
	{
		return TwoPhaseFixedVelBallTrajectory
				.fromKick(Vector2f.ZERO_VECTOR, Vector2.fromXY(1e3, 0).multiplyNew(kickSpeed), params)
				.getVelByTime(travelTime)
				.getLength2();
	}
	
	
	@Override
	public double getInitVelForTimeDist(final double distance, final double time)
	{
		double initVel;
		double distanceIfOnlyRolling = getDistanceIfOnlyRollingTimeDist(time);
		if (distanceIfOnlyRolling >= distance)
		{
			initVel = getInitVelForTimeDistRolling(distance, time);
		} else
		{
			initVel = getInitVelForTimeDistSliding(distance, time);
		}
		return 1e-3 * initVel;
	}
	
	
	private double getInitVelForTimeDistSliding(final double distance, final double time)
	{
		double initVel;
		double det = (2 * params.getAccSlide() - 2 * params.getAccRoll()) * time * params.getVSwitch()
				+ params.getAccRoll() * params.getAccSlide() * pow(time, 2)
				+ (2 * params.getAccRoll() - 2 * params.getAccSlide()) * distance;
		if (det >= 0)
		{
			double initVelOne = -(params.getAccSlide() * sqrt(det)
					+ (params.getAccRoll() - params.getAccSlide()) * params.getVSwitch()
					- params.getAccRoll() * params.getAccSlide() * time) / (params.getAccSlide() - params.getAccRoll());
			double initVelTwo = (params.getAccSlide() * sqrt(det)
					+ (params.getAccSlide() - params.getAccRoll()) * params.getVSwitch()
					+ params.getAccRoll() * params.getAccSlide() * time) / (params.getAccSlide() - params.getAccRoll());
			initVel = max(initVelOne, initVelTwo);
		} else
		{
			initVel = getInitVelForTimeDistOnlySliding(distance, time);
		}
		return initVel;
	}
	
	
	private double getDistanceIfOnlyRollingTimeDist(final double time)
	{
		return params.getVSwitch() * time + .5 * params.getAccRoll() * pow(time, 2);
	}
	
	
	private double getInitVelForTimeDistRolling(final double distance, final double time)
	{
		double initVel;
		initVel = distance / time - .5 * params.getAccRoll() * time;
		return initVel;
	}
	
	
	private double getInitVelForTimeDistOnlySliding(final double distance, final double time)
	{
		return distance / time - .5 * params.getAccRoll() * time;
	}
}
