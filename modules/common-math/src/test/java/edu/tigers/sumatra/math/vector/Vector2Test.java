/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.StatisticsMath;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Class for testing several functions provided by {@link AVector2} and {@link Vector2}
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
class Vector2Test
{
	private static final double ACCURACY = 0.001;
	
	
	@Test
	void testTurnNew()
	{
		final Vector2 input = Vector2.fromXY(1, 0);
		Vector2 expected = Vector2.fromXY(0, 1);
		Vector2 output;
		
		output = input.turnNew(AngleMath.PI / 2.0);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		output = input.turnNew(-AngleMath.PI / 2.0);
		expected = Vector2.fromXY(0, -1);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		output = input.turnNew(4 * AngleMath.PI);
		expected = Vector2.copy(input);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
	}
	
	
	@Test
	void testScaleToNew()
	{
		Vector2 input = Vector2.fromXY(1, 0);
		Vector2 expected = Vector2.fromXY(5, 0);
		Vector2 output;
		
		output = input.scaleToNew(5);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		input = Vector2.fromXY(0, -1);
		expected = Vector2.fromXY(0, 0.5);
		output = input.scaleToNew(-0.5f);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		input = Vector2.fromXY(0, 0);
		expected = Vector2.fromXY(0, 0);
		output = input.scaleToNew(-8);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
	}
	
	
	@Test
	void testAdd()
	{
		Vector2 vec1 = Vector2.fromXY(2, 1);
		Vector2 vec2 = Vector2.fromXY(3, 4);
		
		vec1.add(vec2);
		assertEquals(Vector2.fromXY(5, 5), vec1);
		
		vec1 = Vector2.fromXY(-2, 0);
		vec1.add(vec2);
		assertEquals(Vector2.fromXY(1, 4), vec1);
		
		vec1 = Vector2.fromXY(-2, -3);
		vec2 = Vector2.fromXY(-1, 5);
		vec1.add(vec2);
		assertEquals(Vector2.fromXY(-3, 2), vec1);
	}
	
	
	@Test
	void testAddNew()
	{
		Vector2 vec1 = Vector2.fromXY(5, 7);
		Vector2 vec2 = Vector2.fromXY(1, 2);
		Vector2 result = vec1.addNew(vec2);
		assertEquals(Vector2.fromXY(6, 9), result);
		
		vec1 = Vector2.fromXY(-3, 0);
		vec2 = Vector2.fromXY(4, 2);
		result = vec1.addNew(vec2);
		assertEquals(Vector2.fromXY(1, 2), result);
		
		vec1 = Vector2.fromXY(-3, 5);
		vec2 = Vector2.fromXY(-4, -2);
		result = vec1.addNew(vec2);
		assertEquals(Vector2.fromXY(-7, 3), result);
		assertEquals(Vector2.fromXY(-3, 5), vec1);
	}
	
	
	@Test
	void testMultiply()
	{
		Vector2 vec1 = Vector2.fromXY(4, 7);
		double factor = 4.5;
		vec1.multiply(factor);
		assertEquals(Vector2.fromXY(18, 31.5), vec1);
		
		vec1 = Vector2.fromXY(1, 1);
		factor = AngleMath.PI;
		vec1.multiply(factor);
		assertEquals(vec1.x(), factor, ACCURACY);
		assertEquals(vec1.y(), factor, ACCURACY);
		
		vec1 = Vector2.fromXY(3, -5);
		factor = -2.1f;
		vec1.multiply(factor);
		assertEquals(vec1.x(), -6.3f, ACCURACY);
		assertEquals(vec1.y(), 10.5, ACCURACY);
		
		factor = 0;
		vec1.multiply(factor);
		assertThat(Vector2.fromXY(0, 0).isCloseTo(vec1)).isTrue();
	}
	
	
	@Test
	void testMultiplyNewFactor()
	{
		Vector2 vec1 = Vector2.fromXY(4, 7);
		double factor = 4.5;
		Vector2 result = vec1.multiplyNew(factor);
		assertEquals(Vector2.fromXY(18, 31.5), result);
		
		vec1 = Vector2.fromXY(1, 1);
		factor = AngleMath.PI;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), factor, ACCURACY);
		assertEquals(result.y(), factor, ACCURACY);
		assertEquals(Vector2.fromXY(1, 1), vec1);
		
		vec1 = Vector2.fromXY(3, -5);
		factor = -2.1f;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), -6.3f, ACCURACY);
		assertEquals(result.y(), 10.5, ACCURACY);
		
		factor = 0;
		result = vec1.multiplyNew(factor);
		assertThat(Vector2.fromXY(0, 0).isCloseTo(result)).isTrue();
	}
	
	
	@Test
	void testMultiplyNewVector()
	{
		assertThat(Vector2.zero().multiplyNew(Vector2.fromXY(1, 1))).isEqualTo(Vector2.zero());
		assertThat(Vector2.fromXY(1, 1).multiplyNew(Vector2.fromXY(1, 1))).isEqualTo(Vector2.fromXY(1, 1));
		assertThat(Vector2.fromXY(42, 1337).multiplyNew(Vector2.fromXY(5, 2))).isEqualTo(Vector2.fromXY(210, 2674));
	}
	
	
	@Test
	void testSubtract()
	{
		Vector2 vec1 = Vector2.fromXY(4, 2);
		Vector2 vec2 = Vector2.fromXY(3, 5);
		vec1.subtract(vec2);
		assertEquals(Vector2.fromXY(1, -3), vec1);
		
		vec1 = Vector2.fromXY(1, 5);
		vec2 = Vector2.fromXY(-2, 5);
		vec1.subtract(vec2);
		assertEquals(Vector2.fromXY(3, 0), vec1);
		
		vec2 = Vector2.fromXY(2.4f, -3.1f);
		vec1.subtract(vec2);
		assertEquals(vec1.x(), 0.6, ACCURACY);
		assertEquals(vec1.y(), 3.1, ACCURACY);
	}
	
	
	@Test
	void testSubtractNew()
	{
		Vector2 vec1 = Vector2.fromXY(5, 7);
		Vector2 vec2 = Vector2.fromXY(1, 2);
		Vector2 result = vec1.subtractNew(vec2);
		assertEquals(Vector2.fromXY(4, 5), result);
		
		vec1 = Vector2.fromXY(-3, 0);
		vec2 = Vector2.fromXY(4, 2);
		result = vec1.subtractNew(vec2);
		assertEquals(Vector2.fromXY(-7, -2), result);
		
		vec1 = Vector2.fromXY(-3, 5);
		vec2 = Vector2.fromXY(-4, -2);
		result = vec1.subtractNew(vec2);
		assertEquals(Vector2.fromXY(1, 7), result);
		assertEquals(Vector2.fromXY(-3, 5), vec1);
	}
	
	
	@Test
	void testScaleTo()
	{
		Vector2 vec1 = Vector2.fromXY(3, -4);
		vec1.scaleTo(15.0f);
		assertEquals(Vector2.fromXY(9, -12), vec1);
		
		vec1 = Vector2.fromXY(2.5f, 2.5);
		vec1.scaleTo(12.5f);
		assertEquals(vec1.x(), 8.8388, ACCURACY);
		assertEquals(vec1.y(), 8.8388, ACCURACY);
		
		vec1 = Vector2.fromXY(0, 0);
		vec1.scaleTo(8);
		assertEquals(Vector2.fromXY(0, 0), vec1);
	}
	
	
	@Test
	void testTurn()
	{
		Vector2 vec1 = Vector2.fromXY(1, 0);
		vec1.turn(AngleMath.PI);
		assertEquals(vec1.x(), -1, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
		
		vec1 = Vector2.fromXY(1, 0);
		vec1.turn(AngleMath.PI * (1.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = Vector2.fromXY(1, 0);
		vec1.turn(AngleMath.PI * (-0.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = Vector2.fromXY(0, 0);
		vec1.turn(AngleMath.PI * (1.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
	}
	
	
	@Test
	void testTurnTo()
	{
		Vector2 vec1 = Vector2.fromXY(1, 0);
		vec1.turnTo(AngleMath.PI);
		assertEquals(vec1.x(), -1, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
		
		vec1 = Vector2.fromXY(1, 0);
		vec1.turnTo(AngleMath.PI * (-0.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = Vector2.fromXY(0, 0);
		vec1.turnTo(AngleMath.PI);
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
	}
	
	
	@Test
	void testScalarProduct()
	{
		Vector2 vec1 = Vector2.fromXY(1, 3);
		Vector2 vec2 = Vector2.fromXY(-4, 2);
		double result = vec1.scalarProduct(vec2);
		assertEquals(2f, result, ACCURACY);
		
		vec1 = Vector2.fromXY(1, 3);
		vec2 = Vector2.fromXY(0, 0);
		result = vec1.scalarProduct(vec2);
		assertEquals(0f, result, ACCURACY);
		
		vec1 = Vector2.fromXY(2.1f, 3);
		vec2 = Vector2.fromXY(10f, 1.5);
		result = vec1.scalarProduct(vec2);
		assertEquals(25.5f, result, ACCURACY);
	}
	
	
	@Test
	void testNormalize()
	{
		assertThat(Vector2.fromXY(42, 1337).normalize().getLength2()).isCloseTo(1, within(1e-6));
		assertThat(Vector2.fromXY(42, 1337).normalizeNew().getLength2()).isCloseTo(1, within(1e-6));
		assertThat(Vector2.zero().normalize().getLength2()).isCloseTo(0, within(1e-6));
		assertThat(Vector2.zero().normalizeNew().getLength2()).isCloseTo(0, within(1e-6));
	}
	
	
	@Test
	void testGetLength2()
	{
		assertThat(Vector2.fromX(42).getLength2()).isEqualTo(42);
		assertThat(Vector2.fromY(-42).getLength2()).isEqualTo(42);
		assertThat(Vector2.fromXY(2, 2).getLength2()).isCloseTo(Math.sqrt(8), within(1e-6));
		assertThat(Vector2.fromXY(42, -1337).getLength2()).isCloseTo(Vector2.fromXY(42, -1337).getLength(), within(1e-6));
	}
	
	
	@Test
	void testGetAngle()
	{
		assertThat(Vector2.fromX(1).getAngle()).isCloseTo(0, within(ACCURACY));
		assertThat(Vector2.fromY(1).getAngle()).isCloseTo(AngleMath.PI_HALF, within(ACCURACY));
		assertThat(Vector2.fromX(-1).getAngle()).isCloseTo(AngleMath.PI, within(ACCURACY));
		assertThat(Vector2.fromXY(1, 1).getAngle()).isCloseTo(AngleMath.PI_QUART, within(ACCURACY));
		assertThat(Vector2.zero().getAngle(42)).isCloseTo(42.0, within(ACCURACY));
		assertThat(Vector2.fromX(1).getAngle(42)).isCloseTo(0.0, within(ACCURACY));
	}
	
	
	@Test
	void testEquals()
	{
		Vector2 vec1 = Vector2.fromXY(5, 0);
		Vector2 vec2 = Vector2.fromXY(5, 0);
		Boolean result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = Vector2.fromXY(3.1f, 1.4);
		vec2 = Vector2.fromXY(3.1f, 1.4);
		result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = Vector2.fromXY(3.1f, 1.4);
		vec2 = Vector2.fromXY(3.1f, 1.3);
		result = vec1.equals(vec2);
		assertTrue(!result);
		
		EqualsVerifier.forClass(Vector2.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
	
	
	@Test
	void testFromAngle()
	{
		assertThat(Vector2.fromAngle(1).getAngle()).isEqualTo(1, within(ACCURACY));
		assertThat(Vector2.fromAngle(-1).getAngle()).isEqualTo(-1, within(ACCURACY));
		assertThat(Vector2.fromAngle(AngleMath.PI).getAngle()).isCloseTo(AngleMath.PI, within(ACCURACY));
		assertThat(Vector2.fromAngle(3 * AngleMath.PI).getAngle()).isCloseTo(AngleMath.PI, within(ACCURACY));
	}
	
	
	@Test
	void testFromPoints()
	{
		assertThat(Vector2.fromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1))).isEqualTo(Vector2.fromXY(1, 1));
		assertThat(Vector2.fromPoints(Vector2.fromXY(1, 0), Vector2.fromXY(2, 1))).isEqualTo(Vector2.fromXY(1, 1));
	}
	
	
	@Test
	void testValueOf()
	{
		assertThat(Vector2.valueOf("0,0").isCloseTo(Vector2f.ZERO_VECTOR)).isTrue();
		assertThat(Vector2.valueOf("42,21").isCloseTo(Vector2.fromXY(42, 21))).isTrue();
		assertThat(Vector2.valueOf("pi,0").isCloseTo(Vector2.fromXY(AngleMath.PI, 0))).isTrue();
		assertThat(Vector2.valueOf("21+21,1*42").isCloseTo(Vector2.fromXY(42, 42))).isTrue();
		assertThat(Vector2.valueOf("1+2*3-7,2/4").isCloseTo(Vector2.fromXY(0, 0.5))).isTrue();
	}
	
	
	@Test
	void testSet()
	{
		Vector2 vector = Vector2.zero();
		assertThat(vector.isCloseTo(Vector2f.ZERO_VECTOR)).isTrue();
		vector.set(0, 42);
		assertThat(vector.isCloseTo(Vector2.fromX(42))).isTrue();
		vector.set(1, 21);
		assertThat(vector.isCloseTo(Vector2.fromXY(42, 21))).isTrue();
		assertThatThrownBy(() -> vector.set(2, 1)).isInstanceOfAny(IllegalArgumentException.class);
	}
	
	
	@Test
	void testRealVector()
	{
		RealVector realVector = new ArrayRealVector(2);
		assertThat(Vector2.fromReal(realVector).isCloseTo(Vector2f.ZERO_VECTOR)).isTrue();
		realVector.setEntry(1, 42);
		assertThat(Vector2.fromReal(realVector).isCloseTo(Vector2.fromXY(0, 42))).isTrue();
		assertThat(Vector2.fromReal(realVector).toRealVector()).isEqualTo(realVector);
	}
	
	
	@Test
	void testIsZeroVector()
	{
		assertThat(Vector2.fromXY(0, 0).isZeroVector()).isTrue();
		assertThat(Vector2.fromXY(1e-8, 0).isZeroVector()).isTrue();
		assertThat(Vector2.fromXY(1e-8, 1e-8).isZeroVector()).isTrue();
		assertThat(Vector2.fromXY(1e-2, 0).isZeroVector()).isFalse();
	}
	
	
	@Test
	void testIsFinite()
	{
		assertThat(Vector2.fromX(0).isFinite()).isTrue();
		assertThat(Vector2.fromX(Double.NaN).isFinite()).isFalse();
		assertThat(Vector2.fromX(Double.POSITIVE_INFINITY).isFinite()).isFalse();
	}
	
	
	@Test
	void testGetSaveableString()
	{
		assertThat(Vector2.fromXY(42, 1337).getSaveableString())
				.isEqualTo(42d + ";" + 1337d);
	}
	
	
	@Test
	void testNumberList()
	{
		IVector2 vector = Vector2.fromXY(42, 1337);
		List<Number> nbrList = vector.getNumberList();
		assertThat(nbrList).contains(42d, 1337d);
		IVector newVector = Vector2.fromNumberList(nbrList);
		assertThat(newVector).isEqualTo(vector);
	}
	
	
	@Test
	void testToArray()
	{
		IVector2 vector = Vector2.fromXY(42, 1337);
		assertThat(vector.toArray()).isEqualTo(new double[] { 42d, 1337d });
	}
	
	
	@Test
	void testToString()
	{
		IVector2 vector1 = Vector2.fromXY(42, 0);
		assertThat(vector1.toString()).isEqualTo("[42.000,0.000|l=42.000|a=0.000]");
		IVector2 vector2 = Vector2.fromXY(0, 0.001);
		assertThat(vector2.toString()).isEqualTo("[0.000,0.001|l=0.001]");
		IVector2 vector3 = Vector2.fromXY(0, 0.01);
		assertThat(vector3.toString()).isEqualTo("[0.000,0.010|l=0.010|a=1.571]");
	}
	
	
	@Test
	void testToJSON()
	{
		assertThat(Vector2.fromXY(42, 1337).toJSON().toJson()).isEqualTo("{\"dim1\":1337.0,\"dim0\":42.0}");
	}
	
	
	@Test
	void testToJsonArray()
	{
		assertThat(Vector2.fromXY(42, 1337).toJsonArray().toJson()).isEqualTo("[42.0,1337.0]");
	}
	
	
	@Test
	void testIsCloseTo()
	{
		assertThat(Vector2.zero().isCloseTo(Vector2.zero(), 0)).isTrue();
		assertThat(Vector2.zero().isCloseTo(Vector2.fromX(1), 0.1)).isFalse();
		assertThat(Vector2.fromXY(42, 1337).isCloseTo(Vector2.fromXY(42, 1337), 1e-6)).isTrue();
		assertThat(Vector2.fromXY(42, 1337).isCloseTo(Vector3.zero())).isFalse();
	}
	
	
	@Test
	void testMeanVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector2.fromXY(24, 7331));
		list.add(Vector2.fromXY(42, 1337));
		list.add(Vector2.fromXY(1234, -5678));
		double muX = StatisticsMath.mean(list.stream().map(IVector::x).collect(Collectors.toList()));
		double muY = StatisticsMath.mean(list.stream().map(IVector::y).collect(Collectors.toList()));
		IVector muVector = Vector2.meanVector(list);
		IVector muRefVector = VectorN.from(muX, muY);
		assertThat(muVector.isCloseTo(muRefVector, 1e-2)).withFailMessage("expected: %s, but: %s", muVector, muRefVector)
				.isTrue();
	}
	
	
	@Test
	void testVarianceVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector2.fromXY(24, 0));
		list.add(Vector2.fromXY(42, -3));
		list.add(Vector2.fromXY(-21, 10));
		double varX = StatisticsMath.variance(list.stream().map(IVector::x).collect(Collectors.toList()));
		double varY = StatisticsMath.variance(list.stream().map(IVector::y).collect(Collectors.toList()));
		IVector varVector = Vector2.varianceVector(list);
		IVector varRefVector = VectorN.from(varX, varY);
		assertThat(varVector.isCloseTo(varRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", varVector, varRefVector).isTrue();
	}
	
	
	@Test
	void testStdVector()
	{
		List<IVector> list = new ArrayList<>();
		list.add(Vector2.fromXY(24, 0));
		list.add(Vector2.fromXY(42, -3));
		list.add(Vector2.fromXY(-21, 10));
		double stdX = StatisticsMath.std(list.stream().map(IVector::x).collect(Collectors.toList()));
		double stdY = StatisticsMath.std(list.stream().map(IVector::y).collect(Collectors.toList()));
		IVector stdVector = Vector2.stdVector(list);
		IVector stdRefVector = VectorN.from(stdX, stdY);
		assertThat(stdVector.isCloseTo(stdRefVector, 1e-2))
				.withFailMessage("expected: %s, but: %s", stdVector, stdRefVector).isTrue();
	}
	
	
	@Test
	void testGet()
	{
		IVector2 vector = Vector2.fromXY(42, 1337);
		assertThat(vector.get(0)).isEqualTo(vector.x());
		assertThat(vector.get(1)).isEqualTo(vector.y());
		assertThatThrownBy(() -> vector.get(2)).isExactlyInstanceOf(IllegalArgumentException.class);
	}
	
	
	@Test
	void testAbs()
	{
		assertThat(Vector2.fromXY(42, 1337).absNew()).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(Vector2.fromXY(-42, 1337).absNew()).isEqualTo(Vector2.fromXY(42, 1337));
		assertThat(Vector2.fromXY(-42, -1337).absNew()).isEqualTo(Vector2.fromXY(42, 1337));
	}
	
	
	@Test
	void testGetNormalVector()
	{
		assertThat(Vector2.fromXY(1, 0).getNormalVector()).isEqualTo(Vector2.fromXY(0, -1));
		assertThat(Vector2.fromXY(1, 1).getNormalVector()).isEqualTo(Vector2.fromXY(1, -1));
		assertThat(Vector2.fromXY(0, 1).getNormalVector()).isEqualTo(Vector2.fromXY(1, -0));
	}
	
	
	@Test
	void testIsVertical()
	{
		assertThat(Vector2.fromX(1).isVertical()).isFalse();
		assertThat(Vector2.fromY(1).isVertical()).isTrue();
		assertThat(Vector2.fromXY(1, 1).isVertical()).isFalse();
	}
	
	
	@Test
	void testIsHorizontal()
	{
		assertThat(Vector2.fromX(1).isHorizontal()).isTrue();
		assertThat(Vector2.fromY(1).isHorizontal()).isFalse();
		assertThat(Vector2.fromXY(1, 1).isHorizontal()).isFalse();
	}
	
	
	@Test
	void testGetXYZVector()
	{
		assertThat(Vector2.fromXY(42, 1337).getXYZVector()).isEqualTo(Vector3.fromXYZ(42, 1337, 0));
	}
	
	
	@Test
	void testDistanceTo()
	{
		assertThat(Vector2.zero().distanceTo(Vector2.fromX(1))).isEqualTo(1.0);
		assertThat(Vector2.zero().distanceTo(Vector2.fromY(1))).isEqualTo(1.0);
		assertThat(Vector2.zero().distanceTo(Vector2.fromXY(1, 1))).isCloseTo(Math.sqrt(2), within(1e-6));
		assertThat(Vector2.fromXY(42, 1337).distanceTo(Vector2.fromXY(42, 337))).isEqualTo(1000.0);
		assertThat(Vector2.fromXY(-42, 1337).distanceTo(Vector2.fromXY(42, 1337))).isEqualTo(84.0);
	}
	
	
	@Test
	void testIsParallelTo()
	{
		assertThat(Vector2.fromX(1).isParallelTo(Vector2.fromX(2))).isTrue();
		assertThat(Vector2.fromX(1).isParallelTo(Vector2.fromX(-5))).isTrue();
		assertThat(Vector2.fromX(1).isParallelTo(Vector2.fromY(2))).isFalse();
		assertThat(Vector2.fromX(1).isParallelTo(Vector2.fromXY(2, 1))).isFalse();
		assertThat(Vector2.fromY(1).isParallelTo(Vector2.fromY(1))).isTrue();
		assertThat(Vector2.fromXY(3, 7).isParallelTo(Vector2.fromXY(6, 14))).isTrue();
	}
	
	
	@Test
	void testAngleTo()
	{
		assertThat(Vector2.fromX(1).angleTo(Vector2.fromY(1)).get()).isEqualTo(AngleMath.PI_HALF, within(ACCURACY));
		assertThat(Vector2.fromX(1).angleTo(Vector2.fromX(-1)).get()).isEqualTo(AngleMath.PI, within(ACCURACY));
		assertThat(Vector2.fromY(1).angleTo(Vector2.fromX(1)).get()).isEqualTo(-AngleMath.PI_HALF, within(ACCURACY));
		assertThat(Vector2.fromX(1).angleTo(Vector2.fromX(1)).get()).isEqualTo(0.0, within(ACCURACY));
		assertThat(Vector2.zero().angleTo(Vector2.fromX(-1)).isPresent()).isFalse();
	}
	
	
	@Test
	void testAngleToAbs()
	{
		assertThat(Vector2.fromX(1).angleToAbs(Vector2.fromY(1)).get()).isEqualTo(AngleMath.PI_HALF, within(ACCURACY));
		assertThat(Vector2.fromX(1).angleToAbs(Vector2.fromX(-1)).get()).isEqualTo(AngleMath.PI, within(ACCURACY));
		assertThat(Vector2.fromY(1).angleToAbs(Vector2.fromX(1)).get()).isEqualTo(AngleMath.PI_HALF, within(ACCURACY));
		assertThat(Vector2.fromX(1).angleToAbs(Vector2.fromX(1)).get()).isEqualTo(0.0, within(ACCURACY));
		assertThat(Vector2.zero().angleToAbs(Vector2.fromX(-1)).isPresent()).isFalse();
	}
	
	
	@Test
	void testNearestTo()
	{
		List<IVector2> points = new ArrayList<>();
		points.add(Vector2.fromX(1));
		points.add(Vector2.fromY(1));
		points.add(Vector2.fromXY(1, 1));
		points.add(Vector2.fromXY(1, -2));
		assertThat(Vector2.zero().nearestTo(points)).isEqualTo(points.get(0));
		assertThat(Vector2.fromXY(0.5, 0).nearestTo(points)).isEqualTo(points.get(0));
		assertThat(Vector2.fromXY(1, 0).nearestTo(points)).isEqualTo(points.get(0));
		assertThat(Vector2.fromXY(0, 0.5).nearestTo(points)).isEqualTo(points.get(1));
		assertThat(Vector2.fromXY(0.5, -2).nearestTo(points)).isEqualTo(points.get(3));
		assertThat(Vector2.fromXY(2, 2).nearestTo(points)).isEqualTo(points.get(2));
		assertThat(Vector2.zero().nearestToOpt(Collections.singleton(Vector2.fromX(1))).get())
				.isEqualTo(Vector2.fromX(1));
		assertThat(Vector2.zero().nearestToOpt(Collections.emptyList()).isPresent()).isFalse();
		assertThat(Vector2.zero().nearestTo(Vector2.fromX(1), Vector2.fromX(2))).isEqualTo(Vector2.fromX(1));
	}


	@Test
	void testProjectOntoThis()
	{
		var nonZeroVector = Vector2.fromX(10);
		var zeroVector = Vector2.zero();

		var test = Vector2.fromY(2);
		assertThat(nonZeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());
		assertThat(zeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());

		test = Vector2.fromX(2);
		assertThat(nonZeroVector.projectOntoThis(test)).isEqualTo(Vector2.fromX(2));
		assertThat(zeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());

		test = Vector2.fromXY(2, 3);
		assertThat(nonZeroVector.projectOntoThis(test)).isEqualTo(Vector2.fromX(2));
		assertThat(zeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());

		nonZeroVector = Vector2.fromXY(10, 10);

		test = Vector2.fromXY(2, -2);
		assertThat(nonZeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());
		assertThat(zeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());

		test = Vector2.fromXY(2, 2);
		assertThat(nonZeroVector.projectOntoThis(test)).isEqualTo(Vector2.fromXY(2, 2));
		assertThat(zeroVector.projectOntoThis(test)).isEqualTo(Vector2.zero());
	}
}
