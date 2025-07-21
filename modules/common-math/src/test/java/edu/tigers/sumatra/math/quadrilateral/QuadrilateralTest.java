/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import com.google.common.collect.Collections2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.polygon.PolygonBuilder;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class QuadrilateralTest
{
	@Test
	public void fromCorners()
	{
		List<IVector2> corners = new ArrayList<>(4);

		corners.add(Vector2.fromXY(-1, -1));
		corners.add(Vector2.fromXY(-1, 1));
		corners.add(Vector2.fromXY(1, -1));
		corners.add(Vector2.fromXY(1, 1));

		IQuadrilateral quadrilateral = Quadrilateral.fromCorners(corners);
		for (List<IVector2> permCorners : Collections2.permutations(corners))
		{
			IQuadrilateral permQuadrangle = Quadrilateral.fromCorners(permCorners);
			assertThat(permQuadrangle.getCorners()).containsAll(permCorners);
			assertThat(permQuadrangle).isEqualTo(quadrilateral);
		}
	}


	@Test
	public void isPointInShape()
	{
		List<IVector2> acorners = new ArrayList<>(4);

		acorners.add(Vector2.fromXY(1, -1));
		acorners.add(Vector2.fromXY(-1, 1));
		acorners.add(Vector2.fromXY(1, 1));
		acorners.add(Vector2.fromXY(-1, -1));

		for (List<IVector2> corners : Collections2.permutations(acorners))
		{
			IQuadrilateral quadrilateral = Quadrilateral.fromCorners(corners.get(0), corners.get(1), corners.get(2),
					corners.get(3));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, 0))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(1, 0.9))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(1, 0.5))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0.9))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0.5))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, -0.9))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, 0.9))).isTrue();
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, -0.2))).isTrue();
		}
	}


	@Test
	public void getLineIntersections()
	{
		List<IVector2> corners = new ArrayList<>(4);

		corners.add(Vector2.fromXY(-1, -1));
		corners.add(Vector2.fromXY(-1, 1));
		corners.add(Vector2.fromXY(1, -1));
		corners.add(Vector2.fromXY(1, 1));
		IQuadrilateral quadrilateral = Quadrilateral.fromCorners(corners);

		// test one intersection
		ILineSegment line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-5, 0), Vector2.fromXY(0, 0));
		List<IVector2> intersections = quadrilateral.intersectPerimeterPath(line1Intersection);
		assertThat(intersections).hasSize(1);
		assertThat(intersections.get(0).x()).isCloseTo(-1, Percentage.withPercentage(1e-3));
		assertThat(intersections.get(0).y()).isCloseTo(0, Percentage.withPercentage(1e-3));

		// test two intersections
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-5, 0), Vector2.fromXY(5, 0));
		intersections = quadrilateral.intersectPerimeterPath(line1Intersection);
		assertThat(intersections).hasSize(2);

		// test no intersections
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-0.99, -0.99), Vector2.fromXY(0.99, 0.99));
		intersections = quadrilateral.intersectPerimeterPath(line1Intersection);
		assertThat(intersections).isEmpty();

		// test no intersections 2
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-3, 5), Vector2.fromXY(3, 5));
		intersections = quadrilateral.intersectPerimeterPath(line1Intersection);
		assertThat(intersections).isEmpty();
	}


	@Test
	public void testEquals()
	{
		EqualsVerifier.forClass(Quadrilateral.class)
				.suppress(Warning.NULL_FIELDS)
				.verify();
	}


	private List<IVector2> getCorners()
	{
		return List.of(
				Vector2.fromXY(0, 2),
				Vector2.fromXY(2, 0),
				Vector2.fromXY(0, -2),
				Vector2.fromXY(-3, 2)
		);
	}


	private IQuadrilateral buildQuadrilateral()
	{
		return Quadrilateral.fromCorners(getCorners());
	}


	@Test
	public void testIsPointInShape()
	{
		var quad = buildQuadrilateral();
		List.of(
				Vector2.fromXY(0, 0),
				Vector2.fromXY(0, 1.5),
				Vector2.fromXY(1, 1),
				Vector2.fromXY(-2, 1)
		).forEach(p -> assertThat(quad.isPointInShape(p)).isTrue());
		List.of(
				Vector2.fromXY(0, 3),
				Vector2.fromXY(2, -1),
				Vector2.fromXY(2, 2),
				Vector2.fromXY(-3, 1)
		).forEach(p -> assertThat(quad.isPointInShape(p)).isFalse());
	}


	@Test
	public void testWithMargin()
	{
		var quad = buildQuadrilateral();
		var polyBuilder = new PolygonBuilder();
		getCorners().forEach(polyBuilder::addPoint);


		var margin = 0.1;
		var poly = polyBuilder.build().withMargin(margin);
		var withMargin = quad.withMargin(margin);
		assertThat(withMargin.getCorners()).containsExactlyInAnyOrderElementsOf(
				poly.getPoints()
		);

		margin = -0.1;
		poly = polyBuilder.build().withMargin(margin);
		withMargin = quad.withMargin(margin);
		assertThat(withMargin.getCorners()).containsExactlyInAnyOrderElementsOf(
				poly.getPoints()
		);
	}


	@Test
	public void testGetPerimeterPath()
	{
		var quad = buildQuadrilateral();
		var path = quad.getPerimeterPath();
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
		var perimeter = buildQuadrilateral().getPerimeterPath();
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
		assertThat(buildQuadrilateral().getPerimeterLength()).isCloseTo(13.6568542495, within(1e-10));
	}


	@Test
	public void testPointsAroundPerimeter()
	{
		var quad = buildQuadrilateral();

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(-1, 1.999), Vector2.fromXY(-1, 2.001)),
				Lines.segmentFromPoints(Vector2.fromXY(0.999, 0.999), Vector2.fromXY(1.001, 1.001))
		);

		for (var segment : segments)
		{
			assertThat(quad.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(quad.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(quad.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(quad.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(quad.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(quad.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(quad.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(quad.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(quad.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());
		}
	}


	@Test
	public void testIntersectPerimeterPathLine()
	{
		var quad = buildQuadrilateral();
		var line = Lines.lineFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(quad.intersectPerimeterPath(line)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 1),
				Vector2.fromXY(-0.85714, -0.85714)
		);
	}


	@Test
	public void testIntersectPerimeterPathHalfLine()
	{
		var quad = buildQuadrilateral();
		var halfLine = Lines.halfLineFromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		assertThat(quad.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 1)
		);
		halfLine = Lines.halfLineFromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(-1, -1));
		assertThat(quad.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(-0.85714, -0.85714)
		);
	}


	@Test
	public void testIntersectPerimeterPathLineSegment()
	{
		var quad = buildQuadrilateral();
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(quad.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 1),
				Vector2.fromXY(-0.85714, -0.85714)
		);
		segment = Lines.segmentFromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		assertThat(quad.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(1, 1)
		);
		segment = Lines.segmentFromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(0.5, 0.5));
		assertThat(quad.intersectPerimeterPath(segment)).isEmpty();
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		var quad = buildQuadrilateral();
		var circle = Circle.createCircle(Vector2.fromY(1), 2);
		assertThat(quad.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1.73205, 2),
				Vector2.fromXY(-1.96307, 0.61742),
				Vector2.fromXY(-0.91693, -0.77742),
				Vector2.fromXY(1.82288, 0.17712)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		var quad = buildQuadrilateral();

		var arc = Arc.createArc(Vector2.fromY(1), 2, 0, -AngleMath.PI);
		assertThat(quad.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1.96307, 0.61742),
				Vector2.fromXY(-0.91693, -0.77742),
				Vector2.fromXY(1.82288, 0.17712)
		);
		arc = Arc.createArc(Vector2.fromY(1), 2, 0, AngleMath.PI);
		assertThat(quad.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(-1.73205, 2)
		);
		arc = Arc.createArc(Vector2.fromY(1), 2, 0, AngleMath.PI_HALF);
		assertThat(quad.intersectPerimeterPath(arc)).isEmpty();
	}


	@Test
	public void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(buildQuadrilateral(), true);
	}
}

