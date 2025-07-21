/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.botshape;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class BotShapeTest
{
	private IVector2 position = Vector2.fromXY(1, 2);
	private double radius = 0.09;
	private double center2Dribbler = 0.07;
	private double orientation = Math.PI;
	private BotShape botShape = BotShape.fromFullSpecification(position, radius, center2Dribbler, orientation);
	private BotShape botShapeAtCenter = BotShape.fromFullSpecification(Vector2f.ZERO_VECTOR, radius, center2Dribbler,
			orientation);


	@Test
	public void testConstruction()
	{
		assertThat(botShape.center()).isEqualTo(position);
		assertThat(botShape.radius()).isCloseTo(radius, within(1e-10));
		assertThat(botShape.getCenter2Dribbler()).isCloseTo(center2Dribbler, within(1e-10));
		assertThat(botShape.getOrientation()).isCloseTo(orientation, within(1e-10));
	}


	@Test
	public void testMirror()
	{
		IBotShape mirror = botShape.mirror();
		assertThat(mirror.center()).isEqualTo(Vector2.fromXY(-1, -2));
		assertThat(mirror.radius()).isCloseTo(radius, within(1e-10));
		assertThat(mirror.getCenter2Dribbler()).isCloseTo(center2Dribbler, within(1e-10));
		assertThat(mirror.getOrientation()).isCloseTo(Math.PI * 2, within(1e-10));
	}


	@Test
	public void testKickerCenterConversion()
	{
		var botPos = Vector2.fromXY(20, 10);
		double center2DribblerDistance = 10;
		for (double angle = 0; angle <= 360; ++angle)
		{
			var angleRad = AngleMath.normalizeAngle(AngleMath.deg2rad(angle));
			var kicker = BotShape.getKickerCenterPos(botPos, angleRad, center2DribblerDistance);
			var newBotPos = BotShape.getCenterFromKickerPos(kicker, angleRad, center2DribblerDistance);
			var calculatedAngle = AngleMath.normalizeAngle(Vector2.fromPoints(botPos, kicker).getAngle());
			assertThat(botPos.isCloseTo(newBotPos)).isTrue();
			assertThat(botPos.distanceTo(kicker)).isCloseTo(center2DribblerDistance, within(1e-6));
			assertThat(angleRad).isCloseTo(calculatedAngle, within(1e-6));
		}

		var kicker = BotShape.getKickerCenterPos(position, orientation, center2Dribbler);
		assertThat(botShape.getKickerCenterPos().isCloseTo(kicker)).isTrue();
	}


	@Test
	public void testGetKickerLine()
	{
		var kickerCenter = botShape.getKickerCenterPos();
		var kickerWidth = botShape.getKickerWidth();
		assertThat(botShape.getKickerLine().getPathCenter().isCloseTo(kickerCenter)).isTrue();
		assertThat(botShape.getKickerLine().getLength()).isCloseTo(kickerWidth, within(1e-6));
	}


	@Test
	public void testIsPointInShape()
	{
		assertThat(botShape.isPointInShape(botShape.center())).isTrue();
		assertThat(botShape.isPointInShape(botShape.getKickerCenterPos())).isTrue();
		assertThat(botShape.isPointInShape(botShape.getKickerLine().getPathStart())).isTrue();
		assertThat(botShape.isPointInShape(botShape.getKickerLine().getPathEnd())).isTrue();
		assertThat(botShape.isPointInShape(
				botShape.center().addNew(Vector2.fromAngleLength(orientation + AngleMath.PI, radius)))).isTrue();
		assertThat(botShape.isPointInShape(
				botShape.center().addNew(Vector2.fromAngleLength(orientation + AngleMath.PI, radius + 1)))).isFalse();

		assertThat(botShape.isPointInShape(
				botShape.center().addNew(Vector2.fromAngleLength(orientation, center2Dribbler)))).isTrue();
		assertThat(botShape.isPointInShape(
				botShape.center().addNew(Vector2.fromAngleLength(orientation, center2Dribbler + 1)))).isFalse();
	}


	@Test
	public void testWithMargin()
	{
		double factor = 1.1;
		var withMargin = botShape.withMargin(botShape.radius() * factor - botShape.radius());
		assertThat(withMargin.radius()).isCloseTo(botShape.radius() * factor, within(1e-10));
		assertThat(withMargin.getCenter2Dribbler()).isCloseTo(botShape.getCenter2Dribbler() * factor, within(1e-10));
		assertThat(withMargin.center().distanceTo(withMargin.getKickerLine().getPathEnd()))
				.isCloseTo(botShape.radius() * factor, within(1e-10));

		factor = 0.9;
		withMargin = botShape.withMargin(botShape.radius() * factor - botShape.radius());
		assertThat(withMargin.radius()).isCloseTo(botShape.radius() * factor, within(1e-10));
		assertThat(withMargin.getCenter2Dribbler()).isCloseTo(botShape.getCenter2Dribbler() * factor, within(1e-10));
		assertThat(withMargin.center().distanceTo(withMargin.getKickerLine().getPathEnd()))
				.isCloseTo(botShape.radius() * factor, within(1e-10));

	}


	@Test
	public void testGetPerimeterPath()
	{
		var perimeter = botShape.getPerimeterPath();
		var kickerLine = botShape.getKickerLine();
		for (var path : perimeter)
		{
			if (path instanceof IArc arc)
			{
				assertThat(kickerLine.isPointOnPath(arc.getPathStart())).isTrue();
				assertThat(kickerLine.isPointOnPath(arc.getPathEnd())).isTrue();
				assertThat(botShape.center().isCloseTo(arc.center())).isTrue();
			} else if (path instanceof ILineSegment segment)
			{
				assertThat(kickerLine.getPathStart().isCloseTo(segment.getPathStart())).isTrue();
				assertThat(kickerLine.getPathEnd().isCloseTo(segment.getPathEnd())).isTrue();
			}
			assertThat(path.getPathStart().isCloseTo(path.getPathEnd())).isFalse();
		}
	}


	@Test
	public void testGetPerimeterLength()
	{
		var kickerLine = botShape.getKickerLine();
		var centerKickerStart = Vector2.fromPoints(botShape.center(), kickerLine.getPathStart());
		var centerKickerEnd = Vector2.fromPoints(botShape.center(), kickerLine.getPathEnd());
		var kickerAngle = AngleMath.diffAbs(centerKickerStart.getAngle(), centerKickerEnd.getAngle());

		var perimeterLength = botShape.getKickerLine().getLength() + (AngleMath.PI_TWO - kickerAngle) * botShape.radius();

		assertThat(perimeterLength).isCloseTo(botShape.getPerimeterLength(), within(1e-6));
	}


	@Test
	public void testNearestPointOutside()
	{
		var point = position.addNew(Vector2.fromXY(1, 0));
		assertThat(botShape.nearestPointOutside(point)).isEqualTo(point);
		point = position.addNew(Vector2.fromXY(-1, 0));
		assertThat(botShape.nearestPointOutside(point)).isEqualTo(point);
		point = position.addNew(Vector2.fromXY(0, 1));
		assertThat(botShape.nearestPointOutside(point)).isEqualTo(point);
		point = position.addNew(Vector2.fromXY(0, -1));
		assertThat(botShape.nearestPointOutside(point)).isEqualTo(point);
		assertThat(botShape.nearestPointOutside(position).isCloseTo(botShape.getKickerCenterPos())).isTrue();
	}


	@Test
	public void testNearestPointInside()
	{
		var point = position;
		assertThat(botShape.nearestPointInside(point)).isEqualTo(point);
		point = position.addNew(Vector2.fromXY(1, 0));
		assertThat(botShape.nearestPointInside(point).isCloseTo(position.addNew(Vector2.fromX(radius)))).isTrue();
		point = position.addNew(Vector2.fromXY(-1, 0));
		assertThat(botShape.nearestPointInside(point).isCloseTo(botShape.getKickerCenterPos())).isTrue();
		point = position.addNew(Vector2.fromXY(0, 1));
		assertThat(botShape.nearestPointInside(point).isCloseTo(position.addNew(Vector2.fromY(radius)))).isTrue();
		point = position.addNew(Vector2.fromXY(0, -1));
		assertThat(botShape.nearestPointInside(point).isCloseTo(position.addNew(Vector2.fromY(-radius)))).isTrue();
	}


	@Test
	public void testNearestPointOnPerimeterPath()
	{
		var point = position;
		assertThat(botShape.nearestPointOnPerimeterPath(point).isCloseTo(botShape.getKickerCenterPos())).isTrue();
		point = position.addNew(Vector2.fromXY(1, 0));
		assertThat(
				botShape.nearestPointOnPerimeterPath(point).isCloseTo(position.addNew(Vector2.fromX(radius)))).isTrue();
		point = position.addNew(Vector2.fromXY(-1, 0));
		assertThat(botShape.nearestPointOnPerimeterPath(point).isCloseTo(botShape.getKickerCenterPos())).isTrue();
		point = position.addNew(Vector2.fromXY(0, 1));
		assertThat(
				botShape.nearestPointOnPerimeterPath(point).isCloseTo(position.addNew(Vector2.fromY(radius)))).isTrue();
		point = position.addNew(Vector2.fromXY(0, -1));
		assertThat(
				botShape.nearestPointOnPerimeterPath(point).isCloseTo(position.addNew(Vector2.fromY(-radius)))).isTrue();
	}


	@Test
	public void testIntersectPerimeterPathLine()
	{
		// Data generated with GeoGebra
		var path = Lines.lineFromPoints(Vector2.fromX(-0.1), Vector2.fromXY(0.1, 0.1));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(-0.07, 0.015),
				Vector2.fromXY(0.049857, 0.0749284)
		);
	}


	@Test
	public void testIntersectPerimeterPathHalfLine()
	{
		// Data generated with GeoGebra
		var path = Lines.halfLineFromPoints(Vector2.fromX(-0.1), Vector2.fromXY(0.1, 0.1));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(-0.07, 0.015),
				Vector2.fromXY(0.049857, 0.0749284)
		);
		path = Lines.halfLineFromPoints(Vector2.fromY(0.05), Vector2.fromXY(0.1, 0.1));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.049857, 0.0749284)
		);
	}


	@Test
	public void testIntersectPerimeterPathLineSegment()
	{
		// Data generated with GeoGebra
		var path = Lines.segmentFromPoints(Vector2.fromX(-0.1), Vector2.fromXY(0.1, 0.1));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(-0.07, 0.015),
				Vector2.fromXY(0.049857, 0.0749284)
		);
		path = Lines.segmentFromPoints(Vector2.fromY(0.05), Vector2.fromXY(0.1, 0.1));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.049857, 0.0749284)
		);
		path = Lines.segmentFromPoints(Vector2.fromY(0.05), Vector2.fromXY(0.04, 0.07));
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).isEmpty();
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		// Data generated with GeoGebra
		var path = Circle.createCircle(Vector2.fromXY(0.1, 0.1), 0.1);
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.0005, 0.0899),
				Vector2.fromXY(0.0899, 0.0005)
		);
		path = Circle.createCircle(Vector2.fromXY(0.1, 0.1), 0.06);
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.04397, 0.07853),
				Vector2.fromXY(0.07853, 0.04397)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		// Data generated with GeoGebra
		var path = Arc.createArc(Vector2.fromXY(0.1, 0.1), 0.1, -3 * AngleMath.PI_QUART, AngleMath.PI_QUART);
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.0899, 0.0005)
		);
		path = Arc.createArc(Vector2.fromXY(0.1, 0.1), 0.1, -3 * AngleMath.PI_QUART, -AngleMath.PI_QUART);
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.0005, 0.0899)
		);
		path = Arc.createArc(Vector2.fromXY(0.1, 0.1), 0.1, 0, -AngleMath.PI_QUART);
		assertThat(botShapeAtCenter.intersectPerimeterPath(path)).isEmpty();
	}


	@Test
	public void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(botShape, true);
		I2DShapeComplianceChecker.checkCompliance(botShapeAtCenter, true);
	}
}