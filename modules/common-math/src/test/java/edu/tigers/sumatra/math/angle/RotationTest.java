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
public class RotationTest
{
	
	public static final double ACCURACY = 1e-6;
	
	
	@Test
	public void zero()
	{
		assertEquals(0, Rotation.zero().asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofRad()
	{
		assertEquals(0, Rotation.ofRad(0).asRad(), ACCURACY);
		assertEquals(Math.PI * 2, Rotation.ofRad(Math.PI * 2).asRad(), ACCURACY);
		assertEquals(Math.PI * 100, Rotation.ofRad(Math.PI * 100).asRad(), ACCURACY);
		assertEquals(90, Rotation.ofRad(Math.PI / 2).asDeg(), ACCURACY);
		assertEquals(-270, Rotation.ofRad(Math.PI * -1.5).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void ofDeg()
	{
		assertEquals(0, Rotation.ofDeg(0).asRad(), ACCURACY);
		assertEquals(Math.PI * 2, Rotation.ofDeg(360).asRad(), ACCURACY);
		assertEquals(Math.PI / 2, Rotation.ofDeg(90).asRad(), ACCURACY);
		assertEquals(-3 * Math.PI / 2, Rotation.ofDeg(-270).asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofRotation()
	{
		assertEquals(10 * Math.PI, Rotation.ofRotation(5).asRad(), ACCURACY);
		assertEquals(Math.PI, Rotation.ofRotation(0.5).asRad(), ACCURACY);
		assertEquals(-Math.PI / 2, Rotation.ofRotation(-0.25).asRad(), ACCURACY);
	}
	
	
	@Test
	public void ofVec()
	{
		assertEquals(0, Rotation.ofVec(Vector2.fromAngle(0)).asRad(), ACCURACY);
		assertEquals(-1.982297, Rotation.ofVec(Vector2.fromAngle(42)).asRad(), ACCURACY);
		assertEquals(Math.PI / 2, Rotation.ofVec(Vector2.fromAngle(Math.PI / 2)).asRad(), ACCURACY);
	}
	
	
	@Test
	public void add()
	{
		assertEquals(Math.PI * 2, Rotation.zero().add(Math.PI / 2).add(Rotation.ofDeg(270)).asRad(), ACCURACY);
		assertEquals(3, Rotation.zero().add(Rotation.ofDeg(1)).add(Rotation.ofDeg(2)).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void subtract()
	{
		assertEquals(-2 * Math.PI, Rotation.zero().subtract(Math.PI / 2).subtract(Rotation.ofDeg(270)).asRad(), ACCURACY);
		assertEquals(-3, Rotation.zero().subtract(Rotation.ofDeg(1)).subtract(Rotation.ofDeg(2)).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void multiply()
	{
		assertEquals(0, Rotation.zero().multiply(Math.PI / 2).multiply(42).asRad(), ACCURACY);
		assertEquals(42, Rotation.ofDeg(21).multiply(1).multiply(2).asDeg(), ACCURACY);
	}
	
	
	@Test
	public void equals()
	{
		assertEquals(Rotation.ofDeg(90), Rotation.ofRad(Math.PI / 2));
		assertEquals(Rotation.ofDeg(0), Rotation.ofRad(0));
		assertNotEquals(Rotation.ofDeg(5), Rotation.ofDeg(6));
	}
}