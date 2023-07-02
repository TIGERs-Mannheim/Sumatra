/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CircleMathTest
{
	@Test
	public void tangentialIntersections()
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(1000, 500), 300);

		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 500)).get(0)
				.distanceTo(Vector2.fromXY(1300, 500))).isLessThan(1e-3);
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 500)).get(1)
				.distanceTo(Vector2.fromXY(1300, 500))).isLessThan(1e-3);

		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(200, 300)).get(0)
				.distanceTo(Vector2.fromXY(961.892, 202.43))).isLessThan(1e-3);
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(200, 300)).get(1)
				.distanceTo(Vector2.fromXY(826.343, 744.629))).isLessThan(1e-3);

		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, -200)).get(0)
				.distanceTo(Vector2.fromXY(1271.052, 371.429))).isLessThan(1e-3);
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, -200)).get(1)
				.distanceTo(Vector2.fromXY(728.948, 371.429))).isLessThan(1e-3);

		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 800)).get(0)
				.distanceTo(Vector2.fromXY(1000, 800))).isLessThan(1e-3);
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 800)).get(1)
				.distanceTo(Vector2.fromXY(1000, 800))).isLessThan(1e-3);
	}


	@Test
	public void nearestPointOutsideCircle()
	{
		double radius = 300;
		double posX = 1000;
		double posY = 500;
		IVector2 origin = Vector2.fromXY(posX, posY);
		ICircle circle = Circle.createCircle(origin, radius);
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(300, 300))))
				.isEqualTo(origin.addNew(Vector2.fromXY(300, 300)));
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(-400, 500))))
				.isEqualTo(origin.addNew(Vector2.fromXY(-400, 500)));
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(250, -300))))
				.isEqualTo(origin.addNew(Vector2.fromXY(250, -300)));
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(-240, -220))))
				.isEqualTo(origin.addNew(Vector2.fromXY(-240, -220)));

		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(100, 0))))
				.isEqualTo(origin.addNew(Vector2.fromXY(radius, 0)));
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(0, -50))))
				.isEqualTo(origin.addNew(Vector2.fromXY(0, -radius)));
		assertThat(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(0, 0))))
				.isEqualTo(origin.addNew(Vector2.fromXY(radius, 0)));
		assertThat((CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(100, 100)))
				.subtractNew(origin.addNew(Vector2.fromAngle(Math.PI / 4).multiply(radius)))).getLength()).isLessThan(1e-3);
	}


	@Test
	public void isPointInCircle()
	{
		double radius = 300;
		double margin = 0.0;
		ICircle circle = Circle.createCircle(Vector2.fromXY(1000, 500), radius);
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1000, 500), margin)).isTrue();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1299, 500), margin)).isTrue();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1000, 799), margin)).isTrue();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1000, 201), margin)).isTrue();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(701, 500), margin)).isTrue();

		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1301, 500), margin)).isFalse();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1000, 801), margin)).isFalse();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1000, 199), margin)).isFalse();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(699, 500), margin)).isFalse();
		assertThat(CircleMath.isPointInCircle(circle, Vector2.fromXY(1250, 750), margin)).isFalse();

		assertOutsideCircle(circle);
	}


	private void assertOutsideCircle(ICircular circular)
	{
		double r = circular.radius();
		assertThat(CircleMath.isPointInCircle(circular, circular.center().addNew(Vector2.fromXY(r, r)), 0.0)).isFalse();
		assertThat(CircleMath.isPointInCircle(circular, circular.center().addNew(Vector2.fromXY(-r, r)), 0.0)).isFalse();
		assertThat(CircleMath.isPointInCircle(circular, circular.center().addNew(Vector2.fromXY(-r, -r)), 0.0)).isFalse();
		assertThat(CircleMath.isPointInCircle(circular, circular.center().addNew(Vector2.fromXY(r, -r)), 0.0)).isFalse();
	}


	@Test
	public void isPointInArc180degStart0deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = 0;
		double rotation = AngleMath.PI;
		IVector2 center = Vector2.fromXY(-100, 500);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArc180degStart90deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = AngleMath.PI_HALF;
		double rotation = AngleMath.PI;
		IVector2 center = Vector2.fromXY(500, 500);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArc180degStartMinus90deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = -AngleMath.PI_HALF;
		double rotation = AngleMath.PI;
		IVector2 center = Vector2.fromXY(-500, -500);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isTrue();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArc90degStart0deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = 0;
		double rotation = AngleMath.PI_HALF;
		IVector2 center = Vector2.fromXY(1000, -100);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArc90degStart180deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = AngleMath.PI;
		double rotation = AngleMath.PI_HALF;
		IVector2 center = Vector2.fromXY(1000, -100);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArc90degStartMinus180deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = -AngleMath.PI;
		double rotation = AngleMath.PI_HALF;
		IVector2 center = Vector2.fromXY(1000, -100);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void isPointInArcMinus90degStart180deg()
	{
		double radius = 300;
		double margin = 0.0;
		double start = AngleMath.PI;
		double rotation = -AngleMath.PI_HALF;
		IVector2 center = Vector2.fromXY(1000, -100);
		IArc arc = Arc.createArc(center, radius, start, rotation);
		assertThat(CircleMath.isPointInArc(arc, center, margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, 50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, 50)), margin)).isTrue();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(-50, -50)), margin)).isFalse();
		assertThat(CircleMath.isPointInArc(arc, center.addNew(Vector2.fromXY(50, -50)), margin)).isFalse();

		assertOutsideCircle(arc);
	}


	@Test
	public void lineIntersectionsArc()
	{
		ILine line;
		IVector2 center = Vector2.fromXY(-200, 100);
		IArc arc = Arc.createArc(center, 300, Math.PI, 3 * Math.PI / 4);

		line = Lines.lineFromPoints(Vector2.fromXY(-800, 200), Vector2.fromXY(200, -200));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(2.762, -121.105), Vector2.fromXY(-499.314, 79.726));

		line = Lines.lineFromPoints(Vector2.fromXY(-800, -200), Vector2.fromXY(200, 200));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-453.009, -61.204));

		line = Lines.lineFromPoints(Vector2.fromXY(200, -200), Vector2.fromXY(-400, 100));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList()).isEmpty();
	}


	@Test
	public void nearestPointOutsideArc()
	{
		IArc arc = Arc.createArc(Vector2f.ZERO_VECTOR, 100, 0, Math.PI / 2);

		assertThat(
				CircleMath.nearestPointOutsideArc(arc, Vector2.fromXY(20, 20)).distanceTo(Vector2.fromXY(70.711, 70.711)))
				.isLessThan(1e-3);

		assertThat(CircleMath.nearestPointOutsideArc(arc, Vector2.fromXY(20, -1)).distanceTo(Vector2.fromXY(20, -1)))
				.isLessThan(1e-3);

	}


	@Test
	public void stepAlongCircle()
	{
		IVector2 center = Vector2.fromXY(100, 100);

		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(300, 100), center, Math.PI / 2)
				.distanceTo(Vector2.fromXY(100, 300))).isLessThan(1e-3);

		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(200, 200), center, Math.PI)
				.distanceTo(Vector2.fromXY(0, 0))).isLessThan(1e-3);

		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(100, 100), center, Math.PI)
				.distanceTo(Vector2.fromXY(100, 100))).isLessThan(1e-3);
	}


	@Test
	public void nearestPointOnArcLine()
	{
		IArc arc = Arc.createArc(Vector2.fromXY(-100, 0), 100, 0, -AngleMath.PI_HALF);
		var nearestPoint = CircleMath.nearestPointOnArcLine(arc, Vector2.fromXY(100, 100));

		IVector2 startPoint = arc.center().addNew(Vector2.fromAngle(arc.getStartAngle()).scaleToNew(arc.radius()));
		IVector2 endPoint = arc.center()
				.addNew(Vector2.fromAngle(arc.getStartAngle() + arc.getRotation()).scaleToNew(arc.radius()));
		assertThat(nearestPoint).isEqualTo(startPoint);

		nearestPoint = CircleMath.nearestPointOnArcLine(arc, Vector2.fromXY(0, 0));
		assertThat(nearestPoint).isEqualTo(Vector2.fromXY(0, 0));

		nearestPoint = CircleMath.nearestPointOnArcLine(arc, Vector2.fromXY(-100, -100));
		assertThat(nearestPoint).isEqualTo(endPoint);

		nearestPoint = CircleMath.nearestPointOnArcLine(arc, Vector2.fromXY(-200, -200));
		assertThat(nearestPoint).isEqualTo(endPoint);

		double radius = 300;
		double posX = 1000;
		double posY = 500;
		IVector2 origin = Vector2.fromXY(posX, posY);
		arc = Arc.createArc(origin, radius, 0, AngleMath.PI_TWO);

		assertThat(CircleMath.nearestPointOnArcLine(arc, origin.addNew(Vector2.fromXY(100, 0))))
				.isEqualTo(origin.addNew(Vector2.fromXY(radius, 0)));
		assertThat(CircleMath.nearestPointOnArcLine(arc, origin.addNew(Vector2.fromXY(0, -50))))
				.isEqualTo(origin.addNew(Vector2.fromXY(0, -radius)));
		assertThat(CircleMath.nearestPointOnArcLine(arc, origin.addNew(Vector2.fromXY(0, 0))))
				.isEqualTo(origin.addNew(Vector2.fromXY(radius, 0)));
	}
}