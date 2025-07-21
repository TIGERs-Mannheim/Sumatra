/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ObjectSizeAnalyzerTest
{
	private ObjectSizeAnalyzer objectSizeAnalyzer = new ObjectSizeAnalyzer();


	@Test
	public void getTotalBytes()
	{
		Foo foo = new Foo();
		long bytes = objectSizeAnalyzer.getTotalBytes(foo);
		assertThat(bytes).isEqualTo(48);

		foo.bar = null;
		bytes = objectSizeAnalyzer.getTotalBytes(foo);
		assertThat(bytes).isEqualTo(24);
	}


	static class Foo
	{
		Bar bar = new Bar();
		Foo foo = null;
	}

	static class Bar
	{
		double value = 42;
	}
}
