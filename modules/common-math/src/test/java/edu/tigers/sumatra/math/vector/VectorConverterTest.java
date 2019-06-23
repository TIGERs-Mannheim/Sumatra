/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorConverterTest
{
	
	private VectorConverter converter = new VectorConverter();
	
	
	@Test
	public void testSupportedClass()
	{
		assertThat(converter.supportedClass(IVector.class)).isTrue();
		assertThat(converter.supportedClass(IVector2.class)).isTrue();
		assertThat(converter.supportedClass(IVector3.class)).isTrue();
		assertThat(converter.supportedClass(IVectorN.class)).isTrue();
	}
	
	
	@Test
	public void testParseString()
	{
		assertThat(converter.parseString(IVector.class, "42;1337")).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(converter.parseString(IVector2.class, "42;1337")).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(converter.parseString(IVector3.class, "42;1337,3.14")).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThat(converter.parseString(IVectorN.class, "42;1337,3.14,-5")).isEqualTo(VectorN.from(42, 1337, 3.14, -5));
	}
	
	
	@Test
	public void testParseStringInvalid()
	{
		Logger.getRootLogger().setLevel(Level.OFF);
		assertThat(converter.parseString(IVector2.class, "42;133a7")).isEqualTo(Vector2.zero());
		assertThat(converter.parseString(IVector3.class, "42;1337,3.a14")).isEqualTo(Vector3.zero());
		assertThat(converter.parseString(IVectorN.class, "42;1337,3.14,-5a")).isEqualTo(VectorN.zero(0));
		assertThat(converter.parseString(IVector.class, "4a2;1337")).isEqualTo(VectorN.zero(0));
		assertThat(converter.parseString(Object.class, "")).isNull();
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	
	@Test
	public void testToString()
	{
		assertThat(converter.toString(IVector.class, Vector2.fromXY(42, 1337))).isEqualTo("42.0;1337.0");
		assertThat(converter.toString(IVector2.class, Vector2.fromXY(42, 1337))).isEqualTo("42.0;1337.0");
		assertThat(converter.toString(IVector3.class, Vector3.fromXYZ(42, 1337, 3.14))).isEqualTo("42.0;1337.0;3.14");
		assertThat(converter.toString(IVectorN.class, VectorN.from(42, 1337, 3.14, -5)))
				.isEqualTo("42.0;1337.0;3.14;-5.0");
	}
}
