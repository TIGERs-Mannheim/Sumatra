/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Test class for {@link PenaltyArea}
 */
public class PenaltyAreaTest
{
	private double depth = 400;
	private double length = 800;
	private double goalX = -1000;
	private double borderX = goalX + depth;
	private IVector2 goalCenter = Vector2.fromXY(goalX, 0);
	private PenaltyArea penaltyArea = new PenaltyArea(goalCenter, depth, length);

	private IVector2 center = Vector2f.ZERO_VECTOR;
	private IVector2 penBorderCenter = Vector2.fromXY(borderX, 0);


	@Test
	public void lineIntersectionsWithLineSegment()
	{
		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(center, goalCenter)))
						.as("Does not intersect front border center")
						.containsExactly(penBorderCenter);

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromY(100), goalCenter.addNew(Vector2.fromY(100)))))
						.as("Does not intersect front border positive y")
						.containsExactly(Vector2.fromXY(borderX, 100));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromY(-100), goalCenter.addNew(Vector2.fromY(-100)))))
						.as("Does not intersect front border negative y")
						.containsExactly(Vector2.fromXY(borderX, -100));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromY(100), goalCenter.addNew(Vector2.fromXY(10, 100)))))
						.as("Does not does not intersect goal line")
						.containsExactly(Vector2.fromXY(borderX, 100));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth / 2, length), Vector2.fromXY(goalX + depth / 2, 0))))
						.as("Does not intersect positive side border")
						.containsExactly(Vector2.fromXY(goalX + depth / 2, length / 2));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth / 2, -length), Vector2.fromXY(goalX + depth / 2, 0))))
						.as("Does not intersect negative side border")
						.containsExactly(Vector2.fromXY(goalX + depth / 2, -length / 2));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth * 2, length), goalCenter)))
						.as("Does not intersect positive upper corner")
						.containsExactly(Vector2.fromXY(goalX + depth, length / 2));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(Vector2.fromXY(goalX + depth * 2, -length), goalCenter)))
						.as("Does not intersect negative upper corner")
						.containsExactly(Vector2.fromXY(goalX + depth, -length / 2));

		assertThat(penaltyArea.lineIntersections(
				Lines.segmentFromPoints(center, Vector2.fromX(-100))))
						.as("Intersection when line segment outside")
						.isEmpty();

		assertThat(penaltyArea.lineIntersections(
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

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth * 2, depth * 2)))
				.as("Does not project to positive corner from outside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth * 2, -depth * 2)))
				.as("Does not project to negative corner from outside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 2, depth / 2)))
				.as("Does not project to positive corner from inside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2));

		assertThat(penaltyArea.projectPointOnToPenaltyAreaBorder(Vector2.fromXY(goalX + depth / 2, -depth / 2)))
				.as("Does not project to negative corner from inside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2));

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

		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX - 1, 0)))
				.as("Point outside should be outside")
				.isEqualTo(Vector2.fromXY(goalX - 1, 0));
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
	public void testIntersectionArea()
	{
		// cut half way
		assertThat(penaltyArea.intersectionArea(
				Vector2.fromXY(goalX + depth / 2, -length / 2),
				Vector2.fromXY(goalX + depth / 2, length / 2)))
						.isCloseTo(depth / 2 * length * 1e-6, within(1e-6));
		// intersections on both sides
		assertThat(penaltyArea.intersectionArea(
				Vector2.fromXY(goalX + depth * 0.5, -length / 2),
				Vector2.fromXY(goalX + depth * 0.8, length / 2)))
						.isCloseTo(((depth * 0.2) * length + (depth * 0.3) * length * 0.5) * 1e-6, within(1e-6));
		// intersection on front line
		assertThat(penaltyArea.intersectionArea(
				Vector2.fromXY(goalX + depth * 0.5, -length / 2),
				Vector2.fromXY(goalX + depth, 0)))
						.isCloseTo((depth * 0.5) * (length * 0.5) * 0.5 * 1e-6, within(1e-6));
	}
}
