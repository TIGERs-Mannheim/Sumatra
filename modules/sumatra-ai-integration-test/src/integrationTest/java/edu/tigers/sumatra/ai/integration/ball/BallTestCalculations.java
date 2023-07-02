/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.ball;

import edu.tigers.sumatra.ai.metis.kicking.KickSpeedFactory;
import edu.tigers.sumatra.ball.trajectory.BallFactory;
import edu.tigers.sumatra.ball.trajectory.IChipBallConsultant;
import edu.tigers.sumatra.ball.trajectory.chipped.ChipBallTrajectory;
import edu.tigers.sumatra.ball.trajectory.flat.FlatBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.ArrayList;

import static java.lang.System.out;


/**
 * Some test calculations for straight and chipped ball speeds.
 */
public class BallTestCalculations
{
	private BallFactory factory = Geometry.getBallFactory();
	private IChipBallConsultant chipConsultant = factory.createChipConsultant();
	private KickSpeedFactory kickSpeedFactory = new KickSpeedFactory();

	private double maxBallSpeedAtTarget = 2.0;
	private double maxKickSpeed = 6.5;
	private double minPassDuration = 0;


	public static void main(String[] args)
	{
		var ballTest = new BallTestCalculations();
		ballTest.run();
	}


	private void run()
	{
		out.println(factory.getBallParams());

		var distance = 1500;

		var dataSets = new ArrayList<DataSet>();
		dataSets.add(straightPass(distance));
		dataSets.add(chipPass(distance));
		dataSets.add(chipLowPass(distance));
		dataSets.add(chipPassMin(distance));

		for (double kickSpeed = 0.1; kickSpeed < 4; kickSpeed += 0.1)
		{
			dataSets.add(chip("kickSpeed", distance, maxBallSpeedAtTarget, kickSpeed));
		}

		out.println(
				"name, targetDistance, targetReceivingSpeed, kickSpeed, timeToTarget, distance, receivingSpeed, peakHeight, timeToStop, distanceToStop, receivingHeight");
		dataSets.forEach((DataSet it) ->
				out.printf(
						"%s, %.0f, %.1f, %.1f, %.2f, %.0f, %.1f, %.0f, %.1f, %.0f, %.0f%n",
						it.name, it.targetDistance, it.targetReceivingSpeed, it.kickSpeed,
						it.time, it.distance, it.receivingSpeed, it.peakHeight,
						it.timeToStop, it.distanceToStop,
						it.receivingHeight
				));
	}


	private DataSet chip(String name, double distanceIn, double maxBallSpeedAtTarget, double kickSpeed)
	{
		var trajectory = ChipBallTrajectory.fromKick(
				factory.getBallParams(),
				Vector2f.ZERO_VECTOR,
				chipConsultant.speedToVel(0, kickSpeed * 1000),
				Vector2f.ZERO_VECTOR
		);
		double kickSpeedDiff = Math.abs(kickSpeed - trajectory.getInitialVel().getLength());
		if (kickSpeedDiff > 0.1)
		{
			throw new IllegalStateException(String.valueOf(kickSpeedDiff));
		}
		var time = trajectory.getTimeByDist(distanceIn);
		var height = trajectory.getPosByTime(time).z();
		var totalTime = trajectory.getTimeByVel(0);
		return new DataSet(
				name,
				distanceIn,
				maxBallSpeedAtTarget,
				trajectory.getInitialVel().getLength(),
				time,
				trajectory.getDistByTime(time),
				trajectory.getVelByTime(time).getLength(),
				maxHeight(kickSpeed),
				totalTime,
				trajectory.getDistByTime(totalTime),
				height
		);
	}


	private DataSet flat(String name, double distanceIn, double maxBallSpeedAtTargetIn, double kickSpeed)
	{
		var trajectory = FlatBallTrajectory.fromKick(
				factory.getBallParams(),
				Vector2f.ZERO_VECTOR,
				Vector2.fromX(kickSpeed * 1000),
				Vector2f.ZERO_VECTOR
		);
		var time = trajectory.getTimeByDist(distanceIn);
		var totalTime = trajectory.getTimeByVel(0);
		return new DataSet(
				name,
				distanceIn,
				maxBallSpeedAtTargetIn,
				kickSpeed,
				time,
				trajectory.getDistByTime(time),
				trajectory.getVelByTime(time).getLength(),
				0.0,
				totalTime,
				trajectory.getDistByTime(totalTime),
				0
		);
	}


	private DataSet straightPass(double distance)
	{
		var kickSpeed = kickSpeedFactory.straight(distance, maxBallSpeedAtTarget, maxKickSpeed, minPassDuration);
		return flat("straight", distance, maxBallSpeedAtTarget, kickSpeed);
	}


	private DataSet chipPass(double distance)
	{
		var kickSpeed = kickSpeedFactory.chip(distance, maxBallSpeedAtTarget, maxKickSpeed, minPassDuration);
		return chip("chipPass", distance, maxBallSpeedAtTarget, kickSpeed);
	}


	private DataSet chipPassMin(double distance)
	{
		var kickSpeed = kickSpeedFactory.chip(distance, 0.0, maxKickSpeed, minPassDuration);
		return chip("chipMin", distance, 0.0, kickSpeed);
	}


	private DataSet chipLowPass(double distance)
	{
		var kickSpeed = chipConsultant.getInitVelForPeakHeight(15);
		return chip("chipLow", distance, Double.NaN, kickSpeed);
	}


	private static double maxHeight(double initVel)
	{
		final double g = 9.81;
		IVector2 kickVel = absoluteKickVelToVector(initVel);
		double velZ = kickVel.y();

		// maximum height at parabola peak
		return (velZ * velZ) / (2.0 * g) * 1000.0;
	}


	private static IVector2 absoluteKickVelToVector(final double vel)
	{
		var chipAngle = AngleMath.deg2rad(45);
		return Vector2.fromXY(SumatraMath.cos(chipAngle) * vel, SumatraMath.sin(chipAngle) * vel);
	}


	private record DataSet(
			String name,
			double targetDistance,
			double targetReceivingSpeed,
			double kickSpeed,
			double time,
			double distance,
			double receivingSpeed,
			double peakHeight,
			double timeToStop,
			double distanceToStop,
			double receivingHeight
	)
	{
	}
}
