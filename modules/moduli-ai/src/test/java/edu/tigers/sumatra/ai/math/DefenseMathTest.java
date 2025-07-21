/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DefenseMathTest
{

	@Test
	public void testCalculateGoalDefPoint()
	{

		Random rnd = new Random(1245849416);
		for (int i = 0; i < 100; i++)
		{
			IVector2 target = Vector2.fromXY((rnd.nextDouble() - 0.5) * Geometry.getFieldLength(),
					(rnd.nextDouble() - 0.5) * Geometry.getFieldWidth());

			IVector2 defPoint = DefenseMath.calculateGoalDefPoint(target, Geometry.getBotRadius());
			ILine lineToGoal = Lines.lineFromPoints(target, Geometry.getGoalOur().getCenter());

			// defPoint should protect the goal, especially the goal center
			assertThat(lineToGoal.distanceTo(defPoint)).as("Target: {}", target.toString())
					.isLessThan(Geometry.getBotRadius());
		}
	}

}
