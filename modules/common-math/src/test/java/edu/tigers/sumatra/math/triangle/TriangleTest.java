/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Lukas Magel
 */
public class TriangleTest
{

	/**
	 * Test method for
	 * {@link Triangle#lineIntersections(ILine)}
	 */
	@Test
	public void testLineIntersections()
	{
		Vector2 a = Vector2.fromXY(0.0, 0.0);
		Vector2 b = Vector2.fromXY(1000.0, 0.0);
		Vector2 c = Vector2.fromXY(0.0, 1000.0);
		Triangle triangle = Triangle.fromCorners(a, b, c);
		
		// Intersecting lines
		ILine line1 = edu.tigers.sumatra.math.line.Line.fromPoints(Vector2.fromXY(1, 1), Vector2.fromXY(2, 2));
		assertThat(triangle.lineIntersections(line1)).hasSize(3);
		
		// Intersecting lines
		ILine line4 = edu.tigers.sumatra.math.line.Line.fromPoints(Vector2.fromXY(0, 500), Vector2.fromXY(500, 500));
		assertThat(triangle.lineIntersections(line4)).hasSize(2);
		
		// Non Intersecting lines
		ILine line2 = edu.tigers.sumatra.math.line.Line.fromPoints(Vector2.fromXY(-1, 0), Vector2.fromXY(-1, 0));
		assertThat(triangle.lineIntersections(line2)).isEmpty();
		
		ILine line3 = edu.tigers.sumatra.math.line.Line.fromPoints(Vector2.fromXY(0, -1), Vector2.fromXY(1, -1));
		assertThat(triangle.lineIntersections(line3)).isEmpty();
		
		
	}
	
	
	/**
	 * Test method for
	 * {@link Triangle#isPointInShape(IVector2, double)}.
	 */
	@Test
	public void testIsPointInShapeWithMargin()
	{
		double margin = 250d;
		Vector2 a = Vector2.fromXY(0.0, 0.0);
		Vector2 b = Vector2.fromXY(1000.0, 0.0);
		Vector2 c = Vector2.fromXY(0.0, 1000.0);
		
		Triangle triangle = Triangle.fromCorners(a, b, c);

		ILineSegment abSegment = Lines.segmentFromPoints(a,b);
		IVector2 ab = abSegment.stepAlongLine(abSegment.getLength()/2.);
		ILineSegment bcSegment = Lines.segmentFromPoints(a,b);
		IVector2 bc = bcSegment.stepAlongLine(bcSegment.getLength()/2.);
		ILineSegment acSegment = Lines.segmentFromPoints(a,b);
		IVector2 ac = acSegment.stepAlongLine(acSegment.getLength()/2.);
		
		testCornersWithMargin(triangle, margin);
		
		// Testing Corners here
		assertThat(triangle.isPointInShape(a)).isTrue();
		assertThat(triangle.isPointInShape(b)).isTrue();
		assertThat(triangle.isPointInShape(c)).isTrue();
		assertThat(triangle.isPointInShape(ab)).isTrue();
		assertThat(triangle.isPointInShape(bc)).isTrue();
		assertThat(triangle.isPointInShape(ac)).isTrue();
		
		assertThat(triangle.isPointInShape(a, margin)).isTrue();
		assertThat(triangle.isPointInShape(b, margin)).isTrue();
		assertThat(triangle.isPointInShape(c, margin)).isTrue();
		
		assertThat(triangle.isPointInShape(a, -margin)).isFalse();
		assertThat(triangle.isPointInShape(b, -margin)).isFalse();
		assertThat(triangle.isPointInShape(c, -margin)).isFalse();
	}
	
	
	/**
	 * Lay a circle around each corner with the same radius as the margin and assert that each point on the circle edge
	 * is located inside the triangle
	 * 
	 * @param triangle
	 * @param margin
	 */
	private void testCornersWithMargin(final Triangle triangle, final double margin)
	{
		List<IVector2> corners = triangle.getCorners();
		for (int i = 0; i < corners.size(); i++)
		{
			IVector2 corner = corners.get(i);
			IVector2 otherCorner = corners.get((i + 1) % 3);
			Vector2 edgePoint = corner.addNew(otherCorner.subtractNew(corner).scaleTo(margin * 0.999));
			
			for (int j = 0; j < 100; j++)
			{
				IVector2 curPoint = CircleMath.stepAlongCircle(edgePoint, corner, Math.toRadians((j * 360.0d) / 100.0d));
				assertThat(triangle.isPointInShape(curPoint, margin)).isTrue();
			}
		}
	}
}
