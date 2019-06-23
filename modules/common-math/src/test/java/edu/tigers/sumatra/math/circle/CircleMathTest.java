/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircleMathTest
{
	@Test
	public void tangentialIntersections() throws Exception
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(1000, 500), 300);
		
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 500)).get(0)
				.distanceTo(Vector2.fromXY(1300, 500)) < 1e-3).isTrue();
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 500)).get(1)
				.distanceTo(Vector2.fromXY(1300, 500)) < 1e-3).isTrue();
		
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(200, 300)).get(0)
				.distanceTo(Vector2.fromXY(961.892, 202.43)) < 1e-3).isTrue();
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(200, 300)).get(1)
				.distanceTo(Vector2.fromXY(826.343, 744.629)) < 1e-3).isTrue();
		
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, -200)).get(0)
				.distanceTo(Vector2.fromXY(1271.052, 371.429)) < 1e-3).isTrue();
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, -200)).get(1)
				.distanceTo(Vector2.fromXY(728.948, 371.429)) < 1e-3).isTrue();
		
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 800)).get(0)
				.distanceTo(Vector2.fromXY(1000, 800)) < 1e-3).isTrue();
		assertThat(CircleMath.tangentialIntersections(circle, Vector2.fromXY(1000, 800)).get(1)
				.distanceTo(Vector2.fromXY(1000, 800)) < 1e-3).isTrue();
	}
	
	
	@Test
	public void lineIntersectionsCircle() throws Exception
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(-300, 200), 100);
		ILine line;
		
		line = Line.fromPoints(Vector2.fromXY(400, 400), Vector2.fromXY(0, 300));
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).get(0).distanceTo(Vector2.fromXY(-400, 200)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).get(1)
				.distanceTo(Vector2.fromXY(-211.765, 247.059)) < 1e-3).isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(-700, 500), Vector2.fromXY(-500, 300));
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).get(0).distanceTo(Vector2.fromXY(-300, 100)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).get(1).distanceTo(Vector2.fromXY(-400, 200)) < 1e-3)
				.isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(-400, 600), Vector2.fromXY(-400, 100));
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).get(0).distanceTo(Vector2.fromXY(-400, 200)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).size() == 1).isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(100, 200), Vector2.fromXY(0, 100));
		assertThat(CircleMath.lineIntersectionsCircle(circle, line).isEmpty()).isTrue();
	}
	
	
	@Test
	public void lineSegmentIntersections() throws Exception
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(200, -200), 100);
		ILine line;
		
		line = Line.fromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(200, -200));
		assertThat(CircleMath.lineSegmentIntersections(circle, line).get(0)
				.distanceTo(Vector2.fromXY(129.289, -129.289)) < 1e-3).isTrue();
		assertThat(CircleMath.lineSegmentIntersections(circle, line).size() == 1).isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(200, 0), Vector2.fromXY(0, -400));
		assertThat(CircleMath.lineSegmentIntersections(circle, line).get(0).distanceTo(Vector2.fromXY(100, -200)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineSegmentIntersections(circle, line).get(1).distanceTo(Vector2.fromXY(140, -120)) < 1e-3)
				.isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(400, -300), Vector2.fromXY(0, -300));
		assertThat(CircleMath.lineSegmentIntersections(circle, line).get(0).distanceTo(Vector2.fromXY(200, -300)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineSegmentIntersections(circle, line).size() == 1).isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(100, -100), Vector2.fromXY(0, 0));
		assertThat(CircleMath.lineSegmentIntersections(circle, line).isEmpty()).isTrue();
		
	}
	
	
	@Test
	public void nearestPointOutsideCircle() throws Exception
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
		assertThat(
				(CircleMath.nearestPointOutsideCircle(circle, origin.addNew(Vector2.fromXY(100, 100)))
						.subtractNew(origin.addNew(Vector2.fromAngle(Math.PI / 4).multiply(radius)))).getLength() < 1e-3)
								.isTrue();
	}
	
	
	@Test
	public void isPointInCircle() throws Exception
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
	public void isPointInArc180degStart0deg() throws Exception
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
	public void isPointInArc180degStart90deg() throws Exception
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
	public void isPointInArc180degStartMinus90deg() throws Exception
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
	public void isPointInArc90degStart0deg() throws Exception
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
	public void isPointInArc90degStart180deg() throws Exception
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
	public void isPointInArc90degStartMinus180deg() throws Exception
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
	public void isPointInArcMinus90degStart180deg() throws Exception
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
	public void lineIntersectionsArc() throws Exception
	{
		ILine line;
		IVector2 center = Vector2.fromXY(-200, 100);
		IArc arc = Arc.createArc(center, 300, Math.PI, 3 * Math.PI / 4);
		
		line = Line.fromPoints(Vector2.fromXY(-800, 200), Vector2.fromXY(200, -200));
		assertThat(CircleMath.lineIntersectionsArc(arc, line).get(0).distanceTo(Vector2.fromXY(2.762, -121.105)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineIntersectionsArc(arc, line).get(1).distanceTo(Vector2.fromXY(-499.314, 79.726)) < 1e-3)
				.isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(-800, -200), Vector2.fromXY(200, 200));
		assertThat(CircleMath.lineIntersectionsArc(arc, line).get(0).distanceTo(Vector2.fromXY(-453.009, -61.204)) < 1e-3)
				.isTrue();
		assertThat(CircleMath.lineIntersectionsArc(arc, line).size() == 1).isTrue();
		
		line = Line.fromPoints(Vector2.fromXY(200, -200), Vector2.fromXY(-400, 100));
		assertThat(CircleMath.lineIntersectionsArc(arc, line).isEmpty()).isTrue();
	}
	
	
	@Test
	public void nearestPointOutsideArc() throws Exception
	{
		IArc arc = Arc.createArc(Vector2.ZERO_VECTOR, 100, 0, Math.PI / 2);
		
		assertThat(
				CircleMath.nearestPointOutsideArc(arc, Vector2.fromXY(20, 20)).distanceTo(Vector2.fromXY(70.711, 70.711)))
						.isLessThan(1e-3);
		
		assertThat(CircleMath.nearestPointOutsideArc(arc, Vector2.fromXY(20, -1)).distanceTo(Vector2.fromXY(20, -1)))
				.isLessThan(1e-3);
		
	}
	
	
	@Test
	public void stepAlongCircle() throws Exception
	{
		IVector2 center = Vector2.fromXY(100, 100);
		
		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(300, 100), center, Math.PI / 2)
				.distanceTo(Vector2.fromXY(100, 300)) < 1e-3).isTrue();
		
		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(200, 200), center, Math.PI)
				.distanceTo(Vector2.fromXY(0, 0)) < 1e-3).isTrue();
		
		assertThat(CircleMath.stepAlongCircle(Vector2.fromXY(100, 100), center, Math.PI)
				.distanceTo(Vector2.fromXY(100, 100)) < 1e-3).isTrue();
	}
}