/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.penaltyarea;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class PenaltyAreaRoundedCornersTest
{

	private double depth = 400;
	private double length = 800;
	private double radius = 100;
	private double goalX = -1000;
	private double borderX = goalX + depth;
	private IVector2 penBorderCenter = Vector2.fromXY(borderX, 0);
	private IVector2 goalCenter = Vector2.fromXY(goalX, 0);
	private PenaltyAreaRoundedCorners penaltyArea = new PenaltyAreaRoundedCorners(goalCenter, depth, length, radius);
	private IVector2 center = Vector2.zero();


	@Test
	public void lineIntersectionsWithLineSegment()
	{
		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(center, goalCenter)))
				.as("Does not intersect front border center")
				.containsExactly(penBorderCenter);

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromY(100), goalCenter.addNew(Vector2.fromY(100)))))
				.as("Does not intersect front border positive y")
				.containsExactly(Vector2.fromXY(borderX, 100));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromY(-100), goalCenter.addNew(Vector2.fromY(-100)))))
				.as("Does not intersect front border negative y")
				.containsExactly(Vector2.fromXY(borderX, -100));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromY(100), goalCenter.addNew(Vector2.fromXY(10, 100)))))
				.as("Does not does not intersect goal line")
				.containsExactly(Vector2.fromXY(borderX, 100));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth / 2, length), Vector2.fromXY(goalX + depth / 2, 0))))
				.as("Does not intersect positive side border")
				.containsExactly(Vector2.fromXY(goalX + depth / 2, length / 2));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth / 2, -length), Vector2.fromXY(goalX + depth / 2, 0))))
				.as("Does not intersect negative side border")
				.containsExactly(Vector2.fromXY(goalX + depth / 2, -length / 2));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth * 2, length), goalCenter)))
				.as("Does not intersect positive upper corner")
				.containsExactly(Vector2.fromXY(-629.2893218813, 370.7106781187));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth * 2, -length), goalCenter)))
				.as("Does not intersect negative upper corner")
				.containsExactly(Vector2.fromXY(-629.2893218813, -370.7106781187));

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(center, Vector2.fromX(-100))))
				.as("Intersection when line segment outside")
				.isEmpty();

		assertThat(penaltyArea.intersectPerimeterPath(
				Lines.segmentFromPoints(goalCenter.addNew(Vector2.fromXY(-100, length)),
						goalCenter.addNew(Vector2.fromXY(100, -100)))))
				.as("Intersection when line segment crosses goal line only")
				.isEmpty();
	}


	@Test
	public void projectPointOnToPenaltyAreaBorder()
	{
		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(0, 0)))
				.as("Center does not project to front border center")
				.isEqualTo(penBorderCenter);

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(goalCenter))
				.as("Goal center does not project to front border center")
				.isEqualTo(penBorderCenter);

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX - 1, 0)))
				.as("Behind goal center does not project to front border center")
				.isEqualTo(penBorderCenter);

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(borderX + 1, 0)))
				.as("In front of penArea does not project to front border center")
				.isEqualTo(penBorderCenter);

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth * 2, depth)))
				.as("Does not project to positive front from outside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2 / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth * 2, -depth)))
				.as("Does not project to negative front from outside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2 / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(borderX, length / 2)))
				.as("Does not project to positive corner from outside")
				.isEqualTo(Vector2.fromXY(-629.2893218813, 370.7106781187));
		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(borderX, -length / 2)))

				.as("Does not project to negative corner from outside")
				.isEqualTo(Vector2.fromXY(-629.2893218813, -370.7106781187));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 2, depth / 2)))
				.as("Does not project to positive corner from inside")
				.isEqualTo(Vector2.fromXY(-629.2893218813, 370.7106781187));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 2, -depth / 2)))
				.as("Does not project to negative corner from inside")
				.isEqualTo(Vector2.fromXY(-629.2893218813, -370.7106781187));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth, length)))
				.as("Does not project to positive side from outside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth, -length)))
				.as("Does not project to negative side from outside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, -length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 4, length / 4)))
				.as("Does not project to positive side from inside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 4, -length / 4)))
				.as("Does not project to negative side from inside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, -length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX, length)))
				.as("Does not project to lower positive corner")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX, -length)))
				.as("Does not project to lower negative corner")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX - 100, length)))
				.as("Does not project to lower positive corner from outside field")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX - 100, -length)))
				.as("Does not project to lower negative corner from outside field")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));
	}


	@Test
	public void nearestPointOutside()
	{
		assertThat(penaltyArea.nearestPointOutside(center))
				.as("Center is outside and should be returned as-is")
				.isEqualTo(center);

		assertThat(penaltyArea.nearestPointOutside(goalCenter))
				.as("Goal center should be moved to border")
				.isIn(Vector2.fromXY(goalX, length / 2),
						Vector2.fromXY(goalX, -length / 2),
						Vector2.fromXY(goalX + depth, 0));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX + 1, 0)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(borderX, 0));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX, 1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX, -1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(borderX - 1, 1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(borderX, 1));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(borderX - 1, -1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(borderX, -1));

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX - 1, length / 2 + 1)))
				.as("Point outside should be outside")
				.isEqualTo(Vector2.fromXY(goalX - 1, length / 2 + 1));
	}


	@Test
	public void isBehindPenaltyArea()
	{
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(0, 0))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(borderX, 0))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(borderX + 1, 0))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX + 1, 0))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, 0))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, length))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, length / 2))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, length / 4))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, -length))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, -length / 2))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX, -length / 4))).isFalse();

		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, 0))).isTrue();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, length / 2 - 1))).isTrue();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, -length / 2 + 1))).isTrue();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, length / 4))).isTrue();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, -length / 4))).isTrue();

		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, length / 2))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, -length / 2))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, -length))).isFalse();
		assertThat(penaltyArea.isBehindPenaltyArea(Vector2.fromXY(goalX - 1, length))).isFalse();
	}


	@Test
	public void isInsidePenaltyArea()
	{
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX + 1, 0))).isTrue();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX - 1, 0))).isFalse();
		assertThat(penaltyArea.withMargin(10).isPointInShape(Vector2.fromXY(goalX + 1, 0))).isTrue();
		assertThat(penaltyArea.withMargin(10).isPointInShape(Vector2.fromXY(goalX - 1, 0))).isFalse();
	}


	@Test
	public void testIsPointInShape()
	{
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX - 1, 0))).isFalse();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX, 0))).isTrue();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(borderX, 0))).isTrue();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(borderX + 1, 0))).isFalse();

		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX, length / 2 + 1))).isFalse();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX, length / 2))).isTrue();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX, -length / 2))).isTrue();
		assertThat(penaltyArea.isPointInShape(Vector2.fromXY(goalX, -length / 2 - 1))).isFalse();
	}


	@Test
	public void testWithMargin()
	{
		var withMargin = penaltyArea.withMargin(20);
		assertThat(withMargin.getGoalCenter()).isEqualTo(penaltyArea.getGoalCenter());
		assertThat(withMargin.getRectangle().yExtent()).isCloseTo(length + 40, within(1e-6));
		assertThat(withMargin.getRectangle().xExtent()).isCloseTo(depth + 20, within(1e-6));


		withMargin = penaltyArea.withMargin(-20);
		assertThat(withMargin.getGoalCenter()).isEqualTo(penaltyArea.getGoalCenter());
		assertThat(withMargin.getRectangle().yExtent()).isCloseTo(length - 40, within(1e-6));
		assertThat(withMargin.getRectangle().xExtent()).isCloseTo(depth - 20, within(1e-6));
	}


	@Test
	public void testGetPerimeterPath()
	{
		var perimeter = penaltyArea.getPerimeterPath();

		var a = Vector2.fromXY(-1000, 400);
		var b = Vector2.fromXY(-700, 400);
		var c = Vector2.fromXY(-600, 300);
		var d = Vector2.fromXY(-600, -300);
		var e = Vector2.fromXY(-700, -400);
		var f = Vector2.fromXY(-1000, -400);
		var center1 = Vector2.fromXY(-700, 300);
		var center2 = Vector2.fromXY(-700, -300);
		assertThat(perimeter).containsExactlyInAnyOrder(
				Lines.segmentFromPoints(a, b),
				Arc.createArc(center1, radius, AngleMath.PI_HALF, -AngleMath.PI_HALF),
				Lines.segmentFromPoints(c, d),
				Arc.createArc(center2, radius, 0, -AngleMath.PI_HALF),
				Lines.segmentFromPoints(e, f)
		);
	}


	@Test
	public void testPerimeterPathOrder()
	{
		var perimeter = penaltyArea.getPerimeterPath();
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
		assertThat(penaltyArea.getPerimeterLength()).isCloseTo(2 * 157.0796326795 + 1200, within(1e-6));
	}


	@Test
	public void testPointsAroundPerimeter()
	{
		assertThat(penaltyArea.nearestPointInside(penaltyArea.getGoalCenter())).isEqualTo(Vector2.fromXY(goalX, 0));

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(borderX - 0.001, 0), Vector2.fromXY(borderX + 0.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(goalX, length / 2 - 0.001),
						Vector2.fromXY(goalX, length / 2 + 0.001)),
				Lines.segmentFromPoints(Vector2.fromXY(goalX, -length / 2 + 0.001),
						Vector2.fromXY(goalX, -length / 2 - 0.001))
		);

		for (var segment : segments)
		{
			assertThat(penaltyArea.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(penaltyArea.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(penaltyArea.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(penaltyArea.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(penaltyArea.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(penaltyArea.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(penaltyArea.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(penaltyArea.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(
					segment.getPathCenter());
			assertThat(penaltyArea.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(penaltyArea.distanceTo(segment.getPathStart())).isCloseTo(0, within(1e-10));
			assertThat(penaltyArea.distanceTo(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(penaltyArea.distanceTo(segment.getPathEnd())).isCloseTo(0.001, within(1e-10));
		}
	}


	@Test
	public void testIntersectPerimeterPathLine()
	{
		var line = Lines.lineFromDirection(Vector2.zero(), Vector2.fromX(1));
		assertThat(penaltyArea.intersectPerimeterPath(line)).containsExactlyInAnyOrder(
				Vector2.fromX(borderX)
		);

		line = Lines.lineFromDirection(Vector2.fromX(borderX + 1), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(line)).isEmpty();
		line = Lines.lineFromDirection(Vector2.fromX(borderX), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(line)).contains(
				Vector2.fromXY(borderX, 300),
				Vector2.fromXY(borderX, -300)
		);
		line = Lines.lineFromDirection(Vector2.fromX(borderX - 1), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(line)).containsExactlyInAnyOrder(
				Vector2.fromXY(borderX - 1, 314.1067359797),
				Vector2.fromXY(borderX - 1, -314.1067359797)
		);
	}


	@Test
	public void testIntersectPerimeterPathHalfLine()
	{
		var halfLine = Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromX(-1));
		assertThat(penaltyArea.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromX(borderX)
		);
		halfLine = Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromX(1));
		assertThat(penaltyArea.intersectPerimeterPath(halfLine)).isEmpty();

		halfLine = Lines.halfLineFromDirection(Vector2.fromX(borderX + 1), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(halfLine)).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2.fromX(borderX), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(halfLine)).contains(
				Vector2.fromXY(borderX, 300)
		);
		halfLine = Lines.halfLineFromDirection(Vector2.fromX(borderX - 1), Vector2.fromY(1));
		assertThat(penaltyArea.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(borderX - 1, 314.1067359797)
		);
	}


	@Test
	public void testIntersectPerimeterPathLineSegment()
	{
		var segment = Lines.segmentFromPoints(Vector2.fromXY(borderX, length), Vector2.fromXY(goalX, -length));
		assertThat(penaltyArea.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(borderX + 0.25 * (goalX - borderX), length / 2),
				Vector2.fromXY(borderX + 0.75 * (goalX - borderX), -length / 2)
		);
		segment = Lines.segmentFromPoints(Vector2.fromX(goalX), Vector2.zero());
		assertThat(penaltyArea.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromX(borderX)
		);
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		// Data generated with GeoGebra
		var circle = Circle.createCircle(Vector2.fromXY(-400, -400), 400);
		assertThat(penaltyArea.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromXY(-800, -400),
				Vector2.fromXY(-600, -53.5898)
		);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		// Data generated with GeoGebra

		var arc = Arc.createArc(Vector2.fromXY(-400, -400), 400, AngleMath.PI_HALF, AngleMath.PI);
		assertThat(penaltyArea.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(-800, -400),
				Vector2.fromXY(-600, -53.5898)
		);
		arc = Arc.createArc(Vector2.fromXY(-400, -400), 400, AngleMath.PI_HALF, -7 * AngleMath.PI_QUART);
		assertThat(penaltyArea.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(-800, -400)
		);
		arc = Arc.createArc(Vector2.fromXY(-400, -400), 400, AngleMath.PI_HALF, AngleMath.PI_QUART);
		assertThat(penaltyArea.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(-600, -53.5898)
		);
		arc = Arc.createArc(Vector2.fromXY(-400, -400), 400, 3 * AngleMath.PI_QUART, 0.5 * AngleMath.PI_QUART);
		assertThat(penaltyArea.intersectPerimeterPath(arc)).isEmpty();
	}


	@Test
	public void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(penaltyArea, false);
	}
}