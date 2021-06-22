/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class BangBangTrajectory2DAsyncTest
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
	public void testMirror() {

		var initPos = Vector2.fromXY(1,1);
		var finalPos = Vector2.fromXY(2,2);
		var initVel = Vector2.fromXY(1,1);
		var primaryDirection = Vector2.fromXY(1,1);
		var traj = trajectoryFactory
				.async(initPos, finalPos, initVel, 2.0, 3.0, primaryDirection);
		var mirrored = traj.mirrored();

		assertThat(mirrored.getPosition(0)).isEqualTo(initPos.multiply(-1));
		assertThat(mirrored.getVelocity(0)).isEqualTo(initVel.multiply(-1));
		assertThat(mirrored.getPosition(10)).isEqualTo(finalPos.multiply(-1));
		assertThat(mirrored.getVelocity(10)).isEqualTo(Vector2.zero());
	}


	@Test
	public void testMonteCarloWithinVMax()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0f);
			IVector2 primaryDirection = Vector2.fromAngle(getRandomDouble(AngleMath.PI_TWO));

			BangBangTrajectory2DAsync traj = trajectoryFactory
					.async(initPos, finalPos, initVel, 2.0, 3.0, primaryDirection);

			assertThat(traj.getTotalTime()).isGreaterThanOrEqualTo(0f);

			checkTimeOrder(traj.child.x);
			checkTimeOrder(traj.child.y);

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
			IVector2 primaryDirection = Vector2.fromAngle(getRandomDouble(AngleMath.PI_TWO));

			BangBangTrajectory2DAsync traj = trajectoryFactory
					.async(initPos, finalPos, initVel, 2.0, 3.0, primaryDirection);

			assertThat(traj.getTotalTime()).isGreaterThanOrEqualTo(0f);

			checkTimeOrder(traj.child.x);
			checkTimeOrder(traj.child.y);

			// check final velocity == 0
			assertThat(traj.getVelocity(traj.getTotalTime()).getLength2()).isCloseTo(0.0f, within(VEL_TOLERANCE));

			// check final position reached
			assertThat(traj.getPositionMM(traj.getTotalTime()).x() * 1e-3).isCloseTo(finalPos.x(), within(POS_TOLERANCE));
			assertThat(traj.getPositionMM(traj.getTotalTime()).y() * 1e-3).isCloseTo(finalPos.y(), within(POS_TOLERANCE));
		}
	}
}