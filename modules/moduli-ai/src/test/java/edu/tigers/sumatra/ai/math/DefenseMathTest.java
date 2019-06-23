/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.util.Random;

import org.junit.Test;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DefenseMathTest
{
	
	@Test
	public void testCalculateLineDefPoint()
	{

        Random rnd = new Random(1245849416);
		for (int i = 0; i < 100; i++)
		{
			IVector2 target = Vector2.fromXY((rnd.nextDouble() - 0.5) * Geometry.getFieldLength(),
					(rnd.nextDouble() - 0.5) * Geometry.getFieldWidth());

			IVector2 defPoint = DefenseMath.calculateLineDefPoint(target, Geometry.getGoalOur().getLeftPost(),
					Geometry.getGoalOur().getRightPost(), Geometry.getBotRadius());
			ILine lineToGoal = Line.fromPoints(target, Geometry.getGoalOur().getCenter());
			
			// defPoint should protect the goal, especially the goal center
			assertThat("Target: " + target.toString(), lineToGoal.distanceTo(defPoint), lessThan(Geometry.getBotRadius()));
		}
	}
	
}
