/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Lukas Magel
 */
public class LinesTest
{
	
	@Test
	public void testSegmentFromPoints()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);
		
		ILineSegment lineA = LineSegment.fromPoints(a, b);
		ILineSegment lineB = Lines.segmentFromPoints(a, b);
		assertThat(lineA, is(lineB));
	}
	
	
	@Test
	public void testSegmentFromOffset()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);
		
		ILineSegment lineA = LineSegment.fromOffset(a, b);
		ILineSegment lineB = Lines.segmentFromOffset(a, b);
		assertThat(lineA, is(lineB));
	}
	
	
	@Test
	public void testHalfLineFromDirection()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);
		
		IHalfLine lineA = HalfLine.fromDirection(a, b);
		IHalfLine lineB = Lines.halfLineFromDirection(a, b);
		assertThat(lineA, is(lineB));
	}
	
	
	@Test
	public void testLineFromPoints()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);
		
		ILine lineA = Line.fromPoints(a, b);
		ILine lineB = Lines.lineFromPoints(a, b);
		assertThat(lineA, is(lineB));
	}
	
	
	@Test
	public void testLineFromDirection()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);
		
		ILine lineA = Line.fromDirection(a, b);
		ILine lineB = Lines.lineFromDirection(a, b);
		assertThat(lineA, is(lineB));
	}
	
}
