/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import com.google.common.collect.Collections2;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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
		List<IVector2> intersections = quadrilateral.lineIntersections(line1Intersection);
		assertThat(intersections).hasSize(1);
		assertThat(intersections.get(0).x()).isCloseTo(-1, Percentage.withPercentage(1e-3));
		assertThat(intersections.get(0).y()).isCloseTo(0, Percentage.withPercentage(1e-3));

		// test two intersections
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-5, 0), Vector2.fromXY(5, 0));
		intersections = quadrilateral.lineIntersections(line1Intersection);
		assertThat(intersections).hasSize(2);

		// test no intersections
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-0.99, -0.99), Vector2.fromXY(0.99, 0.99));
		intersections = quadrilateral.lineIntersections(line1Intersection);
		assertThat(intersections).hasSize(0);

		// test no intersections 2
		line1Intersection = Lines.segmentFromPoints(Vector2.fromXY(-3, 5), Vector2.fromXY(3, 5));
		intersections = quadrilateral.lineIntersections(line1Intersection);
		assertThat(intersections).hasSize(0);
	}


	@Test
	public void testEquals()
	{
		EqualsVerifier.forClass(Quadrilateral.class)
				.suppress(Warning.NULL_FIELDS)
				.verify();
	}
}