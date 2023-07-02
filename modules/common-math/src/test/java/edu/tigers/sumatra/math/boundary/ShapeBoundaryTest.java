/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.boundary;


import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.penaltyarea.PenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class ShapeBoundaryTest
{
	private I2DShape shape = new PenaltyArea(Vector2.fromX(-1000), 400, 800);
	private IShapeBoundary shapeBoundary = shape.getShapeBoundary();


	@Test
	public void testProjectPoint()
	{
		double x = -600;
		double y = 400;

		// front
		assertThat(project(x + 100, -y)).isEqualTo(Vector2.fromXY(x, -320));
		assertThat(project(x + 100, 0)).isEqualTo(Vector2.fromXY(x, 0));
		assertThat(project(x + 100, y)).isEqualTo(Vector2.fromXY(x, 320));
		// pos corner
		assertThat(project(x + 100, y + 100)).isEqualTo(Vector2.fromXY(x, y));
		// pos side
		assertThat(project(-600, y + 100)).isEqualTo(Vector2.fromXY(-680, y));
		assertThat(project(-800, y + 100)).isEqualTo(Vector2.fromXY(-840, y));
		assertThat(project(-1000, y + 100)).isEqualTo(Vector2.fromXY(-1000, y));
		// neg corner
		assertThat(project(x + 100, -y - 100)).isEqualTo(Vector2.fromXY(x, -y));
		// neg side
		assertThat(project(-600, -y - 100)).isEqualTo(Vector2.fromXY(-680, -y));
		assertThat(project(-800, -y - 100)).isEqualTo(Vector2.fromXY(-840, -y));
		assertThat(project(-1000, -y - 100)).isEqualTo(Vector2.fromXY(-1000, -y));
	}


	private IVector2 project(double x, double y)
	{
		var goalCenter = Vector2.fromX(-1000);
		return shapeBoundary.projectPoint(goalCenter, Vector2.fromXY(x, y));
	}


	@Test
	public void testNextIntermediateCorner()
	{
		double x = -600;
		double y = 400;

		var cornerPos = Vector2.fromXY(x, y);
		var cornerNeg = Vector2.fromXY(x, -y);

		// front
		assertThat(intermediateCorner(x, 50, x, 0)).isEmpty();
		assertThat(intermediateCorner(x, -50, x, -100)).isEmpty();

		// side
		assertThat(intermediateCorner(x - 50, y, x - 100, y)).isEmpty();
		assertThat(intermediateCorner(x - 50, -y, x - 100, -y)).isEmpty();

		// negative side -> front
		assertThat(intermediateCorner(x - 50, -y, x, -y + 80)).contains(cornerNeg);
		// negative side -> positive side
		assertThat(intermediateCorner(x - 50, -y, x - 10, y)).contains(cornerNeg);
		// positive side -> front
		assertThat(intermediateCorner(x - 50, y, x, y - 80)).contains(cornerPos);
		// positive side -> negative side
		assertThat(intermediateCorner(x - 10, y, x - 50, -y)).contains(cornerPos);
		// front -> negative side
		assertThat(intermediateCorner(x, -y + 80, x - 50, -y)).contains(cornerNeg);
		// front -> positive side
		assertThat(intermediateCorner(x, y - 80, x - 50, y)).contains(cornerPos);
	}


	private Optional<IVector2> intermediateCorner(double x1, double y1, double x2, double y2)
	{
		return shapeBoundary.nextIntermediateCorner(Vector2.fromXY(x1, y1), Vector2.fromXY(x2, y2));
	}


	@Test
	public void testDistanceBetween()
	{
		double x = -600;
		double y = 400;

		// front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, 50), Vector2.fromXY(x, 0))).isCloseTo(50,
				within(1e-10));
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, -50), Vector2.fromXY(x, -100))).isCloseTo(50,
				within(1e-10));

		// side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 100, y))).isCloseTo(50,
				within(1e-10));
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 100, -y))).isCloseTo(50,
				within(1e-10));

		// negative side -> front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x, -y + 80))).isCloseTo(130,
				within(1e-10));
		// negative side -> positive side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 10, y))).isCloseTo(
				60 + 2 * y, within(1e-10));
		// positive side -> front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 50, y), Vector2.fromXY(x, y - 80))).isCloseTo(130,
				within(1e-10));
		// positive side -> negative side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 10, y), Vector2.fromXY(x - 50, -y))).isCloseTo(
				60 + 2 * y, within(1e-10));
		// front -> negative side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, -y + 80), Vector2.fromXY(x - 50, -y))).isCloseTo(130,
				within(1e-10));
		// front -> positive side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, y - 80), Vector2.fromXY(x - 50, y))).isCloseTo(130,
				within(1e-10));
	}


	@Test
	public void testCompare()
	{
		double x = -600;
		double y = 500;

		// equal
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, 0), Vector2.fromXY(x, 0))).isZero();
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, 50))).isZero();
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 50, y))).isZero();
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 50, -y))).isZero();

		// negative side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 100, -y), Vector2.fromXY(x - 99, -y)))
				.isEqualTo(1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 99, -y), Vector2.fromXY(x - 100, -y)))
				.isEqualTo(-1);

		// front
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, 51))).isEqualTo(1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, 51), Vector2.fromXY(x, 50))).isEqualTo(-1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, -50), Vector2.fromXY(x, -49))).isEqualTo(1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, -49), Vector2.fromXY(x, -50))).isEqualTo(-1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, -50), Vector2.fromXY(x, 50))).isEqualTo(1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, 50), Vector2.fromXY(x, -50))).isEqualTo(-1);

		// positive side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 100, y), Vector2.fromXY(x - 99, y))).isEqualTo(-1);
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 99, y), Vector2.fromXY(x - 100, y))).isEqualTo(1);

		// negative side -> front
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x, -y + 80))).isEqualTo(1);
		// front -> negative side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, -y + 80), Vector2.fromXY(x - 50, -y)))
				.isEqualTo(-1);

		// positive side -> front
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x, y - 80))).isEqualTo(-1);
		// front -> positive side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x, y - 80), Vector2.fromXY(x - 50, y))).isEqualTo(1);

		// negative side -> positive side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, -y), Vector2.fromXY(x - 50, y))).isEqualTo(1);
		// positive side -> negative side
		assertThat(shapeBoundary.compare(Vector2.fromXY(x - 50, y), Vector2.fromXY(x - 50, -y))).isEqualTo(-1);
	}


	@Test
	public void testStepAlongBoundary()
	{
		var edges = shapeBoundary.getShape().getPerimeterPath();

		// Do not exceed edge limits
		for (var edge : edges)
		{
			Optional<IVector2> steppedPointPos = shapeBoundary.stepAlongBoundary(edge.getPathStart(),
					edge.getLength() / 2);
			assertThat(steppedPointPos.isPresent() && edge.isPointOnPath(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = shapeBoundary.stepAlongBoundary(edge.getPathEnd(),
					-edge.getLength() / 2);
			assertThat(steppedPointNeg).isEmpty();
		}

		// Move over to next edge
		for (var edge : edges.subList(0, edges.size() - 2))
		{
			Optional<IVector2> steppedPointPos = shapeBoundary.stepAlongBoundary(edge.getPathStart(),
					edge.getLength() + 10);
			assertThat(steppedPointPos.isPresent() && edges.get(edges.indexOf(edge) + 1)
					.isPointOnPath(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = shapeBoundary.stepAlongBoundary(edge.getPathEnd(),
					-(edge.getLength() + 10));
			assertThat(steppedPointNeg).isEmpty();
		}

		// Return empty optional if calculated point would be behind last edge
		var edge = edges.get(edges.size() - 1);
		Optional<IVector2> steppedPoint = shapeBoundary.stepAlongBoundary(edge.getPathStart(), edge.getLength() + 10);
		assertThat(steppedPoint).isEmpty();

		// Return point after stepping over whole length
		double penAreaLength = shapeBoundary.getShape().getPerimeterLength();
		steppedPoint = shapeBoundary.stepAlongBoundary(shapeBoundary.getStart(), penAreaLength - 1);
		assertThat(steppedPoint.isPresent() && edges.get(edges.size() - 1).isPointOnPath(steppedPoint.get())).isTrue();
	}
}