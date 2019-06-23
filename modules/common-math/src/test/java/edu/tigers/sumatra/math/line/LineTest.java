/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.Vector2;


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
		assertThat(line1.getYIntercept().isPresent(), is(true));
		assertThat(line1.getYIntercept().get(), IsCloseTo.closeTo(0.0, 1e-6));
		
		
		vecS = Vector2.fromXY(3, 4);
		vecD = Vector2.fromXY(0, 1);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent(), is(false));
		
		vecS = Vector2.fromXY(-11, 1);
		vecD = Vector2.fromXY(-3, 0);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent(), is(true));
		assertThat(line1.getYIntercept().get(), IsCloseTo.closeTo(1.0, 1e-6));
		
		vecS = Vector2.fromXY(-9, 1);
		vecD = Vector2.fromXY(-3, 0.5);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getYIntercept().isPresent(), is(true));
		assertThat(line1.getYIntercept().get(), IsCloseTo.closeTo(-0.5, 1e-6));
	}
	
	
	@Test
	public void textGetSlope()
	{
		Vector2 vecS = Vector2.fromXY(1, 1);
		Vector2 vecD = Vector2.fromXY(1, 1);
		Line line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent(), is(true));
		assertThat(line1.getSlope().get(), IsCloseTo.closeTo(1.0, 1e-6));
		
		vecS = Vector2.fromXY(3, 4);
		vecD = Vector2.fromXY(0, 1);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent(), is(false));
		
		vecS = Vector2.fromXY(-11, 1);
		vecD = Vector2.fromXY(-3, 0);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent(), is(true));
		assertThat(line1.getSlope().get(), IsCloseTo.closeTo(0.0, 1e-6));
		
		vecS = Vector2.fromXY(-9, 1);
		vecD = Vector2.fromXY(-4, 0.5);
		line1 = Line.fromDirection(vecS, vecD);
		assertThat(line1.getSlope().isPresent(), is(true));
		assertThat(line1.getSlope().get(), IsCloseTo.closeTo(-0.125, 1e-6));
	}
	
	
	@Test
	public void testIsPointInFront()
	{
		ILine line = Line.fromDirection(Vector2.fromXY(42, 1337), Vector2.fromX(1));
		assertThat(line.isPointInFront(Vector2.fromXY(42, 1337)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(42.1, 1337)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(43, 1337)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(1000, 1337)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(41, 1337)), is(false));
		
		line = Line.fromDirection(Vector2.fromXY(-1, 2), Vector2.fromXY(1, 1));
		assertThat(line.isPointInFront(Vector2.fromXY(-1, 2)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(0, 3)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(-2, 1)), is(false));
		
		line = Line.fromDirection(Vector2.zero(), Vector2.fromY(5));
		assertThat(line.isPointInFront(Vector2.fromXY(1, 0)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(-1, 0)), is(true));
		assertThat(line.isPointInFront(Vector2.fromXY(1, -1)), is(false));
	}
}
