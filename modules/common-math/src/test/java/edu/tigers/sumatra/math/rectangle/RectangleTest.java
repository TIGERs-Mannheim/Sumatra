/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


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
		
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 0), 2)).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(5, 4), 2)).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(-5, 4), 2)).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 5), 2)).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, -5), 2)).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(4, 4), 2)).isEqualTo(Vector2.fromXY(4, 4));
		
		rect = Rectangle.fromCenter(Vector2.zero(), 10, 10);
		
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 0), -1)).isEqualTo(Vector2.fromXY(0, 0));
		assertThat(rect.nearestPointInside(Vector2.fromXY(5, 4), -1)).isEqualTo(Vector2.fromXY(4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(-5, 4), -1)).isEqualTo(Vector2.fromXY(-4, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, 5), -1)).isEqualTo(Vector2.fromXY(0, 4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(0, -5), -1)).isEqualTo(Vector2.fromXY(0, -4));
		assertThat(rect.nearestPointInside(Vector2.fromXY(4, 4), -1)).isEqualTo(Vector2.fromXY(4, 4));
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
		assertThat(rect.isPointInShape(Vector2.fromXY(22, 22), 1)).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(20, 0), -1)).isTrue();
		assertThat(rect.isPointInShape(Vector2.fromXY(24, 24), 2)).isFalse();
		assertThat(rect.isPointInShape(Vector2.fromXY(20, 0), -2)).isFalse();
	}
	
	
	@Test
	public void testLineIntersections()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		
		assertThat(rect.lineIntersections(
				Line.fromDirection(Vector2.zero(), Vector2.fromX(1)))).containsExactlyInAnyOrder(
						Vector2.fromX(21), Vector2.fromX(-21));
		assertThat(rect.lineIntersections(
				Line.fromDirection(Vector2.zero(), Vector2.fromY(1)))).containsExactlyInAnyOrder(
						Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(rect.lineIntersections(
				Line.fromDirection(Vector2.zero(), Vector2.fromXY(1, 1)))).containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));
		
		assertThat(rect.lineIntersections(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(rect.lineIntersections(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(0, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(rect.lineIntersections(Lines.lineFromDirection(Vector2.zero(), Vector2.fromXY(1, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));
		
		assertThat(rect.lineIntersections(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(1, 0))))
				.containsExactly(Vector2.fromXY(21, 0));
		assertThat(rect.lineIntersections(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(0, -1))))
				.containsExactly(Vector2.fromXY(0, -21));
		assertThat(rect.lineIntersections(Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromXY(1, 1))))
				.containsExactly(Vector2.fromXY(21, 21));
		assertThat(rect.lineIntersections(Lines.halfLineFromDirection(Vector2.fromXY(21.1, 0), Vector2.fromXY(-1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(rect.lineIntersections(Lines.halfLineFromDirection(Vector2.fromXY(0, -21.1), Vector2.fromXY(0, 1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(
				rect.lineIntersections(Lines.halfLineFromDirection(Vector2.fromXY(21.1, 21.1), Vector2.fromXY(-1, -1))))
						.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));
		
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(21.1, 0))))
				.containsExactly(Vector2.fromXY(21, 0));
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(0, -21.1))))
				.containsExactly(Vector2.fromXY(0, -21));
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(21.1, 21.1))))
				.containsExactly(Vector2.fromXY(21, 21));
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(20.9, 0)))).isEmpty();
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(0, -20.9)))).isEmpty();
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.zero(), Vector2.fromXY(20.9, 20.9)))).isEmpty();
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.fromXY(-21.1, 0), Vector2.fromXY(21.1, 0))))
				.containsExactlyInAnyOrder(Vector2.fromXY(21, 0), Vector2.fromXY(-21, 0));
		assertThat(rect.lineIntersections(Lines.segmentFromPoints(Vector2.fromXY(0, 21.1), Vector2.fromXY(0, -21.1))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 21), Vector2.fromXY(0, -21));
		assertThat(
				rect.lineIntersections(Lines.segmentFromPoints(Vector2.fromXY(-21.1, -21.1), Vector2.fromXY(21.1, 21.1))))
						.containsExactlyInAnyOrder(Vector2.fromXY(21, 21), Vector2.fromXY(-21, -21));
		
		
	}
	
	
	@Test
	public void testGetEdges()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		
		// Starting at topLeft, going counter clockwise.
		List<ILine> edges = rect.getEdges();
		assertThat(edges.stream().map(ILine::supportVector))
				.containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21),
						Vector2.fromXY(21, -21),
						Vector2.fromXY(-21, -21),
						Vector2.fromXY(-21, 21));
		
		assertThat(edges.get(0).directionVector().getAngle()).isCloseTo(AngleMath.PI_HALF, within(1e-4));
		assertThat(edges.get(1).directionVector().getAngle()).isCloseTo(0.0, within(1e-4));
		assertThat(edges.get(2).directionVector().getAngle()).isCloseTo(-AngleMath.PI_HALF, within(1e-4));
		assertThat(edges.get(3).directionVector().getAngle()).isCloseTo(AngleMath.PI, within(1e-4));
	}
	
	
	@Test
	public void testGetEdgesAsSegments()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 42, 42);
		
		// Starting at topLeft, going counter clockwise.
		List<ILineSegment> edges = rect.getEdgesAsSegments();
		assertThat(edges.stream().map(ILineSegment::getStart))
				.containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21),
						Vector2.fromXY(21, -21),
						Vector2.fromXY(-21, -21),
						Vector2.fromXY(-21, 21));
		
		assertThat(edges.stream().map(ILineSegment::getEnd))
				.containsExactlyInAnyOrder(
						Vector2.fromXY(21, 21),
						Vector2.fromXY(21, -21),
						Vector2.fromXY(-21, -21),
						Vector2.fromXY(-21, 21));
		
		List<ILine> edgesAsLines = rect.getEdges();
		
		assertThat(edges.get(0).directionVector().getAngle()).isCloseTo(edgesAsLines.get(0).directionVector().getAngle(),
				within(1e-4));
		assertThat(edges.get(1).directionVector().getAngle()).isCloseTo(edgesAsLines.get(1).directionVector().getAngle(),
				within(1e-4));
		assertThat(edges.get(2).directionVector().getAngle()).isCloseTo(edgesAsLines.get(2).directionVector().getAngle(),
				within(1e-4));
		assertThat(edges.get(3).directionVector().getAngle()).isCloseTo(edgesAsLines.get(3).directionVector().getAngle(),
				within(1e-4));
	}
	
	
	@Test
	public void testIsIntersectionWithLine()
	{
		Rectangle rect = Rectangle.fromCenter(Vector2.zero(), 2, 42);
		
		ILine line = Line.fromDirection(Vector2.zero(), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithLine(line)).isTrue();
		
		line = Line.fromDirection(Vector2.fromXY(1, 1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithLine(line)).isTrue();
		
		line = Line.fromDirection(Vector2.fromXY(1, 1), Vector2.fromY(1));
		assertThat(rect.isIntersectingWithLine(line)).isTrue();
		
		line = Line.fromDirection(Vector2.fromXY(2, 1), Vector2.fromY(1));
		assertThat(rect.isIntersectingWithLine(line)).isFalse();
		
		line = Line.fromDirection(Vector2.fromXY(2, 1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithLine(line)).isTrue();
		
		line = Line.fromDirection(Vector2.fromXY(0, 21), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithLine(line)).isTrue();
		
		line = Line.fromDirection(Vector2.fromXY(0, 21.1), Vector2.fromX(1));
		assertThat(rect.isIntersectingWithLine(line)).isFalse();
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
}
