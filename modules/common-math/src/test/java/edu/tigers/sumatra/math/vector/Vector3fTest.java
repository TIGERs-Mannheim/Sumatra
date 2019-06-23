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
public class Vector3fTest
{
	
	@Test
	public void testEquals()
	{
		EqualsVerifier.forClass(Vector3f.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
		
		assertThat(Vector3.fromXYZ(42, 1337, 3.14)).isEqualTo(Vector3f.fromXYZ(42, 1337, 3.14));
		
	}
	
	
	@Test
	public void testFromXYZ()
	{
		assertThat(Vector3f.fromXYZ(42, 1337, 3.14)).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThat(Vector3f.fromXYZ(42, 1337, 3.14)).isNotEqualTo(Vector3.zero());
	}
	
	
	@Test
	public void testFrom2d()
	{
		assertThat(Vector3f.from2d(Vector2.fromXY(42, 1337), 3.14)).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThat(Vector3f.from2d(Vector2.fromXY(42, 1337), 3.14)).isNotEqualTo(Vector3.zero());
	}
	
	
	@Test
	public void testCopy()
	{
		assertThat(Vector3f.copy(Vector3.fromXYZ(42, 1337, 3.14))).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThat(Vector3f.copy(Vector3.fromXYZ(42, 42, 3.14))).isNotEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThat(Vector3f.copy(Vector2.fromXY(42, 1337))).isEqualTo(Vector3.fromXYZ(42, 1337, 0));
	}
	
	
	@Test
	public void testZero()
	{
		Vector3f zero = Vector3f.zero();
		assertThat(zero.x()).isEqualTo(0.0);
		assertThat(zero.y()).isEqualTo(0.0);
	}
	
	
	@Test
	public void testGetXYVector()
	{
		assertThat(Vector3f.fromXYZ(42, 1337, 3.14).getXYVector()).isEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	public void testGetXYZVector()
	{
		assertThat(Vector3f.fromXYZ(42, 1337, 3.14).getXYZVector()).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
	}
}
