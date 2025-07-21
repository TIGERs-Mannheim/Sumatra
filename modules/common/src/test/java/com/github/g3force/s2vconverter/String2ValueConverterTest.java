package com.github.g3force.s2vconverter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class String2ValueConverterTest
{
	@Test
	public void parseString()
	{
		Double doubleValue = (Double) String2ValueConverter.getDefault().parseString(Double.TYPE, "42.1337");
		assertThat(doubleValue).isCloseTo(42.1337, within(0.0001));
		Boolean booleanValue = (Boolean) String2ValueConverter.getDefault().parseString(Boolean.TYPE, "false");
		assertThat(booleanValue).isFalse();
	}
}
