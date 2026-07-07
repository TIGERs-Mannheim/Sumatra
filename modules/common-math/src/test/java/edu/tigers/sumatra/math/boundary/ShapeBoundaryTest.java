package edu.tigers.sumatra.math.boundary;


import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.penaltyarea.PenaltyAreaRoundedCorners;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


class ShapeBoundaryTest
{
	private double x = -600;
	private double y = 400;

	private double depth = 400;
	private double length = 800;
	private double radius = 100;

	private I2DShape shape = new PenaltyAreaRoundedCorners(Vector2.fromX(x - depth), depth, length, radius);
	private IShapeBoundary shapeBoundary = shape.getShapeBoundary();

	private IVector2 cornerNegSideArc = Vector2.fromXY(x - 100, -y);
	private IVector2 cornerNegArcFront = Vector2.fromXY(x, -y + 100);
	private IVector2 cornerPosSideArc = Vector2.fromXY(x - 100, y);
	private IVector2 cornerPosArcFront = Vector2.fromXY(x, y - 100);
	private IVector2 posOnFront1 = Vector2.fromXY(x, 0);
	private IVector2 posOnFront2 = Vector2.fromXY(x, 50);
	private IVector2 posOnFront3 = Vector2.fromXY(x, -100);
	private IVector2 posOnPosSide1 = Vector2.fromXY(x - 150, y);
	private IVector2 posOnPosSide2 = Vector2.fromXY(x - 200, y);
	private IVector2 posOnNegSide1 = Vector2.fromXY(x - 150, -y);
	private IVector2 posOnNegSide2 = Vector2.fromXY(x - 200, -y);
	private IVector2 posOnPosArc1 = project(x, y + 100);
	private IVector2 posOnPosArc2 = project(x + 100, y);
	private IVector2 posOnNegArc1 = project(x, -y - 100);
	private IVector2 posOnNegArc2 = project(x + 100, -y);


	@Test
	void testEnvironment()
	{
		assertThat(shapeBoundary.closestPoint(cornerNegSideArc)).isEqualTo(cornerNegSideArc);
		assertThat(shapeBoundary.closestPoint(cornerNegArcFront)).isEqualTo(cornerNegArcFront);
		assertThat(shapeBoundary.closestPoint(cornerPosSideArc)).isEqualTo(cornerPosSideArc);
		assertThat(shapeBoundary.closestPoint(cornerPosArcFront)).isEqualTo(cornerPosArcFront);
		assertThat(shapeBoundary.closestPoint(posOnFront1)).isEqualTo(posOnFront1);
		assertThat(shapeBoundary.closestPoint(posOnFront2)).isEqualTo(posOnFront2);
		assertThat(shapeBoundary.closestPoint(posOnFront3)).isEqualTo(posOnFront3);
		assertThat(shapeBoundary.closestPoint(posOnPosSide1)).isEqualTo(posOnPosSide1);
		assertThat(shapeBoundary.closestPoint(posOnPosSide2)).isEqualTo(posOnPosSide2);
		assertThat(shapeBoundary.closestPoint(posOnNegSide1)).isEqualTo(posOnNegSide1);
		assertThat(shapeBoundary.closestPoint(posOnNegSide2)).isEqualTo(posOnNegSide2);
		assertThat(shapeBoundary.closestPoint(posOnPosArc1)).isEqualTo(posOnPosArc1);
		assertThat(shapeBoundary.closestPoint(posOnPosArc2)).isEqualTo(posOnPosArc2);
		assertThat(shapeBoundary.closestPoint(posOnNegArc1)).isEqualTo(posOnNegArc1);
		assertThat(shapeBoundary.closestPoint(posOnNegArc2)).isEqualTo(posOnNegArc2);
	}


	@Test
	void testProjectPoint()
	{
		// front
		assertThat(project(x + 100, -y + 100)).isEqualTo(Vector2.fromXY(x, -240));
		assertThat(project(x + 100, 0)).isEqualTo(Vector2.fromXY(x, 0));
		assertThat(project(x + 100, y - 100)).isEqualTo(Vector2.fromXY(x, 240));
		// pos corner
		assertThat(posOnPosArc1).isEqualTo(Vector2.fromXY(-681.3965439073913, 398.2543201157607));
		assertThat(project(x + 100, y + 100)).isEqualTo(Vector2.fromXY(-629.2893218813452, 370.7106781186548));
		assertThat(posOnPosArc2).isEqualTo(Vector2.fromXY(-601.7456798842392, 318.6034560926086));
		// pos side
		assertThat(project(-700, y + 100)).isEqualTo(Vector2.fromXY(-760, y));
		assertThat(project(-800, y + 100)).isEqualTo(Vector2.fromXY(-840, y));
		assertThat(project(-1000, y + 100)).isEqualTo(Vector2.fromXY(-1000, y));
		// neg corner
		assertThat(posOnNegArc1).isEqualTo(Vector2.fromXY(-681.3965439073913, -398.2543201157607));
		assertThat(project(x + 100, -y - 100)).isEqualTo(Vector2.fromXY(-629.2893218813452, -370.7106781186548));
		assertThat(posOnNegArc2).isEqualTo(Vector2.fromXY(-601.7456798842392, -318.6034560926086));
		// neg side
		assertThat(project(-700, -y - 100)).isEqualTo(Vector2.fromXY(-760, -y));
		assertThat(project(-800, -y - 100)).isEqualTo(Vector2.fromXY(-840, -y));
		assertThat(project(-1000, -y - 100)).isEqualTo(Vector2.fromXY(-1000, -y));
	}


	private IVector2 project(double x, double y)
	{
		var goalCenter = Vector2.fromX(-1000);
		return shapeBoundary.projectPoint(goalCenter, Vector2.fromXY(x, y));
	}


	@Test
	void testNextIntermediateSingleSide()
	{
		// front
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnFront2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnFront3)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnFront3)).isEmpty();

		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnFront2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnFront1)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnFront1)).isEmpty();

		// side
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide1, posOnPosSide2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide2, posOnPosSide1)).isEmpty();

		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide1, posOnNegSide2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide2, posOnNegSide1)).isEmpty();

		// arcs
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc1, posOnPosArc2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc2, posOnPosArc1)).isEmpty();

		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc1, posOnNegArc2)).isEmpty();
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc2, posOnNegArc1)).isEmpty();
	}


	@Test
	void testNextIntermediateCornerClockwise()
	{
		// negative side -> negative arc
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide1, posOnNegArc1)).contains(cornerNegSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide2, posOnNegArc1)).contains(cornerNegSideArc);
		// negative side -> positive side (going through neg arc, front, pos arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide1, posOnPosSide1)).contains(cornerNegSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegSide2, posOnPosSide1)).contains(cornerNegSideArc);
		// negative arc -> front
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc1, posOnFront1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc2, posOnFront1)).contains(cornerNegArcFront);
		// negative arc -> positive side (going through front, pos arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc1, posOnPosSide1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc2, posOnPosSide1)).contains(cornerNegArcFront);
		// front -> positive arc
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnPosArc1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnPosArc1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnPosArc1)).contains(cornerPosArcFront);
		// front -> positive side (going through pos arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnPosSide1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnPosSide1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnPosSide1)).contains(cornerPosArcFront);
		// positive arc -> positive side
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc1, posOnPosSide1)).contains(cornerPosSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc2, posOnPosSide1)).contains(cornerPosSideArc);
	}


	@Test
	void testNextIntermediateCornerCounterClockwise()
	{
		// positive side -> positive arc
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide1, posOnPosArc1)).contains(cornerPosSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide2, posOnPosArc1)).contains(cornerPosSideArc);
		// positive side -> negative side (going through pos arc, front, neg arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide1, posOnNegSide1)).contains(cornerPosSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosSide2, posOnNegSide1)).contains(cornerPosSideArc);
		// positive arc -> front
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc1, posOnFront1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc2, posOnFront1)).contains(cornerPosArcFront);
		// positive arc -> negative side (going through front, neg arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc1, posOnNegSide1)).contains(cornerPosArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnPosArc2, posOnNegSide1)).contains(cornerPosArcFront);
		// front -> negative arc
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnNegArc1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnNegArc1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnNegArc1)).contains(cornerNegArcFront);
		// front -> negative side (going neg arc)
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront1, posOnNegSide1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront2, posOnNegSide1)).contains(cornerNegArcFront);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnFront3, posOnNegSide1)).contains(cornerNegArcFront);
		// negative arc -> negative side
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc1, posOnNegSide1)).contains(cornerNegSideArc);
		assertThat(shapeBoundary.nextIntermediateCorner(posOnNegArc2, posOnNegSide1)).contains(cornerNegSideArc);
	}


	@Test
	void testDistanceBetween()
	{
		double arcLength = Arc.createArc(Vector2f.ZERO_VECTOR, radius, 0, AngleMath.PI_HALF).getLength();

		// front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, 50), Vector2.fromXY(x, 0))).isCloseTo(
				50,
				within(1e-10)
		);
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, -50), Vector2.fromXY(x, -100))).isCloseTo(
				50,
				within(1e-10)
		);

		// side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 150, y), Vector2.fromXY(x - 200, y))).isCloseTo(
				50,
				within(1e-10)
		);
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 150, -y), Vector2.fromXY(x - 200, -y))).isCloseTo(
				50,
				within(1e-10)
		);

		// negative side -> front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 150, -y), Vector2.fromXY(x, -y + 180))).isCloseTo(
				130 + arcLength,
				within(1e-10)
		);
		// negative side -> positive side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 150, -y), Vector2.fromXY(x - 110, y))).isCloseTo(
				60 + length - 2 * radius + 2 * arcLength,
				within(1e-10)
		);
		// positive side -> front
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 150, y), Vector2.fromXY(x, y - 180))).isCloseTo(
				130 + arcLength,
				within(1e-10)
		);
		// positive side -> negative side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x - 110, y), Vector2.fromXY(x - 150, -y))).isCloseTo(
				60 + length - 2 * radius + 2 * arcLength,
				within(1e-10)
		);
		// front -> negative side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, -y + 180), Vector2.fromXY(x - 150, -y))).isCloseTo(
				130 + arcLength,
				within(1e-10)
		);
		// front -> positive side
		assertThat(shapeBoundary.distanceBetween(Vector2.fromXY(x, y - 180), Vector2.fromXY(x - 150, y))).isCloseTo(
				130 + arcLength,
				within(1e-10)
		);
	}


	@Test
	void testCompare()
	{
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
	void testStepAlongBoundary()
	{
		var edges = shapeBoundary.getShape().getPerimeterPath();

		// Do not exceed edge limits
		for (var edge : edges)
		{
			Optional<IVector2> steppedPointPos = shapeBoundary.stepAlongBoundary(
					edge.getPathStart(),
					edge.getLength() / 2
			);
			assertThat(steppedPointPos.isPresent() && edge.isPointOnPath(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = shapeBoundary.stepAlongBoundary(
					edge.getPathEnd(),
					-edge.getLength() / 2
			);
			assertThat(steppedPointNeg).isEmpty();
		}

		// Move over to next edge
		for (var edge : edges.subList(0, edges.size() - 2))
		{
			Optional<IVector2> steppedPointPos = shapeBoundary.stepAlongBoundary(
					edge.getPathStart(),
					edge.getLength() + 10
			);
			assertThat(steppedPointPos.isPresent() && edges.get(edges.indexOf(edge) + 1)
					.isPointOnPath(steppedPointPos.get())).isTrue();

			Optional<IVector2> steppedPointNeg = shapeBoundary.stepAlongBoundary(
					edge.getPathEnd(),
					-(edge.getLength() + 10)
			);
			assertThat(steppedPointNeg).isEmpty();
		}

		// Return empty optional if calculated point would be behind last edge
		var edge = edges.getLast();
		Optional<IVector2> steppedPoint = shapeBoundary.stepAlongBoundary(edge.getPathStart(), edge.getLength() + 10);
		assertThat(steppedPoint).isEmpty();

		// Return point after stepping over whole length
		double penAreaLength = shapeBoundary.getShape().getPerimeterLength();
		steppedPoint = shapeBoundary.stepAlongBoundary(shapeBoundary.getStart(), penAreaLength - 1);
		assertThat(steppedPoint.isPresent() && edges.getLast().isPointOnPath(steppedPoint.get())).isTrue();
	}
}