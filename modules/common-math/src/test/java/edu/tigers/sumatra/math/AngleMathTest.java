/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Testing the AngleMath module.
 * 
 * @author stei_ol
 */
public class AngleMathTest
{
	private static final double	ACCURACY	= 0.001;
	
	
	@Test
	public void testNormalizeAngle()
	{
		assertEquals(AngleMath.normalizeAngle(4.6f * AngleMath.PI), 0.6 * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(-4.6f * AngleMath.PI), -0.6f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(3.6f * AngleMath.PI), -0.4f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(-3.6f * AngleMath.PI), 0.4 * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(5.001f * AngleMath.PI), -0.999f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(5f * AngleMath.PI), AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(4f * AngleMath.PI), 0, ACCURACY);
	}
	
	
	@Test
	public void testTrigonometry()
	{
		// sin
		assertEquals(Math.sin(AngleMath.PI), 0, ACCURACY);
		assertEquals(Math.sin(4.5f), -0.9775301, ACCURACY);
		assertEquals(Math.sin(-34), -0.529, ACCURACY);
		
		// cos
		assertEquals(Math.cos(5), 0.28366, ACCURACY);
		assertEquals(Math.cos(-0.1f), 0.9950, ACCURACY);
		
		// tan
		assertEquals(AngleMath.tan(3), -0.1425, ACCURACY);
		assertEquals(AngleMath.tan(-2), 2.185, ACCURACY);
	}
	
	
	@Test
	public void testDifference()
	{
		assertThat(AngleMath.difference(1, 2)).isEqualTo(-1.0);
		assertThat(AngleMath.difference(2, 1)).isEqualTo(1.0);
		assertThat(AngleMath.difference(50, 10)).isEqualTo(AngleMath.normalizeAngle(40));
		assertThat(AngleMath.difference(50, 48)).isEqualTo(2.0);
		assertThat(AngleMath.difference(50, 90)).isEqualTo(AngleMath.normalizeAngle(-40));
		assertThat(AngleMath.difference(0, 0)).isEqualTo(0.0);
		assertThat(AngleMath.difference(50, 10)).isEqualTo(AngleMath.normalizeAngle(40));
	}
	
	
	@Test
	public void testDeg2Rad()
	{
		assertThat(AngleMath.deg2rad(-360)).isCloseTo(-AngleMath.PI_TWO, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(-180)).isCloseTo(-AngleMath.PI, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(-90)).isCloseTo(-AngleMath.PI_HALF, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(0)).isCloseTo(0.0, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(90)).isCloseTo(AngleMath.PI_HALF, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(180)).isCloseTo(AngleMath.PI, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(270)).isCloseTo(AngleMath.PI + AngleMath.PI_HALF, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(360)).isCloseTo(AngleMath.PI_TWO, withinPercentage(0.1));
		assertThat(AngleMath.deg2rad(450)).isCloseTo(AngleMath.PI_TWO + AngleMath.PI_HALF, withinPercentage(0.1));
	}
	
	
	@Test
	public void testRad2Deg()
	{
		assertThat(AngleMath.rad2deg(-AngleMath.PI_TWO)).isCloseTo(-360, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(-AngleMath.PI)).isCloseTo(-180, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(-AngleMath.PI_HALF)).isCloseTo(-90, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(0.0)).isCloseTo(0, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(AngleMath.PI_HALF)).isCloseTo(90, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(AngleMath.PI)).isCloseTo(180, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(AngleMath.PI + AngleMath.PI_HALF)).isCloseTo(270, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(AngleMath.PI_TWO)).isCloseTo(360, withinPercentage(0.1));
		assertThat(AngleMath.rad2deg(AngleMath.PI_TWO + AngleMath.PI_HALF)).isCloseTo(450, withinPercentage(0.1));
	}
}
