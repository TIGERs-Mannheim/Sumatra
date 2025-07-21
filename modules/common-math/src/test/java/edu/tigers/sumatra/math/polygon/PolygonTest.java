/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.polygon;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author KaiE
 */
class PolygonTest
{


	/**
	 * DEBUG METHOD to plot the Polygon.... saved as png to the given path
	 */
	@SuppressWarnings("unused")
	private void plotSetup(final String path, final Polygon p,
			final double margin, final double min, final double max, final int reso) throws Exception
	{
		BufferedImage bi = new BufferedImage(reso, reso, BufferedImage.TYPE_INT_ARGB);
		Color blue = Color.BLUE;
		Color green = Color.GREEN;
		Color red = Color.RED;
		for (int i = 0; i < reso; ++i)
		{
			for (int j = 0; j < reso; ++j)
			{

				final IVector2 pnt = Vector2.fromXY(min + ((max - min) * ((double) i / reso)),
						min + ((max - min) * ((double) j / reso)));
				int t = 0;
				t += p.isPointInShape(pnt) ? 1 : 0;
				t += p.withMargin(margin).isPointInShape(pnt) ? 1 : 0;
				bi.setRGB(i, j, t == 0 ? blue.getRGB() : (t == 1 ? green.getRGB() : red.getRGB()));
			}
		}
		// retrieve image
		File outputfile = new File(path);
		ImageIO.write(bi, "png", outputfile);
	}


	@Test
	void testPointInShape()
	{
		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(Vector2.fromXY(0, 0));
		b.addPoint(Vector2.fromXY(1, 0));
		b.addPoint(Vector2.fromXY(1, 1));
		b.addPoint(Vector2.fromXY(0, 1));
		Polygon p = b.build();

		assertTrue(p.isPointInShape(Vector2.fromXY(0, 0)));
		assertTrue(p.isPointInShape(Vector2.fromXY(1, 0)));
		assertTrue(p.isPointInShape(Vector2.fromXY(1, 1)));
		assertTrue(p.isPointInShape(Vector2.fromXY(0, 1)));
		assertTrue(p.isPointInShape(Vector2.fromXY(0.5, 0.5)));
		assertFalse(p.isPointInShape(Vector2.fromXY(2, 2)));
		assertTrue(p.withMargin(1).isPointInShape(Vector2.fromXY(2, 2)));
	}


	@Test
	void testNearestPointOutside()
	{

		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(Vector2.fromXY(-1, -1));
		b.addPoint(Vector2.fromXY(1, -1));
		b.addPoint(Vector2.fromXY(1, 1));
		b.addPoint(Vector2.fromXY(-1, 1));
		Polygon p = b.build();

		// nearest point is the most left point as the distance is always the same... -> first edge is taken
		IVector2 res1 = p.nearestPointOutside(Vector2f.ZERO_VECTOR);
		assertEquals(0, res1.x(), 0.0);
		assertEquals(-1, res1.y(), 0.0);

		IVector2 res2 = p.withMargin(1).nearestPointOutside(Vector2f.X_AXIS);
		assertEquals(2, res2.x(), 0.0);
		assertEquals(0, res2.y(), 0.0);
	}


	private List<IVector2> getPolygonCorners()
	{
		return List.of(
				Vector2.fromXY(-2, 1),
				Vector2.fromXY(1, 3),
				Vector2.fromXY(3, 3),
				Vector2.fromXY(1, 1),
				Vector2.fromXY(2, -1),
				Vector2.fromXY(-3, -2)
		);
	}


	private Polygon buildPolygon()
	{
		var builder = new PolygonBuilder();
		getPolygonCorners().forEach(builder::addPoint);
		return builder.build();
	}


	@Test
	void testIsPointInShape()
	{
		var polygon = buildPolygon();
		List.of(
				Vector2.fromXY(0, 0),
				Vector2.fromXY(0, 2),
				Vector2.fromXY(1, 1),
				Vector2.fromXY(1, 2)
		).forEach(p -> assertThat(polygon.isPointInShape(p)).isTrue());
		List.of(
				Vector2.fromXY(0, 3),
				Vector2.fromXY(2, 0),
				Vector2.fromXY(-3, 0),
				Vector2.fromXY(0, -2)
		).forEach(p -> assertThat(polygon.isPointInShape(p)).isFalse());
	}


	@Test
	void testWithMargin()
	{
		var polygon = buildPolygon();
		var margin = 0.1;
		var withMargin = polygon.withMargin(margin);
		assertThat(withMargin.getCentroid()).isEqualTo(polygon.getCentroid());
		assertThat(withMargin.getPoints()).hasSameSizeAs(polygon.getPoints());
		for (int i = 0; i < polygon.getPoints().size(); ++i)
		{
			var expectedDist = polygon.getCentroid().distanceTo(polygon.getPoints().get(i)) + SumatraMath.sqrt(2) * margin;
			assertThat(withMargin.getCentroid().distanceTo(withMargin.getPoints().get(i))).isCloseTo(expectedDist,
					within(1e-6));
		}

		margin = -0.1;
		withMargin = polygon.withMargin(margin);
		assertThat(withMargin.getCentroid()).isEqualTo(polygon.getCentroid());
		assertThat(withMargin.getPoints()).hasSameSizeAs(polygon.getPoints());
		for (int i = 0; i < polygon.getPoints().size(); ++i)
		{
			var expectedDist = polygon.getCentroid().distanceTo(polygon.getPoints().get(i)) + SumatraMath.sqrt(2) * margin;
			assertThat(withMargin.getCentroid().distanceTo(withMargin.getPoints().get(i))).isCloseTo(expectedDist,
					within(1e-6));
		}
	}


	@Test
	void testGetPerimeterPath()
	{
		var polygon = buildPolygon();
		var path = polygon.getPerimeterPath();
		assertThat(path).hasSameSizeAs(getPolygonCorners());
		for (var corner : getPolygonCorners())
		{
			assertThat(path).anyMatch(p -> p.getPathStart().equals(corner));
			assertThat(path).anyMatch(p -> p.getPathEnd().equals(corner));
		}
	}


	@Test
	void testPerimeterPathOrder()
	{
		var perimeter = buildPolygon().getPerimeterPath();
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
	void testGetPerimeterLength()
	{
		assertThat(buildPolygon().getPerimeterLength()).isCloseTo(18.9313435515, within(1e-10));
	}


	@Test
	void testPointsAroundPerimeter()
	{
		var polygon = buildPolygon();

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(2, 2.999), Vector2.fromXY(2, 3.001)),
				Lines.segmentFromPoints(Vector2.fromXY(1.999, 2.001), Vector2.fromXY(2.001, 1.999))
		);

		for (var segment : segments)
		{
			assertThat(polygon.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(polygon.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(polygon.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(polygon.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(polygon.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(polygon.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(polygon.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(polygon.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(polygon.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());
		}
	}


	@Test
	void testIntersectPerimeterPathLine()
	{
		var polygon = buildPolygon();
		var line = Lines.lineFromPoints(Vector2.fromXY(1, -1), Vector2.fromXY(3, 4));
		assertThat(polygon.intersectPerimeterPath(line)).containsExactlyInAnyOrder(
				Vector2.fromXY(2.6, 3),
				Vector2.fromXY(2.33333, 2.33333),
				Vector2.fromXY(1.44444, 0.11111),
				Vector2.fromXY(0.91304, -1.21739)
		);
	}


	@Test
	void testIntersectPerimeterPathHalfLine()
	{
		var polygon = buildPolygon();
		var halfLine = Lines.halfLineFromPoints(Vector2.fromXY(1, -1), Vector2.fromXY(3, 4));
		assertThat(polygon.intersectPerimeterPath(halfLine)).containsExactlyInAnyOrder(
				Vector2.fromXY(2.6, 3),
				Vector2.fromXY(2.33333, 2.33333),
				Vector2.fromXY(1.44444, 0.11111)
		);
	}


	@Test
	void testIntersectPerimeterPathLineSegment()
	{
		var polygon = buildPolygon();
		var segment = Lines.segmentFromPoints(Vector2.fromXY(1, -1), Vector2.fromXY(3, 4));
		assertThat(polygon.intersectPerimeterPath(segment)).containsExactlyInAnyOrder(
				Vector2.fromXY(2.6, 3),
				Vector2.fromXY(2.33333, 2.33333),
				Vector2.fromXY(1.44444, 0.11111)
		);
	}


	@Test
	void testIntersectPerimeterPathCircle()
	{
		var polygon = buildPolygon();
		var circle = Circle.createCircle(Vector2.fromY(2), 2);
		assertThat(polygon.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromXY(2, 2),
				Vector2.fromXY(1.73205, 3),
				Vector2.fromXY(1.27178, 0.45644),
				Vector2.fromXY(-1.80187, 1.13209)
		);
	}


	@Test
	void testIntersectPerimeterPathArc()
	{
		var polygon = buildPolygon();
		var arc = Arc.createArc(Vector2.fromY(2), 2, 0, -AngleMath.PI);
		assertThat(polygon.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(2, 2),
				Vector2.fromXY(1.27178, 0.45644),
				Vector2.fromXY(-1.80187, 1.13209)
		);
		arc = Arc.createArc(Vector2.fromY(2), 2, 0, AngleMath.PI);
		assertThat(polygon.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(2, 2),
				Vector2.fromXY(1.73205, 3)
		);
		arc = Arc.createArc(Vector2.fromY(2), 2, -AngleMath.PI_HALF, -AngleMath.PI_QUART);
		assertThat(polygon.intersectPerimeterPath(arc)).isEmpty();
	}


	@Test
	void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(buildPolygon(), true);
	}
}

