/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.StatisticsMath;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;


/**
 * Class for testing several functions provided by {@link AVectorN} and {@link VectorN}
 *
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorNTest
{
	private static final double ACCURACY = 0.001;
	
	
	@Test
	public void testAdd()
	{
		VectorN vec1 = VectorN.from(2, 1, 5);
		VectorN vec2 = VectorN.from(3, 4, 7);
		
		vec1.add(vec2);
		assertEquals(VectorN.from(5, 5, 12), vec1);
		
		vec1 = VectorN.from(-2, 0, -42);
		vec1.add(vec2);
		assertEquals(VectorN.from(1, 4, -35), vec1);
		
		vec1 = VectorN.from(-2, -3, 13);
		vec2 = VectorN.from(-1, 5, -7);
		vec1.add(vec2);
		assertEquals(VectorN.from(-3, 2, 6), vec1);
	}
	
	
	@Test
	public void testAddNew()
	{
		VectorN vec1 = VectorN.from(5, 7, 3);
		VectorN vec2 = VectorN.from(1, 2, 1);
		VectorN result = vec1.addNew(vec2);
		assertEquals(VectorN.from(6, 9, 4), result);
		
		vec1 = VectorN.from(-3, 0, 5);
		vec2 = VectorN.from(4, 2, -2);
		result = vec1.addNew(vec2);
		assertEquals(VectorN.from(1, 2, 3), result);
		
		vec1 = VectorN.from(-3, 5, 7);
		vec2 = VectorN.from(-4, -2, 1);
		result = vec1.addNew(vec2);
		assertEquals(VectorN.from(-7, 3, 8), result);
	}
	
	
	@Test
	public void testMultiply()
	{
		VectorN vec1 = VectorN.from(4, 7, -2);
		double factor = 4.5;
		vec1.multiply(factor);
		assertEquals(VectorN.from(18, 31.5, -9), vec1);
		
		vec1 = VectorN.from(1, 1, 1);
		factor = AngleMath.PI;
		vec1.multiply(factor);
		assertEquals(vec1.x(), factor, ACCURACY);
		assertEquals(vec1.y(), factor, ACCURACY);
		assertEquals(vec1.z(), factor, ACCURACY);
		
		vec1 = VectorN.from(3, -5, 5);
		factor = -2.1f;
		vec1.multiply(factor);
		assertEquals(vec1.x(), -6.3f, ACCURACY);
		assertEquals(vec1.y(), 10.5, ACCURACY);
		assertEquals(vec1.z(), -10.5, ACCURACY);
		
		factor = 0;
		vec1.multiply(factor);
		assertThat(VectorN.from(0, 0, 0).isCloseTo(vec1)).isTrue();
	}
	
	
	@Test
	public void testMultiplyNewFactor()
	{
		VectorN vec1 = VectorN.from(4, 7, 1);
		double factor = 4.5;
		VectorN result = vec1.multiplyNew(factor);
		assertEquals(VectorN.from(18, 31.5, 4.5), result);
		
		vec1 = VectorN.from(1, 1, 1);
		factor = AngleMath.PI;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), factor, ACCURACY);
		assertEquals(result.y(), factor, ACCURACY);
		assertEquals(result.z(), factor, ACCURACY);
		assertEquals(VectorN.from(1, 1, 1), vec1);
		
		vec1 = VectorN.from(3, -5, 2);
		factor = -2.1f;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), -6.3f, ACCURACY);
		assertEquals(result.y(), 10.5, ACCURACY);
		assertEquals(result.z(), -4.2, ACCURACY);
		
		factor = 0;
		result = vec1.multiplyNew(factor);
		assertThat(VectorN.from(0, 0, 0).isCloseTo(result)).isTrue();
	}
	
	
	@Test
	public void testMultiplyNewVector()
	{
		assertThat(VectorN.zero(3).multiplyNew(VectorN.from(1, 1, 1))).isEqualTo(VectorN.zero(3));
		assertThat(VectorN.from(1, 1, 1).multiplyNew(VectorN.from(1, 1, 1))).isEqualTo(VectorN.from(1, 1, 1));
		assertThat(VectorN.from(42, 1337, 3.14).multiplyNew(VectorN.from(5, 2, 1)))
				.isEqualTo(VectorN.from(210, 2674, 3.14));
	}
	
	
	@Test
	public void testSubtract()
	{
		VectorN vec1 = VectorN.from(4, 2, 1);
		VectorN vec2 = VectorN.from(3, 5, 2);
		vec1.subtract(vec2);
		assertEquals(VectorN.from(1, -3, -1), vec1);
		
		vec1 = VectorN.from(1, 5, 5);
		vec2 = VectorN.from(-2, 5, -3);
		vec1.subtract(vec2);
		assertEquals(VectorN.from(3, 0, 8), vec1);
		
		vec2 = VectorN.from(2.4, -3.1, 3);
		vec1.subtract(vec2);
		assertEquals(vec1.x(), 0.6, ACCURACY);
		assertEquals(vec1.y(), 3.1, ACCURACY);
		assertEquals(vec1.z(), 5, ACCURACY);
	}
	
	
	@Test
	public void testSubtractNew()
	{
		VectorN vec1 = VectorN.from(5, 7, 2);
		VectorN vec2 = VectorN.from(1, 2, -1);
		VectorN result = vec1.subtractNew(vec2);
		assertEquals(VectorN.from(4, 5, 3), result);
		
		vec1 = VectorN.from(-3, 0, 1);
		vec2 = VectorN.from(4, 2, 1);
		result = vec1.subtractNew(vec2);
		assertEquals(VectorN.from(-7, -2, 0), result);
		
		vec1 = VectorN.from(-3, 5, 4);
		vec2 = VectorN.from(-4, -2, 8);
		result = vec1.subtractNew(vec2);
		assertEquals(VectorN.from(1, 7, -4), result);
		assertEquals(VectorN.from(-3, 5, 4), vec1);
	}
	
	
	@Test
	public void testNormalize()
	{
		assertThat(VectorN.from(42, 1337, 3.14).normalizeNew().getLength()).isCloseTo(1, within(1e-6));
		assertThat(VectorN.zero(3).normalizeNew().getLength()).isCloseTo(0, within(1e-6));
	}
	
	
	@Test
	public void testGetLength()
	{
		assertThat(VectorN.from(42, 0, 0).getLength2()).isEqualTo(42);
		assertThat(VectorN.from(-42, 0, 0).getLength2()).isEqualTo(42);
		assertThat(VectorN.from(2, 2, 0).getLength2()).isCloseTo(Math.sqrt(8), within(1e-6));
		assertThat(VectorN.from(42, -1337, 3.14).getLength2()).isCloseTo(VectorN.from(42, -1337, 3.14).getLength(),
				withinPercentage(0.1));
	}
	
	
	@Test
	public void testEquals()
	{
		VectorN vec1 = VectorN.from(5, 0, -2);
		VectorN vec2 = VectorN.from(5, 0, -2);
		Boolean result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = VectorN.from(3.1f, 1.4, -0.1);
		vec2 = VectorN.from(3.1f, 1.4, -0.1);
		result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = VectorN.from(3.1f, 1.4, 0);
		vec2 = VectorN.from(3.1f, 1.3, 0);
		result = vec1.equals(vec2);
		assertTrue(!result);
		
		EqualsVerifier.forClass(VectorN.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
	
	
	@Test
	public void testValueOf()
	{
		assertThat(VectorN.valueOf("0,0,0").isCloseTo(VectorN.zero(3))).isTrue();
		assertThat(VectorN.valueOf("42,21,1").isCloseTo(VectorN.from(42, 21, 1))).isTrue();
		assertThat(VectorN.valueOf("pi,0,0").isCloseTo(VectorN.from(AngleMath.PI, 0, 0))).isTrue();
		assertThat(VectorN.valueOf("21+21,1*42,1").isCloseTo(VectorN.from(42, 42, 1))).isTrue();
		assertThat(VectorN.valueOf("1+2*3-7,2/4,1").isCloseTo(VectorN.from(0, 0.5, 1))).isTrue();
	}
	
	
	@Test
	public void testSet()
	{
		VectorN vector = VectorN.zero(3);
		vector.set(0, 42);
		assertThat(vector.isCloseTo(VectorN.from(42, 0, 0))).isTrue();
		vector.set(1, 21);
		assertThat(vector.isCloseTo(VectorN.from(42, 21, 0))).isTrue();
		vector.set(2, 10);
		assertThat(vector.isCloseTo(VectorN.from(42, 21, 10))).isTrue();
		assertThatThrownBy(() -> vector.set(3, 1)).isInstanceOfAny(IllegalArgumentException.class);
	}
	
	
	@Test
	public void testIsZeroVector()
	{
		assertThat(VectorN.from(0, 0, 0).isZeroVector()).isTrue();
		assertThat(VectorN.from(1e-8, 0, 0).isZeroVector()).isTrue();
		assertThat(VectorN.from(1e-8, 1e-8, 1e-8).isZeroVector()).isTrue();
		assertThat(VectorN.from(1e-4, 0, 0).isZeroVector()).isFalse();
	}
	
	
	@Test
	public void testIsFinite()
	{
		assertThat(VectorN.from(0, 1, 2).isFinite()).isTrue();
		assertThat(VectorN.from(Double.NaN, 0, 0).isFinite()).isFalse();
		assertThat(VectorN.from(Double.POSITIVE_INFINITY, 0, 0).isFinite()).isFalse();
	}
	
	
	@Test
	public void testGetSaveableString()
	{
		assertThat(VectorN.from(42, 1337, 3.14).getSaveableString())
				.isEqualTo(42d + ";" + 1337d + ";" + 3.14);
	}
	
	
	@Test
	public void testNumberList()
	{
		IVectorN vector = VectorN.from(42, 1337, 3.14, 1.0);
		List<Number> nbrList = vector.getNumberList();
		assertThat(nbrList).contains(42d, 1337d, 3.14, 1.0);
		IVector newVector = VectorN.fromNumberList(nbrList);
		assertThat(newVector).isEqualTo(vector);
	}
	
	
	@Test
	public void testToArray()
	{
		IVectorN vector = VectorN.from(42, 1337, 3.14);
		assertThat(vector.toArray()).isEqualTo(new double[] { 42d, 1337d, 3.14 });
	}
	
	
	@Test
	public void testToString()
	{
		IVectorN vector1 = VectorN.from(0, 0, 2.5);
		assertThat(vector1.toString()).isEqualTo("[0.000,0.000,2.500|l=2.500]");
		IVectorN vector2 = VectorN.from(0, 0.001, 0);
		assertThat(vector2.toString()).isEqualTo("[0.000,0.001,0.000|l=0.001|a=1.571]");
	}
	
	
	@Test
	public void testToJSON()
	{
		assertThat(VectorN.from(42, 1337, 1).toJSON().toJSONString())
				.isEqualTo("{\"dim2\":1.0,\"dim1\":1337.0,\"dim0\":42.0}");
	}
	
	
	@Test
	public void testToJSONArray()
	{
		assertThat(VectorN.from(42, 1337, 1).toJSONArray().toJSONString()).isEqualTo("[42.0,1337.0,1.0]");
	}
	
	
	@Test
	public void testIsCloseTo()
	{
		assertThat(VectorN.zero(3).isCloseTo(VectorN.zero(3), 0)).isTrue();
		assertThat(VectorN.zero(3).isCloseTo(VectorN.from(1, 0, 0), 0.1)).isFalse();
		assertThat(VectorN.from(42, 1337, 0).isCloseTo(VectorN.from(42, 1337, 0), 1e-6)).isTrue();
		assertThat(VectorN.from(42, 1337, 0).isCloseTo(Vector2.zero())).isFalse();
	}
	
	
	@Test
	public void testMeanVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(VectorN.from(24, 234, -4));
		list.add(VectorN.from(42, 133, 3));
		list.add(VectorN.from(123, -321, 20));
		double muX = StatisticsMath.mean(list.stream().map(IVector::x).collect(Collectors.toList()));
		double muY = StatisticsMath.mean(list.stream().map(IVector::y).collect(Collectors.toList()));
		double muZ = StatisticsMath.mean(list.stream().map(IVector::z).collect(Collectors.toList()));
		IVector muVector = VectorN.meanVector(list);
		IVector muRefVector = VectorN.from(muX, muY, muZ);
		assertThat(muVector.isCloseTo(muRefVector, 1e-2)).withFailMessage("expected: %s, but: %s", muVector, muRefVector)
				.isTrue();
	}
	
	
	@Test
	public void testVarianceVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(VectorN.from(24, 0, 9));
		list.add(VectorN.from(42, -3, -5));
		list.add(VectorN.from(-21, 10, 21));
		double varX = StatisticsMath.variance(list.stream().map(IVector::x).collect(Collectors.toList()));
		double varY = StatisticsMath.variance(list.stream().map(IVector::y).collect(Collectors.toList()));
		double varZ = StatisticsMath.variance(list.stream().map(IVector::z).collect(Collectors.toList()));
		IVector varVector = VectorN.varianceVector(list);
		IVector varRefVector = VectorN.from(varX, varY, varZ);
		assertThat(varVector.isCloseTo(varRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", varVector, varRefVector).isTrue();
	}
	
	
	@Test
	public void testStdVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(VectorN.from(24, 0, -1));
		list.add(VectorN.from(42, -3, 0));
		list.add(VectorN.from(-21, 10, 41));
		double stdX = StatisticsMath.std(list.stream().map(IVector::x).collect(Collectors.toList()));
		double stdY = StatisticsMath.std(list.stream().map(IVector::y).collect(Collectors.toList()));
		double stdZ = StatisticsMath.std(list.stream().map(IVector::z).collect(Collectors.toList()));
		IVector stdVector = VectorN.stdVector(list);
		IVector stdRefVector = VectorN.from(stdX, stdY, stdZ);
		assertThat(stdVector.isCloseTo(stdRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", stdVector, stdRefVector).isTrue();
	}
	
	
	@Test
	public void testGet()
	{
		IVectorN vector = VectorN.from(42, 1337, 2);
		assertThat(vector.get(0)).isEqualTo(vector.x());
		assertThat(vector.get(1)).isEqualTo(vector.y());
		assertThat(vector.get(2)).isEqualTo(vector.z());
		assertThatThrownBy(() -> vector.get(3)).isExactlyInstanceOf(IllegalArgumentException.class);
	}
	
	
	@Test
	public void testAbs()
	{
		assertThat(VectorN.from(42, 1337, 2).absNew()).isEqualTo(VectorN.from(42, 1337, 2));
		assertThat(VectorN.from(-42, 1337, 2).absNew()).isEqualTo(VectorN.from(42, 1337, 2));
		assertThat(VectorN.from(-42, -1337, -2).absNew()).isEqualTo(VectorN.from(42, 1337, 2));
	}
	
	
	@Test
	public void testGetXYVector()
	{
		assertThat(VectorN.from(42, 1337, 0).getXYVector()).isEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	public void testGetXYZVector()
	{
		VectorN vector = VectorN.from(42, 1337, 3.14);
		assertThat(vector.getXYZVector()).isEqualTo(Vector3.fromXYZ(42, 1337, 3.14));
	}
	
	
	@Test
	public void testApply()
	{
		assertThat(VectorN.from(1, -2, 3).apply(v -> Math.abs(v * 3))).isEqualTo(VectorN.from(3, 6, 9));
	}
	
	
	@Test
	public void testRealVector()
	{
		RealVector realVector = new ArrayRealVector(4);
		assertThat(VectorN.fromReal(realVector).isCloseTo(VectorN.zero(4))).isTrue();
		realVector.setEntry(1, 42);
		assertThat(VectorN.fromReal(realVector).isCloseTo(VectorN.from(0, 42, 0, 0))).isTrue();
		assertThat(VectorN.fromReal(realVector).toRealVector()).isEqualTo(realVector);
	}
	
}
