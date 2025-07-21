/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.StatisticsMath;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Class for testing several functions provided by {@link AVector3} and {@link Vector3}
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
class Vector3Test
{
	private static final double ACCURACY = 0.001;
	
	
	@Test
	void testAdd()
	{
		Vector3 vec1 = Vector3.fromXYZ(2, 1, 5);
		Vector3 vec2 = Vector3.fromXYZ(3, 4, 7);
		
		vec1.add(vec2);
		assertEquals(Vector3.fromXYZ(5, 5, 12), vec1);
		
		vec1 = Vector3.fromXYZ(-2, 0, -42);
		vec1.add(vec2);
		assertEquals(Vector3.fromXYZ(1, 4, -35), vec1);
		
		vec1 = Vector3.fromXYZ(-2, -3, 13);
		vec2 = Vector3.fromXYZ(-1, 5, -7);
		vec1.add(vec2);
		assertEquals(Vector3.fromXYZ(-3, 2, 6), vec1);
	}
	
	
	@Test
	void testAddNew()
	{
		Vector3 vec1 = Vector3.fromXYZ(5, 7, 3);
		Vector3 vec2 = Vector3.fromXYZ(1, 2, 1);
		Vector3 result = vec1.addNew(vec2);
		assertEquals(Vector3.fromXYZ(6, 9, 4), result);
		
		vec1 = Vector3.fromXYZ(-3, 0, 5);
		vec2 = Vector3.fromXYZ(4, 2, -2);
		result = vec1.addNew(vec2);
		assertEquals(Vector3.fromXYZ(1, 2, 3), result);
		
		vec1 = Vector3.fromXYZ(-3, 5, 7);
		vec2 = Vector3.fromXYZ(-4, -2, 1);
		result = vec1.addNew(vec2);
		assertEquals(Vector3.fromXYZ(-7, 3, 8), result);
	}
	
	
	@Test
	void testMultiply()
	{
		Vector3 vec1 = Vector3.fromXYZ(4, 7, -2);
		double factor = 4.5;
		vec1.multiply(factor);
		assertEquals(Vector3.fromXYZ(18, 31.5, -9), vec1);
		
		vec1 = Vector3.fromXYZ(1, 1, 1);
		factor = AngleMath.PI;
		vec1.multiply(factor);
		assertEquals(vec1.x(), factor, ACCURACY);
		assertEquals(vec1.y(), factor, ACCURACY);
		assertEquals(vec1.z(), factor, ACCURACY);
		
		vec1 = Vector3.fromXYZ(3, -5, 5);
		factor = -2.1f;
		vec1.multiply(factor);
		assertEquals(vec1.x(), -6.3f, ACCURACY);
		assertEquals(vec1.y(), 10.5, ACCURACY);
		assertEquals(vec1.z(), -10.5, ACCURACY);
		
		factor = 0;
		vec1.multiply(factor);
		assertThat(Vector3.fromXYZ(0, 0, 0).isCloseTo(vec1)).isTrue();
	}
	
	
	@Test
	void testMultiplyNewFactor()
	{
		Vector3 vec1 = Vector3.fromXYZ(4, 7, 1);
		double factor = 4.5;
		Vector3 result = vec1.multiplyNew(factor);
		assertEquals(Vector3.fromXYZ(18, 31.5, 4.5), result);
		
		vec1 = Vector3.fromXYZ(1, 1, 1);
		factor = AngleMath.PI;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), factor, ACCURACY);
		assertEquals(result.y(), factor, ACCURACY);
		assertEquals(result.z(), factor, ACCURACY);
		assertEquals(Vector3.fromXYZ(1, 1, 1), vec1);
		
		vec1 = Vector3.fromXYZ(3, -5, 2);
		factor = -2.1f;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), -6.3f, ACCURACY);
		assertEquals(result.y(), 10.5, ACCURACY);
		assertEquals(result.z(), -4.2, ACCURACY);
		
		factor = 0;
		result = vec1.multiplyNew(factor);
		assertThat(Vector3.fromXYZ(0, 0, 0).isCloseTo(result)).isTrue();
	}
	
	
	@Test
	void testMultiplyNewVector()
	{
		assertThat(Vector3.zero().multiplyNew(Vector3.fromXYZ(1, 1, 1))).isEqualTo(Vector3.zero());
		assertThat(Vector3.fromXYZ(1, 1, 1).multiplyNew(Vector3.fromXYZ(1, 1, 1))).isEqualTo(Vector3.fromXYZ(1, 1, 1));
		assertThat(Vector3.fromXYZ(42, 1337, 3.14).multiplyNew(Vector3.fromXYZ(5, 2, 1)))
				.isEqualTo(Vector3.fromXYZ(210, 2674, 3.14));
	}
	
	
	@Test
	void testSubtract()
	{
		Vector3 vec1 = Vector3.fromXYZ(4, 2, 1);
		Vector3 vec2 = Vector3.fromXYZ(3, 5, 2);
		vec1.subtract(vec2);
		assertEquals(Vector3.fromXYZ(1, -3, -1), vec1);
		
		vec1 = Vector3.fromXYZ(1, 5, 5);
		vec2 = Vector3.fromXYZ(-2, 5, -3);
		vec1.subtract(vec2);
		assertEquals(Vector3.fromXYZ(3, 0, 8), vec1);
		
		vec2 = Vector3.fromXYZ(2.4, -3.1, 3);
		vec1.subtract(vec2);
		assertEquals(vec1.x(), 0.6, ACCURACY);
		assertEquals(vec1.y(), 3.1, ACCURACY);
		assertEquals(vec1.z(), 5, ACCURACY);
	}
	
	
	@Test
	void testSubtractNew()
	{
		Vector3 vec1 = Vector3.fromXYZ(5, 7, 2);
		Vector3 vec2 = Vector3.fromXYZ(1, 2, -1);
		Vector3 result = vec1.subtractNew(vec2);
		assertEquals(Vector3.fromXYZ(4, 5, 3), result);
		
		vec1 = Vector3.fromXYZ(-3, 0, 1);
		vec2 = Vector3.fromXYZ(4, 2, 1);
		result = vec1.subtractNew(vec2);
		assertEquals(Vector3.fromXYZ(-7, -2, 0), result);
		
		vec1 = Vector3.fromXYZ(-3, 5, 4);
		vec2 = Vector3.fromXYZ(-4, -2, 8);
		result = vec1.subtractNew(vec2);
		assertEquals(Vector3.fromXYZ(1, 7, -4), result);
		assertEquals(Vector3.fromXYZ(-3, 5, 4), vec1);
	}
	
	
	@Test
	void testNormalize()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 3.14).normalizeNew().getLength()).isCloseTo(1, within(1e-6));
		assertThat(Vector3.zero().normalizeNew().getLength()).isCloseTo(0, within(1e-6));
	}
	
	
	@Test
	void testGetLength()
	{
		assertThat(Vector3.fromXYZ(42, 0, 0).getLength2()).isEqualTo(42);
		assertThat(Vector3.fromXYZ(-42, 0, 0).getLength2()).isEqualTo(42);
		assertThat(Vector3.fromXYZ(2, 2, 0).getLength2()).isCloseTo(Math.sqrt(8), within(1e-6));
		assertThat(Vector3.fromXYZ(42, -1337, 3.14).getLength2()).isCloseTo(Vector3.fromXYZ(42, -1337, 3.14).getLength(),
				withinPercentage(0.1));
	}
	
	
	@Test
	void testEquals()
	{
		Vector3 vec1 = Vector3.fromXYZ(5, 0, -2);
		Vector3 vec2 = Vector3.fromXYZ(5, 0, -2);
		Boolean result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = Vector3.fromXYZ(3.1f, 1.4, -0.1);
		vec2 = Vector3.fromXYZ(3.1f, 1.4, -0.1);
		result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = Vector3.fromXYZ(3.1f, 1.4, 0);
		vec2 = Vector3.fromXYZ(3.1f, 1.3, 0);
		result = vec1.equals(vec2);
		assertTrue(!result);
		
		EqualsVerifier.forClass(Vector3.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
	
	
	@Test
	void testValueOf()
	{
		assertThat(Vector3.valueOf("0,0,0").isCloseTo(Vector3f.ZERO_VECTOR)).isTrue();
		assertThat(Vector3.valueOf("42,21,1").isCloseTo(Vector3.fromXYZ(42, 21, 1))).isTrue();
		assertThat(Vector3.valueOf("pi,0,0").isCloseTo(Vector3.fromXYZ(AngleMath.PI, 0, 0))).isTrue();
		assertThat(Vector3.valueOf("21+21,1*42,1").isCloseTo(Vector3.fromXYZ(42, 42, 1))).isTrue();
		assertThat(Vector3.valueOf("1+2*3-7,2/4,1").isCloseTo(Vector3.fromXYZ(0, 0.5, 1))).isTrue();
	}
	
	
	@Test
	void testSet()
	{
		Vector3 vector = Vector3.zero();
		assertThat(vector.isCloseTo(Vector3f.ZERO_VECTOR)).isTrue();
		vector.set(0, 42);
		assertThat(vector.isCloseTo(Vector3.fromXYZ(42, 0, 0))).isTrue();
		vector.set(1, 21);
		assertThat(vector.isCloseTo(Vector3.fromXYZ(42, 21, 0))).isTrue();
		vector.set(2, 10);
		assertThat(vector.isCloseTo(Vector3.fromXYZ(42, 21, 10))).isTrue();
		assertThatThrownBy(() -> vector.set(3, 1)).isInstanceOfAny(IllegalArgumentException.class);
		
		vector.set(Vector2.fromXY(42, 1337), 3.14);
		assertThat(vector).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
	}
	
	
	@Test
	void testIsZeroVector()
	{
		assertThat(Vector3.fromXYZ(0, 0, 0).isZeroVector()).isTrue();
		assertThat(Vector3.fromXYZ(1e-8, 0, 0).isZeroVector()).isTrue();
		assertThat(Vector3.fromXYZ(1e-8, 1e-8, 1e-8).isZeroVector()).isTrue();
		assertThat(Vector3.fromXYZ(1e-2, 0, 0).isZeroVector()).isFalse();
	}
	
	
	@Test
	void testIsFinite()
	{
		assertThat(Vector3.fromXYZ(0, 1, 2).isFinite()).isTrue();
		assertThat(Vector3.fromXYZ(Double.NaN, 0, 0).isFinite()).isFalse();
		assertThat(Vector3.fromXYZ(Double.POSITIVE_INFINITY, 0, 0).isFinite()).isFalse();
	}
	
	
	@Test
	void testGetSaveableString()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 3.14).getSaveableString())
				.isEqualTo(42d + ";" + 1337d + ";" + 3.14);
	}
	
	
	@Test
	void testNumberList()
	{
		IVector3 vector = Vector3.fromXYZ(42, 1337, 3.14);
		List<Number> nbrList = vector.getNumberList();
		assertThat(nbrList).contains(42d, 1337d, 3.14);
		IVector newVector = Vector3.fromNumberList(nbrList);
		assertThat(newVector).isEqualTo(vector);
	}
	
	
	@Test
	void testToArray()
	{
		IVector3 vector = Vector3.fromXYZ(42, 1337, 3.14);
		assertThat(vector.toArray()).isEqualTo(new double[] { 42d, 1337d, 3.14 });
	}
	
	
	@Test
	void testToString()
	{
		IVector3 vector1 = Vector3.fromXYZ(0, 0, 2.5);
		assertThat(vector1.toString()).isEqualTo("[0.000,0.000,2.500|l=2.500]");
		IVector3 vector2 = Vector3.fromXYZ(0, 0.001, 0);
		assertThat(vector2.toString()).isEqualTo("[0.000,0.001,0.000|l=0.001]");
	}
	
	
	@Test
	void testToJSON()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 1).toJSON().toJson())
				.isEqualTo("{\"dim2\":1.0,\"dim1\":1337.0,\"dim0\":42.0}");
	}
	
	
	@Test
	void testToJsonArray()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 1).toJsonArray().toJson()).isEqualTo("[42.0,1337.0,1.0]");
	}
	
	
	@Test
	void testIsCloseTo()
	{
		assertThat(Vector3.zero().isCloseTo(Vector3.zero(), 0)).isTrue();
		assertThat(Vector3.zero().isCloseTo(Vector3.fromXYZ(1, 0, 0), 0.1)).isFalse();
		assertThat(Vector3.fromXYZ(42, 1337, 0).isCloseTo(Vector3.fromXYZ(42, 1337, 0), 1e-6)).isTrue();
		assertThat(Vector3.fromXYZ(42, 1337, 0).isCloseTo(Vector2.zero())).isFalse();
	}
	
	
	@Test
	void testMeanVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector3.fromXYZ(24, 234, -4));
		list.add(Vector3.fromXYZ(42, 133, 3));
		list.add(Vector3.fromXYZ(123, -321, 20));
		double muX = StatisticsMath.mean(list.stream().map(IVector::x).collect(Collectors.toList()));
		double muY = StatisticsMath.mean(list.stream().map(IVector::y).collect(Collectors.toList()));
		double muZ = StatisticsMath.mean(list.stream().map(e -> e.get(2)).collect(Collectors.toList()));
		IVector muVector = Vector3.meanVector(list);
		IVector muRefVector = VectorN.from(muX, muY, muZ);
		assertThat(muVector.isCloseTo(muRefVector, 1e-2)).withFailMessage("expected: %s, but: %s", muVector, muRefVector)
				.isTrue();
	}
	
	
	@Test
	void testVarianceVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector3.fromXYZ(24, 0, 9));
		list.add(Vector3.fromXYZ(42, -3, -5));
		list.add(Vector3.fromXYZ(-21, 10, 21));
		double varX = StatisticsMath.variance(list.stream().map(IVector::x).collect(Collectors.toList()));
		double varY = StatisticsMath.variance(list.stream().map(IVector::y).collect(Collectors.toList()));
		double varZ = StatisticsMath.variance(list.stream().map(e -> e.get(2)).collect(Collectors.toList()));
		IVector varVector = Vector3.varianceVector(list);
		IVector varRefVector = VectorN.from(varX, varY, varZ);
		assertThat(varVector.isCloseTo(varRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", varVector, varRefVector).isTrue();
	}
	
	
	@Test
	void testStdVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector3.fromXYZ(24, 0, -1));
		list.add(Vector3.fromXYZ(42, -3, 0));
		list.add(Vector3.fromXYZ(-21, 10, 41));
		double stdX = StatisticsMath.std(list.stream().map(IVector::x).collect(Collectors.toList()));
		double stdY = StatisticsMath.std(list.stream().map(IVector::y).collect(Collectors.toList()));
		double stdZ = StatisticsMath.std(list.stream().map(e -> e.get(2)).collect(Collectors.toList()));
		IVector stdVector = Vector3.stdVector(list);
		IVector stdRefVector = VectorN.from(stdX, stdY, stdZ);
		assertThat(stdVector.isCloseTo(stdRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", stdVector, stdRefVector).isTrue();
	}
	
	
	@Test
	void testGet()
	{
		IVector3 vector = Vector3.fromXYZ(42, 1337, 2);
		assertThat(vector.get(0)).isEqualTo(vector.x());
		assertThat(vector.get(1)).isEqualTo(vector.y());
		assertThat(vector.get(2)).isEqualTo(vector.z());
		assertThatThrownBy(() -> vector.get(3)).isExactlyInstanceOf(IllegalArgumentException.class);
	}
	
	
	@Test
	void testAbs()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 2).absNew()).isEqualTo(Vector3.fromXYZ(42, 1337, 2));
		assertThat(Vector3.fromXYZ(-42, 1337, 2).absNew()).isEqualTo(Vector3.fromXYZ(42, 1337, 2));
		assertThat(Vector3.fromXYZ(-42, -1337, -2).absNew()).isEqualTo(Vector3.fromXYZ(42, 1337, 2));
	}
	
	
	@Test
	void testGetXYVector()
	{
		assertThat(Vector3.fromXYZ(42, 1337, 0).getXYVector()).isEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	void testFrom2d()
	{
		assertThat(Vector3.from2d(Vector2.fromXY(42, 1337), 3.14)).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
	}
	
	
	@Test
	void testFromArray()
	{
		assertThat(Vector3.fromArray(new double[] { 42, 1337, 3.14 })).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
		assertThatThrownBy(() -> Vector3.fromArray(new double[] { 1 }))
				.isExactlyInstanceOf(IllegalArgumentException.class);
	}
	
	
	@Test
	void testGetXYZVector()
	{
		Vector3 vector = Vector3.fromXYZ(42, 1337, 3.14);
		assertThat(vector.getXYZVector()).isSameAs(vector);
	}
	
	
	@Test
	void testApply()
	{
		assertThat(Vector3.fromXYZ(1, -2, 3).apply(v -> Math.abs(v * 3))).isEqualTo(Vector3.fromXYZ(3, 6, 9));
	}
}
