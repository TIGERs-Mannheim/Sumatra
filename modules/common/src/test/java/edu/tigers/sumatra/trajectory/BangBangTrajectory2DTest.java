/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * Test 2D bang bang trajectories.
 */
public class BangBangTrajectory2DTest
{
	private static final int NUMBER_OF_TESTS = 1000;
	private static final double POS_LIMIT = 10.0;
	private static final double POS_TOLERANCE = 1e-5f;
	private static final double VEL_TOLERANCE = 1e-3f;
	private final Random rng = new Random(0);
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();


	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}


	private IVector2 getRandomVector(final double minmax)
	{
		return Vector2.fromXY(getRandomDouble(minmax), getRandomDouble(minmax));
	}


	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		float tLast = 0.0f;
		for (int i = 0; i < traj.numParts; i++)
		{
			assertThat(traj.parts[i].tEnd).isGreaterThanOrEqualTo(tLast);
			tLast = traj.parts[i].tEnd;
		}
	}


	@Test
	public void testMonteCarloWithinVMax()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0f);

			BangBangTrajectory2D traj = trajectoryFactory.sync(initPos, finalPos, initVel, 2.0, 3.0);

			assertThat(traj.getTotalTime()).isGreaterThanOrEqualTo(0f);

			checkTimeOrder(traj.x);
			checkTimeOrder(traj.y);

			// check final velocity == 0
			assertThat(traj.getVelocity(traj.getTotalTime()).getLength2()).isCloseTo(0.0f, within(VEL_TOLERANCE));

			// check final position reached
			assertThat(traj.getPositionMM(traj.getTotalTime()).x() * 1e-3).isCloseTo(finalPos.x(), within(POS_TOLERANCE));
			assertThat(traj.getPositionMM(traj.getTotalTime()).y() * 1e-3).isCloseTo(finalPos.y(), within(POS_TOLERANCE));
		}
	}


	@Test
	public void testMonteCarloAboveVMax()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(4.0f);

			BangBangTrajectory2D traj = trajectoryFactory.sync(initPos, finalPos, initVel, 2.0, 3.0);

			assertThat(traj.getTotalTime()).isGreaterThanOrEqualTo(0f);

			checkTimeOrder(traj.x);
			checkTimeOrder(traj.y);

			// check final velocity == 0
			assertThat(traj.getVelocity(traj.getTotalTime()).getLength2()).isCloseTo(0.0f, within(VEL_TOLERANCE));

			// check final position reached
			assertThat(traj.getPositionMM(traj.getTotalTime()).x() * 1e-3).isCloseTo(finalPos.x(), within(POS_TOLERANCE));
			assertThat(traj.getPositionMM(traj.getTotalTime()).y() * 1e-3).isCloseTo(finalPos.y(), within(POS_TOLERANCE));
		}
	}


	@Test
	public void testAccuracy()
	{
		IVector2 pos = Vector2.fromXY(-677.6483764648438, 0.011869861744344234);
		IVector2 dest = Vector2.fromXY(-186.5, -0.0);
		IVector2 vel1 = Vector2.fromXY(0.09700202941894531, 4.835854306293186E-4);
		IVector2 vel2 = Vector2.fromXY(0.09700202941894531, 0);

		BangBangTrajectory2D traj1 = trajectoryFactory.sync(
				pos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				vel1,
				2.0,
				2.0);

		BangBangTrajectory2D traj2 = trajectoryFactory.sync(
				pos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				vel2,
				2.0,
				2.0);

		assertThat(traj1.getTotalTime()).isCloseTo(traj2.getTotalTime(), within(1e-4));
	}
}
