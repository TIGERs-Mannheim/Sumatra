/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package com.github.g3force.s2vconverter;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
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
		assertThat(converter.parseString(IVector2.class, "42;133a7")).isNull();
		assertThat(converter.parseString(IVector3.class, "42;1337,3.a14")).isNull();
		assertThat(converter.parseString(IVectorN.class, "42;1337,3.14,-5a")).isNull();
		assertThat(converter.parseString(IVector.class, "4a2;1337")).isNull();
		assertThat(converter.parseString(Object.class, "")).isNull();
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
