/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Lukas Magel
 */
public class TriangleTest
{

	/**
	 * Test method for
	 * {@link Triangle#intersectPerimeterPath(IPath)}
	 */
	@Test
	public void testLineIntersections()
	{
		Vector2 a = Vector2.fromXY(0.0, 0.0);
		Vector2 b = Vector2.fromXY(1000.0, 0.0);
		Vector2 c = Vector2.fromXY(0.0, 1000.0);
		Triangle triangle = Triangle.fromCorners(a, b, c);

		// Intersecting lines
		ILine line1 = Lines.lineFromPoints(Vector2.fromXY(1, 1), Vector2.fromXY(2, 2));
		assertThat(triangle.intersectPerimeterPath(line1)).hasSize(2);

		// Intersecting lines
		ILine line4 = Lines.lineFromPoints(Vector2.fromXY(0, 500), Vector2.fromXY(500, 500));
		assertThat(triangle.intersectPerimeterPath(line4)).hasSize(2);

		// Non Intersecting lines
		ILine line2 = Lines.lineFromPoints(Vector2.fromXY(-1, 0), Vector2.fromXY(-1, 0));
		assertThat(triangle.intersectPerimeterPath(line2)).isEmpty();

		ILine line3 = Lines.lineFromPoints(Vector2.fromXY(0, -1), Vector2.fromXY(1, -1));
		assertThat(triangle.intersectPerimeterPath(line3)).isEmpty();


	}


	/**
	 * Test method for
	 * {@link Triangle#isPointInShape(IVector2)}.
	 */
	@Test
	public void testIsPointInShapeWithMargin()
	{
		double margin = 250d;
		Vector2 a = Vector2.fromXY(0.0, 0.0);
		Vector2 b = Vector2.fromXY(1000.0, 0.0);
		Vector2 c = Vector2.fromXY(0.0, 1000.0);

		Triangle triangle = Triangle.fromCorners(a, b, c);

		ILineSegment abSegment = Lines.segmentFromPoints(a, b);
		IVector2 ab = abSegment.stepAlongPath(abSegment.getLength() / 2.);
		ILineSegment bcSegment = Lines.segmentFromPoints(a, b);
		IVector2 bc = bcSegment.stepAlongPath(bcSegment.getLength() / 2.);
		ILineSegment acSegment = Lines.segmentFromPoints(a, b);
		IVector2 ac = acSegment.stepAlongPath(acSegment.getLength() / 2.);

		testCornersWithMargin(triangle, margin);

		// Testing Corners here
		assertThat(triangle.isPointInShape(a)).isTrue();
		assertThat(triangle.isPointInShape(b)).isTrue();
		assertThat(triangle.isPointInShape(c)).isTrue();
		assertThat(triangle.isPointInShape(ab)).isTrue();
		assertThat(triangle.isPointInShape(bc)).isTrue();
		assertThat(triangle.isPointInShape(ac)).isTrue();

		assertThat(triangle.withMargin(margin).isPointInShape(a)).isTrue();
		assertThat(triangle.withMargin(margin).isPointInShape(b)).isTrue();
		assertThat(triangle.withMargin(margin).isPointInShape(c)).isTrue();

		assertThat(triangle.withMargin(-margin).isPointInShape(a)).isFalse();
		assertThat(triangle.withMargin(-margin).isPointInShape(b)).isFalse();
		assertThat(triangle.withMargin(-margin).isPointInShape(c)).isFalse();
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
				assertThat(triangle.withMargin(margin).isPointInShape(curPoint)).isTrue();
			}
		}
	}


	private List<IVector2> getCorners()
	{
		return List.of(
				Vector2.fromXY(-2, 1),
				Vector2.fromXY(1, -1),
				Vector2.fromXY(1, 4)
		);
	}


	private ITriangle buildTriangle()
	{
		var corners = getCorners();
		return Triangle.fromCorners(corners.get(0), corners.get(1), corners.get(2));
	}


	@Test
	public void testIsPointInShape()
	{
		var triangle = buildTriangle();
		List.of(
				Vector2.fromXY(0, 0),
				Vector2.fromXY(0, 3),
				Vector2.fromXY(1, 1),
				Vector2.fromXY(-2, 1)
		).forEach(p -> assertThat(triangle.isPointInShape(p)).isTrue());
		List.of(
				Vector2.fromXY(0, 3.5),
				Vector2.fromXY(2, -1),
				Vector2.fromXY(2, 2),
				Vector2.fromXY(-3, 1)
		).forEach(p -> assertThat(triangle.isPointInShape(p)).isFalse());
	}


	@Test
	public void testWithMargin()
	{
		var triangle = buildTriangle();

		var margin = 0.1;
		var withMargin = triangle.withMargin(margin);
		for (var edge : triangle.getPerimeterPath())
		{
			if (edge instanceof ILineBase line)
			{
				assertThat(withMargin.getPerimeterPath()).anyMatch(
						p -> ((ILineBase) p).isParallelTo(line)
				);
			}
		}
	}


	@Test
	public void testGetPerimeterPath()
	{
		var triangle = buildTriangle();
		var path = triangle.getPerimeterPath();
		assertThat(path).hasSameSizeAs(getCorners());
		for (var corner : getCorners())
		{
			assertThat(path).anyMatch(p -> p.getPathStart().equals(corner));
			assertThat(path).anyMatch(p -> p.getPathEnd().equals(corner));
		}
	}


	@Test
	public void testPerimeterPathOrder()
	{
		var perimeter = buildTriangle().getPerimeterPath();
		IBoundedPath lastPath = null;
		for (var p : perimeter)
		{
			if (lastPath != null)
			{
				assertThat(p.getPathStart()).isEqualTo(p.getPathStart());
			}
			lastPath = p;
		}
	}


	@Test
	public void testGetPerimeterLength()
	{
		assertThat(buildTriangle().getPerimeterLength()).isCloseTo(12.8481919626, within(1e-10));
	}


	@Test
	public void testPointsAroundPerimeter()
	{
		var triangle = buildTriangle();

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(-0.999, 1.999), Vector2.fromXY(-1.001, 2.001)),
				Lines.segmentFromPoints(Vector2.fromXY(0.999, 1), Vector2.fromXY(1.001, 1))
		);

		for (var segment : segments)
		{
			assertThat(triangle.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(triangle.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(triangle.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(triangle.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(triangle.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(triangle.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(triangle.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(triangle.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(triangle.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());
		}
	}


	@Test
	public void testIntersectPerimeterPathLine()
	{
		var triangle = buildTriangle();
		var line = Lines.lineFromPoints(Vector2.fromXY(-2, 3), Vector2.fromXY(1, 0));
		assertThat(triangle.intersectPerimeterPath(line)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1, 2),
				Vector2.fromXY(1, 0)
		);
	}


	@Test
	public void testIntersectPerimeterPathHalfLine()
	{
		var triangle = buildTriangle();
		var halfLine = Lines.halfLineFromPoints(Vector2.fromXY(-2, 3), Vector2.fromXY(1, 0));
		assertThat(triangle.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1, 2),
				Vector2.fromXY(1, 0)
		);
		halfLine = Lines.halfLineFromPoints(Vector2.fromXY(0, 1), Vector2.fromXY(-2, 3));
		assertThat(triangle.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1, 2)
		);
		halfLine = Lines.halfLineFromPoints(Vector2.fromXY(0, 1), Vector2.fromXY(1, 0));
		assertThat(triangle.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 0)
		);
	}


	@Test
	public void testIntersectPerimeterPathLineSegment()
	{
		var triangle = buildTriangle();
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-2, 3), Vector2.fromXY(1, 0));
		assertThat(triangle.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1, 2),
				Vector2.fromXY(1, 0)
		);
		segment = Lines.segmentFromPoints(Vector2.fromXY(0, 1), Vector2.fromXY(-2, 3));
		assertThat(triangle.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1, 2)
		);
		segment = Lines.segmentFromPoints(Vector2.fromXY(0, 1), Vector2.fromXY(1, 0));
		assertThat(triangle.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 0)
		);
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		var triangle = buildTriangle();
		var circle = Circle.from3Points(getCorners().get(0), getCorners().get(1), getCorners().get(2)).orElseThrow();
		assertThat(triangle.intersectPerimeterPath(circle)).containsExactlyInAnyOrderElementsOf(getCorners());

		circle = Circle.createCircle(Vector2.fromXY(1, 2), 2);
		assertThat(triangle.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 0),
				Vector2.fromXY(1, 4),
				Vector2.fromXY(-1, 2)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		var triangle = buildTriangle();


		var arc = Arc.createArc(Vector2.fromXY(1, 2), 2, AngleMath.PI_HALF, AngleMath.PI);
		assertThat(triangle.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 0),
				Vector2.fromXY(1, 4),
				Vector2.fromXY(-1, 2)
		);
		arc = Arc.createArc(Vector2.fromXY(1, 2), 2, 0, -3 * AngleMath.PI_QUART);
		assertThat(triangle.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 0)
		);
	}


	@Test
	public void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(buildTriangle(), true);
	}
}
