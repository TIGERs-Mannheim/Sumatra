/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.Test;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class AngleRaterMathTest
{
	
	@Test
	public void testGetTriangle()
	{
		IVector2 origin = Geometry.getCenter();
		IVector2 leftPost = Geometry.getGoalTheir().getLeftPost();
		IVector2 rightPost = Geometry.getGoalTheir().getRightPost();
		
		IVector2 pointInGoal = Geometry.getGoalTheir().getCenter()
				.addNew(Vector2.fromX(Geometry.getGoalTheir().getDepth() / 2));
		
		ITriangle triangle = AngleRaterMath.getTriangle(leftPost, rightPost, false, origin);
		assertThat(triangle.getCorners()).as("Check corners equal to arguments").contains(origin, leftPost, rightPost);
		assertThat(triangle.isPointInShape(pointInGoal)).as("Check point in goal (normal triangle)").isFalse();
		
		triangle = AngleRaterMath.getTriangle(leftPost, rightPost, true, origin);
		assertThat(triangle.isPointInShape(pointInGoal)).as("Check point in goal (extended triangle)").isTrue();
		assertThat(Lines.segmentFromPoints(triangle.getA(), triangle.getB()).getLength())
				.as("Check line length")
				.isEqualTo(Lines.segmentFromPoints(triangle.getA(), triangle.getC()).getLength(), Offset.offset(1e-4));
		
		origin = leftPost.subtractNew(Vector2.fromX(500));
		triangle = AngleRaterMath.getTriangle(leftPost, rightPost, true, origin);
		assertThat(triangle.isPointInShape(pointInGoal)).as("Check point in goal (straight line)").isTrue();
		assertThat(Lines.segmentFromPoints(triangle.getA(), triangle.getB()).getLength())
				.as("Check line length (straight line)")
				.isLessThan(Lines.segmentFromPoints(triangle.getA(), triangle.getC()).getLength());
	}
}