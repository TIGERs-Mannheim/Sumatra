/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.g3force.s2vconverter.String2ValueConverter;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class AVector implements IVector
{
	protected static final double EQUAL_TOL = SumatraMath.getEqualTol();
	
	static
	{
		String2ValueConverter.getDefault().addConverter(new VectorConverter());
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null)
		{
			return false;
		}
		if (!(o instanceof IVector))
		{
			return false;
		}
		
		final IVector vector = (IVector) o;
		if (getNumDimensions() != vector.getNumDimensions())
		{
			return false;
		}
		
		boolean identical = true;
		for (int d = 0; d < getNumDimensions(); d++)
		{
			identical = identical && (Double.compare(vector.get(d), get(d)) == 0);
		}
		if (identical)
		{
			return true;
		}
		boolean similar = true;
		for (int d = 0; d < getNumDimensions(); d++)
		{
			similar = similar && SumatraMath.isEqual(vector.get(d), get(d));
		}
		return similar;
	}
	
	
	@Override
	public final int hashCode()
	{
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		for (int d = 0; d < getNumDimensions(); d++)
		{
			builder.append(Math.round(get(d) * 1000));
		}
		return builder.toHashCode();
	}
	
	
	@Override
	public RealVector toRealVector()
	{
		RealVector rv = new ArrayRealVector(getNumDimensions());
		for (int i = 0; i < getNumDimensions(); i++)
		{
			rv.setEntry(i, get(i));
		}
		return rv;
	}
	
	
	/**
	 * @param list
	 * @return
	 */
	public static IVector fromNumberList(final List<? extends Number> list)
	{
		if (list.size() == 2)
		{
			return new Vector2(list.get(0).doubleValue(), list.get(1).doubleValue());
		} else if (list.size() == 3)
		{
			return Vector3.fromXYZ(list.get(0).doubleValue(), list.get(1).doubleValue(), list.get(2).doubleValue());
		}
		double[] arr = list.stream().mapToDouble(Number::doubleValue).toArray();
		return VectorN.from(arr);
	}
	
	
	/**
	 * Parse a string into a vector. <br>
	 * Vector Elements are separated with , or ; <br>
	 * Each element can be a number, 'pi' or simple calculation with +,-,*,/
	 *
	 * @param value a string as described above
	 * @return a new vector based on the string.
	 */
	public static IVector valueOf(final String value)
	{
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<>(3);
		for (String val : values)
		{
			if (!val.trim().isEmpty() && !val.contains(","))
			{
				finalValues.add(val.trim());
			}
		}
		double[] fvalues = new double[finalValues.size()];
		for (int i = 0; i < finalValues.size(); i++)
		{
			fvalues[i] = valueOfElement(finalValues.get(i));
		}
		return VectorN.from(fvalues);
	}
	
	
	private static Double valueOfElement(final String val)
	{
		String value = val.replaceAll("pi", String.valueOf(Math.PI));
		
		return parseStatement(value, "\\+", (x, y) -> x + y)
				.orElseGet(() -> parseStatement(value, "-", (x, y) -> x - y)
						.orElseGet(() -> parseStatement(value, "\\*", (x, y) -> x * y)
								.orElseGet(() -> parseStatement(value, "/", (x, y) -> x / y)
										.orElseGet(() -> value.isEmpty() ? 0.0 : Double.valueOf(value)))));
	}
	
	
	private static Optional<Double> parseStatement(final String statement, final String regex,
			final BiFunction<Double, Double, Double> operation)
	{
		String[] split = statement.split(regex, 2);
		if (split.length == 2)
		{
			return Optional.of(operation.apply(valueOfElement(split[0]), valueOfElement(split[1])));
		}
		return Optional.empty();
	}
	
	
	@Override
	public synchronized double getLength()
	{
		double sum = 0;
		for (int d = 0; d < getNumDimensions(); d++)
		{
			sum += get(d) * get(d);
		}
		return SumatraMath.sqrt(sum);
	}
	
	
	@Override
	public synchronized boolean isZeroVector()
	{
		return getLength() < EQUAL_TOL;
	}
	
	
	@Override
	public synchronized boolean isFinite()
	{
		for (int d = 0; d < getNumDimensions(); d++)
		{
			if (!Double.isFinite(get(d)))
			{
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	public synchronized double getLength2()
	{
		return SumatraMath.sqrt((x() * x()) + (y() * y()));
	}
	
	
	@Override
	public synchronized double[] toArray()
	{
		double[] arr = new double[getNumDimensions()];
		for (int d = 0; d < getNumDimensions(); d++)
		{
			arr[d] = get(d);
		}
		return arr;
	}
	
	
	@Override
	public synchronized String toString()
	{
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern("0.000");
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (getNumDimensions() > 0)
		{
			sb.append(df.format(get(0)));
			for (int d = 1; d < getNumDimensions(); d++)
			{
				sb.append(',');
				sb.append(df.format(get(d)));
			}
			sb.append("|l=");
			sb.append(df.format(getLength()));
			if ((getNumDimensions() > 1) && !getXYVector().isZeroVector())
			{
				sb.append("|a=");
				sb.append(df.format(getXYVector().getAngle()));
			}
		}
		sb.append(']');
		return sb.toString();
	}
	
	
	@Override
	public synchronized String getSaveableString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(x());
		for (int d = 1; d < getNumDimensions(); d++)
		{
			sb.append(';');
			sb.append(get(d));
		}
		return sb.toString();
	}
	
	
	@Override
	public synchronized JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<>();
		for (int d = 0; d < getNumDimensions(); d++)
		{
			jsonMapping.put("dim" + d, get(d));
		}
		return new JSONObject(jsonMapping);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized JSONArray toJSONArray()
	{
		JSONArray arr = new JSONArray();
		for (int d = 0; d < getNumDimensions(); d++)
		{
			arr.add(get(d));
		}
		return arr;
	}
	
	
	@Override
	public synchronized List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>(getNumDimensions());
		for (int d = 0; d < getNumDimensions(); d++)
		{
			numbers.add(get(d));
		}
		return numbers;
	}
	
	
	@Override
	public synchronized boolean isCloseTo(final IVector vec, final double distance)
	{
		if (vec.getNumDimensions() == getNumDimensions())
		{
			for (int d = 0; d < getNumDimensions(); d++)
			{
				if (!SumatraMath.isEqual(vec.get(d), get(d), distance))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	@Override
	public synchronized boolean isCloseTo(final IVector vec)
	{
		return isCloseTo(vec, EQUAL_TOL);
	}
	
	
	/**
	 * Mean value of a vector
	 *
	 * @param values
	 * @return
	 */
	public static IVector meanVector(final List<IVector> values)
	{
		VectorN sum = VectorN.empty();
		values.forEach(sum::add);
		return sum.multiplyNew(1.0f / values.size());
	}
	
	
	/**
	 * Variance of vectors
	 * 
	 * @param values
	 * @return
	 */
	public static IVector varianceVector(final List<IVector> values)
	{
		IVector mu = meanVector(values);
		List<IVector> val2 = new ArrayList<>(values.size());
		for (IVector v : values)
		{
			IVector diff = v.subtractNew(mu);
			val2.add(diff.applyNew(a -> a * a));
		}
		return meanVector(val2);
	}
	
	
	/**
	 * Standard deviation of vectors
	 *
	 * @param values
	 * @return
	 */
	public static IVector stdVector(final List<IVector> values)
	{
		IVector var = varianceVector(values);
		return var.applyNew(Math::sqrt);
	}
}
