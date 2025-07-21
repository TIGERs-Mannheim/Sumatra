/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ball.trajectory.chipped;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ChipBallConsultantTest
{
	private BallParameters params;
	private ChipBallConsultant consultant;


	@BeforeEach
	void setup()
	{
		params = BallParameters.builder()
				.withBallRadius(21.5)
				.withAccSlide(-3600)
				.withAccRoll(-400)
				.withInertiaDistribution(0.667)
				.withChipDampingXYFirstHop(0.75)
				.withChipDampingXYOtherHops(0.95)
				.withChipDampingZ(0.6)
				.withMinHopHeight(10)
				.withMaxInterceptableHeight(150)
				.build();

		consultant = new ChipBallConsultant(params);
	}


	@Test
	void testGetInitVelForDistTouchdown()
	{
		for (double distance = 0; distance < 3000; distance += 100)
		{
			for (int numTouchdown = 0; numTouchdown <= 2; numTouchdown++)
			{
				double kickVel = consultant.getInitVelForDistAtTouchdown(distance, numTouchdown) * 1000;
				IVector2 kickVector = consultant.absoluteKickVelToVector(kickVel);

				ChipBallTrajectory traj = ChipBallTrajectory
						.fromKick(params, Vector2f.ZERO_VECTOR, Vector3.fromXYZ(kickVector.x(), 0, kickVector.y()),
								Vector2f.ZERO_VECTOR);

				if (traj.getTouchdownLocations().size() > numTouchdown)
				{
					double distToTouchdown = traj.getTouchdownLocations().get(numTouchdown).getLength2();

					assertEquals(distance, distToTouchdown, 1e-6);
				}
			}
		}
	}


	@Test
	void testGetMinimumDistanceToOverChip()
	{
		// test specific values
		double dist = consultant.getMinimumDistanceToOverChip(3.0, 150);
		assertEquals(188.89098770912403, dist, 1e-6);

		// test unreachable height
		dist = consultant.getMinimumDistanceToOverChip(1.0, 10000);
		assertTrue(Double.isInfinite(dist));
	}


	@Test
	void testGetMaximumDistanceToOverChip()
	{
		// test specific values
		double dist = consultant.getMaximumDistanceToOverChip(3.0, 150);
		assertEquals(728.5402049514264, dist, 1e-6);

		// test unreachable height
		dist = consultant.getMaximumDistanceToOverChip(1.0, 10000);
		assertEquals(0, dist, 1e-9);
	}


	@Test
	void testGetInitVelForPeakHeight()
	{
		double kickVel = consultant.getInitVelForPeakHeight(200);
		double distMin = consultant.getMinimumDistanceToOverChip(kickVel, 200);
		double distMax = consultant.getMaximumDistanceToOverChip(kickVel, 200);
		assertEquals(0, distMin - distMax, 1e-4);
	}

}
