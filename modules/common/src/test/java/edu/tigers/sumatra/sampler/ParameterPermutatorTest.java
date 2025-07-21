/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sampler;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class ParameterPermutatorTest
{
	private static final double EQUALS_TOLERANCE = 1e-10;
	
	
	@Test
	public void testOneSet()
	{
		ParameterPermutator permutator = new ParameterPermutator();
		String id = "one";
		permutator.add(id, 0.5, 1.5, 0.5);
		
		assertThat(permutator.next().get(id)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(permutator.next().get(id)).isCloseTo(1.0, within(EQUALS_TOLERANCE));
		assertThat(permutator.next().get(id)).isCloseTo(1.5, within(EQUALS_TOLERANCE));
		assertThat(permutator.next().get(id)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(permutator.next().get(id)).isCloseTo(1.0, within(EQUALS_TOLERANCE));
		assertThat(permutator.next().get(id)).isCloseTo(1.5, within(EQUALS_TOLERANCE));
	}
	
	
	@Test
	public void testTwoSets()
	{
		ParameterPermutator permutator = new ParameterPermutator();
		String id1 = "one";
		String id2 = "two";
		permutator.add(id1, 0.5, 1.0, 0.5);
		permutator.add(id2, 0.0, 0.2, 0.1);
		
		Map<String, Double> next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.0, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(1.0, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.0, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.1, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(1.0, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.1, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.2, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(1.0, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.2, within(EQUALS_TOLERANCE));
		next = permutator.next();
		assertThat(next.get(id1)).isCloseTo(0.5, within(EQUALS_TOLERANCE));
		assertThat(next.get(id2)).isCloseTo(0.0, within(EQUALS_TOLERANCE));
	}
	
}