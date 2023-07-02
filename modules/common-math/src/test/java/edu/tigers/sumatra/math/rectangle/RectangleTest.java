/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author nicolai.ommer
 */
public class RectangleTest
{
	@Test
	public void testFromCenter()
	{
		IVector2 p1 = Vector2.fromXY(-7, -13);
		IRectangle baseRect = Rectangle.fromCenter(p1, 10, 20);

		assertThat(baseRect.xExtent()).isCloseTo(10, Percentage.withPercentage(0.1));
		assertThat(baseRect.yExtent()).isCloseTo(20, Percentage.withPercentage(0.1));

		assertThat(baseRect.getCorners()).containsExactlyInAnyOrder(
				Vector2.fromXY(-12, -23),
				Vector2.fromXY(-12, -3),
				Vector2.fromXY(-2, -23),
				Vector2.fromXY(-2, -3));
	}


	@Test
	public void testWithMargin()
	{
		IVector2 p1 = Vector2.fromXY(-1, -1);
		IVector2 p2 = Vector2.fromXY(1, 1);
		IRectangle baseRect = Rectangle.fromPoints(p1, p2);

		IRectangle marginRect = baseRect.withMargin(2);
		assertThat(marginRect.getCorners()).containsExactlyInAnyOrder(
				Vector2.fromXY(-3, 3),
				Vector2.fromXY(-3, -3),
				Vector2.fromXY(3, 3),
				Vector2.fromXY(3, -3));

		marginRect = baseRect.withMargin(-0.5);
		assertThat(marginRect.getCorners()).containsExactlyInAnyOrder(
				Vector2.fromXY(-0.5, 0.5),
				Vector2.fromXY(-0.5, -0.5),
				Vector2.fromXY(0.5, 0.5),
				Vector2.fromXY(0.5, -0.5));

		baseRect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 40, 40);
		marginRect = baseRect.withMargin(2);
		// (1,1) (44,44)
		assertThat(marginRect.getCorners()).containsExactlyInAnyOrder(
				Vector2.fromXY(-21, -21),
				Vector2.fromXY(-21, 23),
				Vector2.fromXY(23, 23),
				Vector2.fromXY(23, -21));

		baseRect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 44, 44);
		marginRect = baseRect.withMargin(-1);
		// (1,1) (42,42)
		assertThat(marginRect.getCorners()).containsExactlyInAnyOrder(
				Vector2.fromXY(-20, 22),
				Vector2.fromXY(-20, -20),
				Vector2.fromXY(22, 22),
				Vector2.fromXY(22, -20));
	}


	@Test
	public void testToJSON()
	{
		Rectangle rectangle = Rectangle.aroundLine(Vector2.fromX(-1), Vector2.fromX(1), 1);
		assertThat(rectangle.toJSON().toJSONString()).isEqualTo("{\"extent\":[4.0,2.0],\"center\":[0.0,0.0]}");
	}


	@Test
	public void testNearestPointOutside()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 4, 4);

		assertThat(rect.nearestPointOutside(Vector2.fromXY(1, 0))).isEqualTo(Vector2.fromXY(2, 0));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(-1, 0))).isEqualTo(Vector2.fromXY(-2, 0));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(0, -1))).isEqualTo(Vector2.fromXY(0, -2));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(0, 1))).isEqualTo(Vector2.fromXY(0, 2));

		assertThat(rect.nearestPointOutside(Vector2.fromXY(2, 0))).isEqualTo(Vector2.fromXY(2, 0));

		assertThat(rect.nearestPointOutside(Vector2.fromXY(3, 0))).isEqualTo(Vector2.fromXY(3, 0));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(-3, 0))).isEqualTo(Vector2.fromXY(-3, 0));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(0, 3))).isEqualTo(Vector2.fromXY(0, 3));
		assertThat(rect.nearestPointOutside(Vector2.fromXY(0, -3))).isEqualTo(Vector2.fromXY(0, -3));

		assertThat(rect.nearestPointOutside(Vector2.fromXY(1.5, 1.5))).isEqualTo(Vector2.fromXY(2, 1.5));
	}


	@Test
	public void testNearestPointInside()
	{
		IRectangle rect = Rectangle.fromCenter(Vector2.zero(), 8, 8);

		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(-5, 4))).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, -5))).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));

		rect = Rectangle.fromCenter(Vector2.zero(), 4, 4);

		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(-5, 4))).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(0, -5))).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.withMargin(2).nearestPointInside(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));

		rect = Rectangle.fromCenter(Vector2.zero(), 10, 10);

		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(-5, 4))).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(0, -5))).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.withMargin(-1).nearestPointInside(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));
	}


	@Test
	public void testNearestPointOnPerimeterPath()
	{
		IRectangle rect = Rectangle.fromCenter(Vector2.zero(), 8, 8);

		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(4, 0));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(1, 0))).isEqualTo(Vector2.fromXY(4, 0));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(-1, 0))).isEqualTo(Vector2.fromXY(-4, 0));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(0, 1))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(0, -1))).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(-1, 1))).isEqualTo(Vector2.fromXY(-4, 1));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(-5, 4))).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(0, -5))).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.nearestPointOnPerimeterPath(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));

		rect = Rectangle.fromCenter(Vector2.zero(), 4, 4);

		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(4, 0));
		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(-5, 4))).isEqualTo(
				Vector2.fromXY(-4, 4));
		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(0, -5))).isEqualTo(
				Vector2.fromXY(0, -4));
		assertThat(rect.withMargin(2).nearestPointOnPerimeterPath(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));

		rect = Rectangle.fromCenter(Vector2.zero(), 10, 10);

		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(4, 0));
		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(5, 4))).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(-5, 4))).isEqualTo(
				Vector2.fromXY(-4, 4));
		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(0, 5))).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(0, -5))).isEqualTo(
				Vector2.fromXY(0, -4));
		assertThat(rect.withMargin(-1).nearestPointOnPerimeterPath(Vector2.fromXY(4, 4))).isEqualTo(Vector2.fromXY(4, 4));
	}


	@Test
	public void testMinX()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		assertThat(rect.minX()).isEqualTo(-21);

		rect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 42, 42);
		assertThat(rect.minX()).isEqualTo(-20);
	}


	@Test
	public void testMinY()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		assertThat(rect.minY()).isEqualTo(-21);

		rect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 42, 42);
		assertThat(rect.minY()).isEqualTo(-20);
	}


	@Test
	public void testMaxX()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		assertThat(rect.maxX()).isEqualTo(21);

		rect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 42, 42);
		assertThat(rect.maxX()).isEqualTo(22);
	}


	@Test
	public void testMaxY()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		assertThat(rect.maxY()).isEqualTo(21);

		rect = Rectangle.fromCenter(Vector2.fromXY(1, 1), 42, 42);
		assertThat(rect.maxY()).isEqualTo(22);
	}


	@Test
	public void testIsPointInShape()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		assertThat(rect.isPointInShape(Vector2.fromXY(0, 0))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(1000, 1000))).isFalse();
		assertThat(rect.isPointInShape(Vector2.fromXY(21, 21))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(0, 21))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(21, 0))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(-21, -21))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(-21, 0))).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(0, -21))).isTrue();
		assertThat(rect.withMargin(1).isPointInShape(Vector2.fromXY(22, 22))).isTrue();
		assertThat(rect.withMargin(-1).isPointInShape(Vector2.fromXY(20, 0))).isTrue();
		assertThat(rect.withMargin(2).isPointInShape(Vector2.fromXY(24, 24))).isFalse();
		assertThat(rect.withMargin(-2).isPointInShape(Vector2.fromXY(20, 0))).isFalse();
	}


	@Test
	public void testLineIntersections()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);

		assertThat(rect.intersectPerimeterPath(
				Lines.lineFromDirection(Vector2.zero(), Vector2.fromX(1)))).containsExactlyInAnyOrder(
				Vector2.fromX(21), Vector2.fromX(-21));
		assertThat(rect.intersectPerimeterPath(
				Lines.lineFromDirection(Vector2.zero(), Vector2.fromY(1)))).containsExactlyInAnyOrder(
				Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(rect.intersectPerimeterPath(
				Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(1, 1)))).containsExactlyInAnyOrder(
				Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));

		assertThat(rect.intersectPerimeterPath(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(rect.intersectPerimeterPath(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(0, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(rect.intersectPerimeterPath(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(1, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));

		assertThat(rect.intersectPerimeterPath(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(1, 0))))
				.containsExactly(Vector2.fromXY(21, 0));
		assertThat(rect.intersectPerimeterPath(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(0, -1))))
				.containsExactly(Vector2.fromXY(0, -21));
		assertThat(rect.intersectPerimeterPath(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(1, 1))))
				.containsExactly(Vector2.fromXY(21, 21));
		assertThat(
				rect.intersectPerimeterPath(Lines.halfLineFromDirection(Vector2.fromXY(21.1, 0), Vector2.fromXY(-1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(
				rect.intersectPerimeterPath(Lines.halfLineFromDirection(Vector2.fromXY(0, -21.1), Vector2.fromXY(0, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(
				rect.intersectPerimeterPath(
						Lines.halfLineFromDirection(Vector2.fromXY(21.1, 21.1), Vector2.fromXY(-1, -1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));

		assertThat(rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(21.1, 0))))
				.containsExactly(Vector2.fromXY(21, 0));
		assertThat(rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(0, -21.1))))
				.containsExactly(Vector2.fromXY(0, -21));
		assertThat(rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(21.1, 21.1))))
				.containsExactly(Vector2.fromXY(21, 21));
		assertThat(
				rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(20.9, 0)))).isEmpty();
		assertThat(
				rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(0, -20.9)))).isEmpty();
		assertThat(
				rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(20.9, 20.9)))).isEmpty();
		assertThat(
				rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(-21.1, 0), Vector2.fromXY(21.1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(
				rect.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(0, 21.1), Vector2.fromXY(0, -21.1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(
				rect.intersectPerimeterPath(
						Lines.segmentFromPoints(Vector2.fromXY(-21.1, -21.1), Vector2.fromXY(21.1, 21.1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));


	}


	@Test
	public void testGetEdges()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);

		// Starting at topLeft, going counter clockwise.
		List<ILineSegment> edges = rect.getEdges();
		assertThat(edges.stream().map(ILineSegment::getPathStart))
				.containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21),
						Vector2.fromXY(21, -21),
						Vector2.fromXY(-21, -21),
						Vector2.fromXY(-21, 21));

		assertThat(edges.stream().map(ILineSegment::getPathEnd))
				.containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21),
						Vector2.fromXY(21, -21),
						Vector2.fromXY(-21, -21),
						Vector2.fromXY(-21, 21));

		assertThat(edges.stream().map(ILineSegment::directionVector).map(IVector2::getAngle))
				.containsExactlyInAnyOrder(
						-AngleMath.PI_HALF,
						0.0,
						AngleMath.PI_HALF,
						AngleMath.PI
				);
	}


	@Test
	public void testIsIntersectionWithLine()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 2, 42);

		ILine line = Lines.lineFromDirection(Vector2.zero(), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithPath(line)).isTrue();

		line = Lines.lineFromDirection(Vector2.fromXY(1, 1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithPath(line)).isTrue();

		line = Lines.lineFromDirection(Vector2.fromXY(1, 1), Vector2.fromY(1));
		assertThat(rect.isIntersectingWithPath(line)).isTrue();

		line = Lines.lineFromDirection(Vector2.fromXY(2, 1), Vector2.fromY(1));
		assertThat(rect.isIntersectingWithPath(line)).isFalse();

		line = Lines.lineFromDirection(Vector2.fromXY(2, 1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithPath(line)).isTrue();

		line = Lines.lineFromDirection(Vector2.fromXY(0, 21), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithPath(line)).isTrue();

		line = Lines.lineFromDirection(Vector2.fromXY(0, 21.1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithPath(line)).isFalse();
	}


	@Test
	public void testNearestPointInsideWithBuildLine()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 2, 6);
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 0), Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 0), Vector2.fromXY(50, 0))).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(1, 3), Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(1, 3));
		assertThat(rect.nearestPointInside(Vector2.fromXY(2, 0), Vector2.fromXY(0, 0))).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(2, 0), Vector2.fromXY(0.5, 0))).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(2, 1), Vector2.fromXY(0, 1))).isEqualTo(Vector2.fromXY(1, 1));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 5), Vector2.fromXY(0, -10))).isEqualTo(Vector2.fromXY(0, 3));
	}


	@Test
	public void testGetPerimeterPath()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 2, 6);
		var corners = List.of(
				Vector2.fromXY(1, 3),
				Vector2.fromXY(-1, 3),
				Vector2.fromXY(1, -3),
				Vector2.fromXY(-1, -3)
		);
		assertThat(rect.getPerimeterPath()).hasSize(4);
		for (var corner : corners)
		{
			assertThat(rect.getPerimeterPath()).anyMatch(
					sect -> sect.getPathStart().equals(corner)
			);
			assertThat(rect.getPerimeterPath()).anyMatch(
					sect -> sect.getPathEnd().equals(corner)
			);
		}
	}


	@Test
	public void testPerimeterPathOrder()
	{
		var perimeter = Rectangle.fromCenter(Vector2.zero(), 2, 6).getPerimeterPath();
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
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 2, 6);
		assertThat(rect.getPerimeterLength()).isCloseTo(16, within(1e-6));
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		// Data generated with GeoGebra
		var rect = Rectangle.fromCenter(Vector2.zero(), 4, 2);
		var circle = Circle.createCircle(Vector2.fromXY(2, 2), 2);
		assertThat(rect.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromX(2),
				Vector2.fromXY(0.26795, 1)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		// Data generated with GeoGebra
		var rect = Rectangle.fromCenter(Vector2.zero(), 4, 2);
		var arc = Arc.createArc(Vector2.fromXY(2, 2), 2, 0, -AngleMath.PI);
		assertThat(rect.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromX(2),
				Vector2.fromXY(0.26795, 1)
		);
		arc = Arc.createArc(Vector2.fromXY(2, 2), 2, -3 * AngleMath.PI_QUART, -AngleMath.PI_QUART);
		assertThat(rect.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.26795, 1)
		);
		arc = Arc.createArc(Vector2.fromXY(2, 2), 2, -3 * AngleMath.PI_QUART, AngleMath.PI_QUART);
		assertThat(rect.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromX(2)
		);
		arc = Arc.createArc(Vector2.fromXY(2, 2), 2, -3 * AngleMath.PI_QUART, -0.1);
		assertThat(rect.intersectPerimeterPath(arc)).isEmpty();
	}


	@Test
	public void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(Rectangle.fromCenter(Vector2.zero(), 4, 2), true);
	}
}
