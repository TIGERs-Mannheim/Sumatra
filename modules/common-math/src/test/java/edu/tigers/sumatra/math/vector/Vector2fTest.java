/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Vector2fTest
{
	
	@Test
	public void testEquals()
	{
		EqualsVerifier.forClass(Vector2f.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
		
		assertThat(Vector2.fromXY(42, 1337)).isEqualTo(Vector2f.fromXY(42, 1337));
		
	}
	
	
	@Test
	public void testFromXY()
	{
		assertThat(Vector2f.fromXY(42, 1337)).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(Vector2f.fromXY(42, 1337)).isNotEqualTo(Vector2.zero());
	}
	
	
	@Test
	public void testCopy()
	{
		assertThat(Vector2f.copy(Vector2.fromXY(42, 1337))).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(Vector2f.copy(Vector2.fromXY(42, 42))).isNotEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	public void testZero()
	{
		Vector2f zero = Vector2f.zero();
		assertThat(zero.x()).isEqualTo(0.0);
		assertThat(zero.y()).isEqualTo(0.0);
	}
	
	
	@Test
	public void testGetXYVector()
	{
		assertThat(Vector2f.fromXY(42, 1337).getXYVector()).isEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	public void testGetXYZVector()
	{
		assertThat(Vector2f.fromXY(42, 1337).getXYZVector()).isEqualTo(Vector3.fromXYZ(42, 1337, 0));
	}
	
}
