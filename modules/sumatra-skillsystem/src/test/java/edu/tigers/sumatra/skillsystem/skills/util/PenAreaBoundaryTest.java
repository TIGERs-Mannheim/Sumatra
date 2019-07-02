package edu.tigers.sumatra.skillsystem.skills.util;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class PenAreaBoundaryTest
{
	private PenAreaBoundary penAreaBoundary = PenAreaBoundary.ownWithMargin(10);
	
	
	@Test
	public void closestPoint()
	{
		assertThat(penAreaBoundary.closestPoint(Vector2.zero()))
				.isEqualTo(Vector2.fromX(penAreaBoundary.getCorner1().x()));
		assertThat(penAreaBoundary.closestPoint(Vector2.fromY(-50)))
				.isEqualTo(Vector2.fromXY(penAreaBoundary.getCorner1().x(), -50));
		assertThat(penAreaBoundary.closestPoint(Vector2.fromXY(penAreaBoundary.getCorner1().x() - 50, -5000)))
				.isEqualTo(Vector2.fromXY(penAreaBoundary.getCorner1().x() - 50, penAreaBoundary.getCorner1().y()));
		assertThat(penAreaBoundary.closestPoint(Vector2.fromXY(penAreaBoundary.getCorner1().x() - 50, 5000)))
				.isEqualTo(Vector2.fromXY(penAreaBoundary.getCorner1().x() - 50, penAreaBoundary.getCorner2().y()));
		assertThat(penAreaBoundary
				.closestPoint(Vector2.fromXY(penAreaBoundary.getStart().x() - 100, -penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x(), penAreaBoundary.getStart().y()));
		assertThat(penAreaBoundary
				.closestPoint(Vector2.fromXY(penAreaBoundary.getStart().x() - 100, penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x(), penAreaBoundary.getEnd().y()));
	}
	
	
	@Test
	public void projectPoint()
	{
		// from center point
		assertThat(penAreaBoundary.projectPoint(Vector2.zero()))
				.isEqualTo(Vector2.fromX(penAreaBoundary.getCorner1().x()));
		
		// front
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getCorner1().x() + penAreaBoundary.getDepth(), -100)))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getCorner1().x(), -50));
		// side
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getStart().x() + 100, -penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x() + 50, penAreaBoundary.getStart().y()));
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getStart().x() + 100, penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x() + 50, penAreaBoundary.getEnd().y()));
		
		// inside
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getCorner1().x() - penAreaBoundary.getDepth() / 2, -100)))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getCorner1().x(), -200));
		
		// outside field
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getStart().x() - 100, -penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x(), penAreaBoundary.getStart().y()));
		assertThat(penAreaBoundary
				.projectPoint(Vector2.fromXY(penAreaBoundary.getStart().x() - 100, penAreaBoundary.getWidth())))
						.isEqualTo(Vector2.fromXY(penAreaBoundary.getStart().x(), penAreaBoundary.getEnd().y()));
	}
	
	
	@Test
	public void nextToInPositiveDirection()
	{
		IVector2 current = penAreaBoundary.getStart();
		double margin = 5;
		for (int i = 0; i < 10000; i++)
		{
			Optional<IVector2> next = penAreaBoundary.nextTo(current, margin, 1);
			if (next.isPresent())
			{
				final double distance = current.distanceTo(next.get());
				assertThat(distance)
						.describedAs("Distance should be equal to margin, but was %.2f in iteration %d", distance, i)
						.isCloseTo(margin, within(1e-10));
				current = next.get();
			} else
			{
				assertThat(current.distanceTo(penAreaBoundary.getEnd())).isLessThan(margin);
				return;
			}
		}
		fail("End of boundary not reached");
	}
	
	
	@Test
	public void nextToInNegativeDirection()
	{
		IVector2 current = penAreaBoundary.getEnd();
		double margin = 5;
		for (int i = 0; i < 10000; i++)
		{
			Optional<IVector2> next = penAreaBoundary.nextTo(current, margin, -1);
			if (next.isPresent())
			{
				final double distance = current.distanceTo(next.get());
				assertThat(distance)
						.describedAs("Distance should be equal to margin, but was %.2f in iteration %d", distance, i)
						.isCloseTo(margin, within(1e-10));
				current = next.get();
			} else
			{
				assertThat(current.distanceTo(penAreaBoundary.getStart())).isLessThan(margin);
				return;
			}
		}
		fail("End of boundary not reached");
	}
	
	
	@Test
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public void nextTo()
	{
		double stepSize = 3;
		double margin = Geometry.getBotRadius() * 2;
		for (ILineSegment edge : penAreaBoundary.getEdges())
		{
			int steps = (int) ((edge.getLength() - margin) / stepSize);
			for (int i = 0; i < steps; i++)
			{
				final IVector2 current = edge.stepAlongLine((i + 1) * stepSize);
				final Optional<IVector2> next = penAreaBoundary.nextTo(current, margin, 1);
				assertThat(next)
						.as("Point exists for i=%d", i)
						.isPresent();
				assertThat(current.distanceTo(next.get()))
						.as("Distance between points is equal to margin for i=%d", i)
						.isCloseTo(margin, within(1e-10));
			}
		}
	}
	
	
	@Test
	public void distanceBetween()
	{
		double x = penAreaBoundary.getCorner1().x();
		double y = penAreaBoundary.getCorner2().y();
		
		// front
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x, 50), Vector2.fromXY(x, 0)))
				.isCloseTo(50, within(1e-10));
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x, -50), Vector2.fromXY(x, -100)))
				.isCloseTo(50, within(1e-10));
		
		// side
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 100, y)))
				.isCloseTo(50, within(1e-10));
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 100, -y)))
				.isCloseTo(50, within(1e-10));
		
		// negative side -> front
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x, -y + 80)))
				.isCloseTo(130, within(1e-10));
		// negative side -> positive side
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 10, y)))
				.isCloseTo(penAreaBoundary.getWidth() + 60, within(1e-10));
		// positive side -> front
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 50, y), Vector2.fromXY(x, y - 80)))
				.isCloseTo(130, within(1e-10));
		// positive side -> negative side
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x - 10, y), Vector2.fromXY(x - 50, -y)))
				.isCloseTo(penAreaBoundary.getWidth() + 60, within(1e-10));
		// front -> negative side
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x, -y + 80), Vector2.fromXY(x - 50, -y)))
				.isCloseTo(130, within(1e-10));
		// front -> positive side
		assertThat(penAreaBoundary.distanceBetween(Vector2.fromXY(x, y - 80), Vector2.fromXY(x - 50, y)))
				.isCloseTo(130, within(1e-10));
	}
	
	
	@Test
	public void compare()
	{
		double x = penAreaBoundary.getCorner1().x();
		double y = penAreaBoundary.getCorner2().y();
		
		// equal
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, 0), Vector2.fromXY(x, 0))).isEqualTo(0);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, 50))).isEqualTo(0);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 50, y))).isEqualTo(0);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 50, -y))).isEqualTo(0);
		
		// negative side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 100, -y), Vector2.fromXY(x - 99, -y))).isEqualTo(-1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 99, -y), Vector2.fromXY(x - 100, -y))).isEqualTo(1);
		
		// front
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, 51))).isEqualTo(-1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, 51), Vector2.fromXY(x, 50))).isEqualTo(1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, -50), Vector2.fromXY(x, -49))).isEqualTo(-1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, -49), Vector2.fromXY(x, -50))).isEqualTo(1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, -50), Vector2.fromXY(x, 50))).isEqualTo(-1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, -50))).isEqualTo(1);
		
		// positive side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 100, y), Vector2.fromXY(x - 99, y))).isEqualTo(1);
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 99, y), Vector2.fromXY(x - 100, y))).isEqualTo(-1);
		
		// negative side -> front
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x, -y + 80))).isEqualTo(-1);
		// front -> negative side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, -y + 80), Vector2.fromXY(x - 50, -y))).isEqualTo(1);
		
		// positive side -> front
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x, y - 80))).isEqualTo(1);
		// front -> positive side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x, y - 80), Vector2.fromXY(x - 50, y))).isEqualTo(-1);
		
		// negative side -> positive side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 50, y))).isEqualTo(-1);
		// positive side -> negative side
		assertThat(penAreaBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 50, -y))).isEqualTo(1);
	}

	@Test
	public void stepAlongBoundary() {
		List<ILineSegment> edges = penAreaBoundary.getEdges();

		// Do not exceed edge limits
		for (ILineSegment edge : edges) {
			Optional<IVector2> steppedPointPos = penAreaBoundary.stepAlongBoundary(edge.getStart(), edge.getLength() / 2);
			assertThat(steppedPointPos.isPresent() && edge.isPointOnLine(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = penAreaBoundary.stepAlongBoundary(edge.getEnd(), -edge.getLength() / 2);
			assertThat(steppedPointNeg).isEmpty();
		}

		// Move over to next edge
		for (ILineSegment edge : edges.subList(0, edges.size() - 2)) {
			Optional<IVector2> steppedPointPos = penAreaBoundary.stepAlongBoundary(edge.getStart(), edge.getLength() + 10);
			assertThat(steppedPointPos.isPresent() && edges.get(edges.indexOf(edge) + 1).isPointOnLine(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = penAreaBoundary.stepAlongBoundary(edge.getEnd(), -(edge.getLength() + 10));
			assertThat(steppedPointNeg).isEmpty();
		}

		// Return empty optional if calculated point would would be behind last edge
		ILineSegment edge = edges.get(edges.size() - 1);
		Optional<IVector2> steppedPoint = penAreaBoundary.stepAlongBoundary(edge.getStart(), edge.getLength() + 10);
		assertThat(steppedPoint).isEmpty();

		// Return point after stepping over whole length
		double penAreaLength = penAreaBoundary.getEdges().stream().mapToDouble(ILineSegment::getLength).sum();
		steppedPoint = penAreaBoundary.stepAlongBoundary(penAreaBoundary.getStart(), penAreaLength - 1);
		assertThat(steppedPoint.isPresent() && edges.get(edges.size() - 1).isPointOnLine(steppedPoint.get())).isTrue();
	}
}