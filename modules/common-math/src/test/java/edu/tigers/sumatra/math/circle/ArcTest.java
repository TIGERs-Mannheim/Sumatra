/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPathComplianceChecker;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class ArcTest
{
	@Test
	public void testIsPointInShape()
	{
		var arc = Arc.createArc(Vector2.fromX(2), 1, 0, AngleMath.PI);
		assertThat(arc.isPointInShape(Vector2.fromXY(2, 0))).isTrue();
		assertThat(arc.isPointInShape(Vector2.fromXY(3, 0))).isTrue();
		assertThat(arc.isPointInShape(Vector2.fromXY(1, 0))).isTrue();
		assertThat(arc.isPointInShape(Vector2.fromXY(2, 1))).isTrue();
		assertThat(arc.isPointInShape(Vector2.fromXY(2, -1))).isFalse();

		assertThat(arc.isPointInShape(Vector2.fromXY(3.001, 0))).isFalse();
		assertThat(arc.isPointInShape(Vector2.fromXY(0.999, 0))).isFalse();
		assertThat(arc.isPointInShape(Vector2.fromXY(2, 1.001))).isFalse();
		assertThat(arc.isPointInShape(Vector2.fromXY(2, -1.001))).isFalse();
	}


	@Test
	public void testWithMargin()
	{
		var arc = Arc.createArc(Vector2f.fromY(3), 2, 0.12, -0.112);
		var withMargin = arc.withMargin(-0.1);
		assertThat(withMargin.center()).isEqualTo(arc.center());
		assertThat(withMargin.radius()).isCloseTo(1.9, within(1e-6));
		assertThat(withMargin.getStartAngle()).isEqualTo(arc.getStartAngle());
		assertThat(withMargin.getRotation()).isEqualTo(arc.getRotation());
		withMargin = arc.withMargin(0.1);
		assertThat(withMargin.center()).isEqualTo(arc.center());
		assertThat(withMargin.radius()).isCloseTo(2.1, within(1e-10));
		assertThat(withMargin.getStartAngle()).isEqualTo(arc.getStartAngle());
		assertThat(withMargin.getRotation()).isEqualTo(arc.getRotation());
	}


	@Test
	public void testGetPerimeterPath()
	{
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		assertThat(arc.getPerimeterPath()).containsExactlyInAnyOrder(
				arc,
				Lines.segmentFromPoints(Vector2.zero(), Vector2.fromX(1)),
				Lines.segmentFromPoints(Vector2.fromX(-1), Vector2.zero())
		);
	}


	@Test
	public void testPointsAroundPerimeter()
	{
		var arc = Arc.createArc(Vector2.fromX(2), 1, -0.1, AngleMath.PI + 0.2);
		assertThat(arc.nearestPointInside(arc.center())).isEqualTo(arc.center());
		assertThat(arc.nearestPointOnPerimeterPath(arc.center())).isEqualTo(Vector2.fromX(2));
		assertThat(arc.nearestPointOutside(arc.center())).isEqualTo(Vector2.fromX(2));

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(2.999, 0), Vector2.fromXY(3.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(1.001, 0), Vector2.fromXY(0.999, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(2, 0.999), Vector2.fromXY(2, 1.001)),
				Lines.segmentFromPoints(Vector2.fromXY(2, 0.001), Vector2.fromXY(2, -0.001))
		);

		for (var segment : segments)
		{
			assertThat(arc.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(arc.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(arc.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(arc.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(arc.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(arc.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(arc.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(arc.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(arc.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());
		}
	}


	@Test
	public void testPointsAroundPath()
	{

		var arc = Arc.createArc(Vector2.fromX(2), 1, -0.1, AngleMath.PI + 0.2);
		assertThat(arc.nearestPointInside(arc.center())).isEqualTo(arc.center());
		assertThat(arc.nearestPointOnPerimeterPath(arc.center())).isEqualTo(Vector2.fromX(2));
		assertThat(arc.nearestPointOutside(arc.center())).isEqualTo(Vector2.fromX(2));

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(2.999, 0), Vector2.fromXY(3.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(1.001, 0), Vector2.fromXY(0.999, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(2, 0.999), Vector2.fromXY(2, 1.001))
		);

		for (var segment : segments)
		{
			assertThat(arc.closestPointOnPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(arc.closestPointOnPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(arc.closestPointOnPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(arc.distanceTo(segment.getPathStart())).isCloseTo(0.001, within(1e-10));
			assertThat(arc.distanceTo(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(arc.distanceTo(segment.getPathEnd())).isCloseTo(0.001, within(1e-10));

			assertThat(arc.distanceToSqr(segment.getPathStart())).isCloseTo(0.000001, within(1e-10));
			assertThat(arc.distanceToSqr(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(arc.distanceToSqr(segment.getPathEnd())).isCloseTo(0.000001, within(1e-10));

			assertThat(arc.isPointOnPath(segment.getPathStart())).isFalse();
			assertThat(arc.isPointOnPath(segment.getPathCenter())).isTrue();
			assertThat(arc.isPointOnPath(segment.getPathEnd())).isFalse();
		}
	}


	@Test
	public void testIntersectPerimeterPathLine()
	{
		// Data generated with GeoGebra
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);

		var path = Lines.lineFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(arc.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(0, 0)
		);
	}


	@Test
	public void testIntersectPerimeterPathHalfLine()
	{
		// Data generated with GeoGebra
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);

		var path = Lines.halfLineFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(arc.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(0, 0)
		);
		path = Lines.halfLineFromPoints(Vector2.fromXY(0.1, 0.1), Vector2.fromXY(1, 1));
		assertThat(arc.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711)
		);
	}


	@Test
	public void testIntersectPerimeterPathLineSegment()
	{
		// Data generated with GeoGebra
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);

		var path = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(arc.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(0, 0)
		);
		path = Lines.segmentFromPoints(Vector2.fromXY(0.1, 0.1), Vector2.fromXY(1, 1));
		assertThat(arc.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711)
		);
		path = Lines.segmentFromPoints(Vector2.fromXY(0.1, 0.1), Vector2.fromXY(0.5, 0.5));
		assertThat(arc.intersectPerimeterPath(path)).isEmpty();

	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		// Data generated with GeoGebra
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		var circle = Circle.createCircle(Vector2f.fromXY(1, 1), 1);

		assertThat(arc.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromX(1),
				Vector2.fromY(1)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		// Data generated with GeoGebra
		var arc1 = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		var arc2 = Arc.createArc(Vector2f.fromXY(1, 1), 1, -3 * AngleMath.PI_QUART, AngleMath.PI_QUART);
		assertThat(arc1.intersectPerimeterPath(arc2)).containsExactlyInAnyOrder(
				Vector2.fromX(1)
		);
		arc2 = Arc.createArc(Vector2f.fromXY(1, 1), 1, -3 * AngleMath.PI_QUART, -AngleMath.PI_QUART);
		assertThat(arc1.intersectPerimeterPath(arc2)).containsExactlyInAnyOrder(
				Vector2.fromY(1)
		);
	}


	@Test
	public void testIsValid()
	{
		var center = Vector2f.ZERO_VECTOR;
		var proper = Arc.createArc(center, 1, 0, 0.1);
		var invalid1 = Arc.createArc(center, 1e-6, 0, 0.1);
		var invalid2 = Arc.createArc(center, 1, 0, 0);

		assertThat(proper.isValid()).isTrue();
		assertThat(invalid1.isValid()).isFalse();
		assertThat(invalid2.isValid()).isFalse();
	}


	@Test
	public void testGetPathPoints()
	{
		var radius = 1.0;
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, radius, 0, AngleMath.PI);
		assertThat(arc.getPathStart()).isEqualTo(Vector2.fromX(radius));
		assertThat(arc.getPathEnd()).isEqualTo(Vector2.fromX(-radius));
		assertThat(arc.getPathCenter()).isEqualTo(Vector2.fromY(radius));

		radius = 0.1;
		arc = Arc.createArc(Vector2f.ZERO_VECTOR, radius, 0, AngleMath.PI);
		assertThat(arc.getPathStart()).isEqualTo(Vector2.fromX(radius));
		assertThat(arc.getPathEnd()).isEqualTo(Vector2.fromX(-radius));
		assertThat(arc.getPathCenter()).isEqualTo(Vector2.fromY(radius));
	}


	@Test
	public void testGetPathLength()
	{
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		assertThat(arc.getLength()).isCloseTo(AngleMath.PI, within(1e-6));
		assertThat(arc.getLength()).isEqualTo(arc.getPerimeterLength() - 2);

		arc = Arc.createArc(Vector2f.ZERO_VECTOR, 0.5, 0, AngleMath.PI);
		assertThat(arc.getLength()).isCloseTo(AngleMath.PI_HALF, within(1e-6));
		assertThat(arc.getLength()).isEqualTo(arc.getPerimeterLength() - 1);

		arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI_HALF);
		assertThat(arc.getLength()).isCloseTo(AngleMath.PI_HALF, within(1e-6));
		assertThat(arc.getLength()).isEqualTo(arc.getPerimeterLength() - 2);
	}


	@Test
	public void testStepAlongPath()
	{
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		assertThat(arc.stepAlongPath(0 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(arc.stepAlongPath(1 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromY(1));
		assertThat(arc.stepAlongPath(2 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(-1));
		assertThat(arc.stepAlongPath(3 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromY(-1));
		assertThat(arc.stepAlongPath(4 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(arc.stepAlongPath(8 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(arc.stepAlongPath(100 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
	}


	@Test
	public void testCompliance()
	{
		var arcs = List.of(
				Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI),
				Arc.createArc(Vector2f.ZERO_VECTOR, 0.5, 0, AngleMath.PI),
				Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI_HALF)
		);
		for (var arc : arcs)
		{
			IBoundedPathComplianceChecker.checkCompliance(arc, false);
			I2DShapeComplianceChecker.checkCompliance(arc, true);
		}
	}
}

