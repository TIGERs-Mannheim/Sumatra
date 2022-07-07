/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.Assert.assertEquals;


/**
 * Testing the AngleMath module.
 *
 * @author stei_ol
 */
public class AngleMathTest
{
	private static final double ACCURACY = 0.001;


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
		assertEquals(SumatraMath.sin(AngleMath.PI), 0, ACCURACY);
		assertEquals(SumatraMath.sin(4.5f), -0.9775301, ACCURACY);
		assertEquals(SumatraMath.sin(-34), -0.529, ACCURACY);

		// cos
		assertEquals(SumatraMath.cos(5), 0.28366, ACCURACY);
		assertEquals(SumatraMath.cos(-0.1f), 0.9950, ACCURACY);

		// tan
		assertEquals(SumatraMath.tan(3), -0.1425, ACCURACY);
		assertEquals(SumatraMath.tan(-2), 2.185, ACCURACY);
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
	public void testRotateAngle()
	{
		assertThat(AngleMath.rotateAngle(-1, 2, ERotationDirection.COUNTER_CLOCKWISE)).isEqualTo(1);
		assertThat(AngleMath.rotateAngle(1, 2, ERotationDirection.CLOCKWISE)).isEqualTo(-1);
		assertThat(AngleMath.rotateAngle(0, 42, ERotationDirection.NONE)).isZero();
		assertThat(AngleMath.rotateAngle(15 * AngleMath.PI_TWO, 42, ERotationDirection.NONE)).isZero();
		assertThat(AngleMath.rotateAngle(-1, 2 + 10 * AngleMath.PI_TWO, ERotationDirection.COUNTER_CLOCKWISE))
				.isEqualTo(1);
	}


	@Test
	public void testRotationDirection()
	{
		assertThat(AngleMath.rotationDirection(-1, 1)).isEqualTo(ERotationDirection.COUNTER_CLOCKWISE);
		assertThat(AngleMath.rotationDirection(1, -1)).isEqualTo(ERotationDirection.CLOCKWISE);
		assertThat(AngleMath.rotationDirection(1, 1)).isEqualTo(ERotationDirection.NONE);
		assertThat(AngleMath.rotationDirection(-1 + 10 * AngleMath.PI_TWO, 1 + 10 * AngleMath.PI_TWO))
				.isEqualTo(ERotationDirection.COUNTER_CLOCKWISE);
		assertThat(AngleMath.rotationDirection(1 + 10 * AngleMath.PI_TWO, -1))
				.isEqualTo(ERotationDirection.CLOCKWISE);
		assertThat(AngleMath.rotationDirection(15 * AngleMath.PI_TWO, 10 * AngleMath.PI_TWO))
				.isEqualTo(ERotationDirection.NONE);
	}


	@Test
	public void testCompareAngle()
	{
		assertThat(AngleMath.compareAngle(AngleMath.deg2rad(-45), AngleMath.deg2rad(45))).isEqualTo(-1);
		assertThat(AngleMath.compareAngle(AngleMath.deg2rad(45), AngleMath.deg2rad(-45))).isEqualTo(1);
		assertThat(AngleMath.compareAngle(AngleMath.deg2rad(-135), AngleMath.deg2rad(135))).isEqualTo(1);
		assertThat(AngleMath.compareAngle(AngleMath.deg2rad(135), AngleMath.deg2rad(-135))).isEqualTo(-1);
		assertThat(AngleMath.compareAngle(0, 1e-7)).isZero();
		assertThat(AngleMath.compareAngle(0, -1e-7)).isZero();
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
