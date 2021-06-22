/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Test method for {@link Line}.
 *
 * @author Malte, Timo, Frieder
 */
public class LineTest
{
	@Test
	public void testGetYIntercept()
	{

		Vector2 vecS = Vector2.fromXY(1, 1);
		Vector2 vecD = Vector2.fromXY(1, 1);
		Line line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent()).isEqualTo(true);
		assertThat(line1.getYIntercept().get()).isCloseTo(0.0, within(1e-6));


		vecS = Vector2.fromXY(3, 4);
		vecD = Vector2.fromXY(0, 1);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent()).isEqualTo(false);

		vecS = Vector2.fromXY(-11, 1);
		vecD = Vector2.fromXY(-3, 0);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent()).isEqualTo(true);
		assertThat(line1.getYIntercept().get()).isCloseTo(1.0, within(1e-6));

		vecS = Vector2.fromXY(-9, 1);
		vecD = Vector2.fromXY(-3, 0.5);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent()).isEqualTo(true);
		assertThat(line1.getYIntercept().get()).isCloseTo(-0.5, within(1e-6));
	}


	@Test
	public void textGetSlope()
	{
		Vector2 vecS = Vector2.fromXY(1, 1);
		Vector2 vecD = Vector2.fromXY(1, 1);
		Line line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent()).isEqualTo(true);
		assertThat(line1.getSlope().get()).isCloseTo(1.0, within(1e-6));

		vecS = Vector2.fromXY(3, 4);
		vecD = Vector2.fromXY(0, 1);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent()).isEqualTo(false);

		vecS = Vector2.fromXY(-11, 1);
		vecD = Vector2.fromXY(-3, 0);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent()).isEqualTo(true);
		assertThat(line1.getSlope().get()).isCloseTo(0.0, within(1e-6));

		vecS = Vector2.fromXY(-9, 1);
		vecD = Vector2.fromXY(-4, 0.5);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent()).isEqualTo(true);
		assertThat(line1.getSlope().get()).isCloseTo(-0.125, within(1e-6));
	}


	@Test
	public void testIsPointInFront()
	{
		ILine line = Line.fromDirection(Vector2.fromXY(42, 1337), Vector2.fromX(1));
		assertThat(line.isPointInFront(Vector2.fromXY(42, 1337))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(42.1, 1337))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(43, 1337))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(1000, 1337))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(41, 1337))).isEqualTo(false);

		line = Line.fromDirection(Vector2.fromXY(-1, 2), Vector2.fromXY(1, 1));
		assertThat(line.isPointInFront(Vector2.fromXY(-1, 2))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(0, 3))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(-2, 1))).isEqualTo(false);

		line = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromY(5));
		assertThat(line.isPointInFront(Vector2.fromXY(1, 0))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(-1, 0))).isEqualTo(true);
		assertThat(line.isPointInFront(Vector2.fromXY(1, -1))).isEqualTo(false);
	}
}
