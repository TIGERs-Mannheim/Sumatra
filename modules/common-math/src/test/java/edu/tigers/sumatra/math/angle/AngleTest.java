/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author DominikE
 */
public class AngleTest
{
	
	public static final double ACCURACY = 1e-6;
	
	
	@Test
	public void zero()
	{
		assertEquals(0, Angle.zero().asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofRad()
	{
		assertEquals(0, Angle.ofRad(0).asRad(), ACCURACY);
		assertEquals(0, Angle.ofRad(Math.PI * 2).asRad(), ACCURACY);
		assertEquals(0, Angle.ofRad(Math.PI * 100).asRad(), ACCURACY);
		assertEquals(90, Angle.ofRad(Math.PI / 2).asDeg(), ACCURACY);
		assertEquals(90, Angle.ofRad(Math.PI * -1.5).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void ofDeg()
	{
		assertEquals(0, Angle.ofDeg(0).asRad(), ACCURACY);
		assertEquals(0, Angle.ofDeg(360).asRad(), ACCURACY);
		assertEquals(Math.PI / 2, Angle.ofDeg(90).asRad(), ACCURACY);
		assertEquals(Math.PI / 2, Angle.ofDeg(-270).asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofRotation()
	{
		assertEquals(0, Angle.ofRotation(5).asRad(), ACCURACY);
		assertEquals(-Math.PI, Angle.ofRotation(0.5).asRad(), ACCURACY);
		assertEquals(-Math.PI / 2, Angle.ofRotation(-0.25).asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofVec()
	{
		assertEquals(0, Angle.ofVec(Vector2.fromAngle(0)).asRad(), ACCURACY);
		assertEquals(-1.982297, Angle.ofVec(Vector2.fromAngle(42)).asRad(), ACCURACY);
		assertEquals(Math.PI / 2, Angle.ofVec(Vector2.fromAngle(Math.PI / 2)).asRad(), ACCURACY);
	}
	
	
	@Test
	public void add()
	{
		assertEquals(0, Angle.zero().add(Math.PI / 2).add(Angle.ofDeg(270)).asRad(), ACCURACY);
		assertEquals(3, Angle.zero().add(Angle.ofDeg(1)).add(Angle.ofDeg(2)).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void subtract()
	{
		assertEquals(0, Angle.zero().subtract(Math.PI / 2).subtract(Angle.ofDeg(270)).asRad(), ACCURACY);
		assertEquals(-3, Angle.zero().subtract(Angle.ofDeg(1)).subtract(Angle.ofDeg(2)).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void multiply()
	{
		assertEquals(0, Angle.zero().multiply(Math.PI / 2).multiply(42).asRad(), ACCURACY);
		assertEquals(42, Angle.ofDeg(21).multiply(1).multiply(2).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void equals()
	{
		assertEquals(Angle.ofDeg(90), Angle.ofRad(Math.PI * -1.5));
		assertEquals(Angle.ofDeg(0), Angle.ofRad(0));
		assertNotEquals(Angle.ofDeg(5), Angle.ofDeg(6));
		assertEquals(Angle.ofDeg(90), Angle.ofDeg(360 + 90));
	}
}