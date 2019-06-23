/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.json.simple.JSONObject;

import com.github.g3force.s2vconverter.String2ValueConverter;
import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class AVector implements IVector
{
	static
	{
		String2ValueConverter.getDefault().addConverter(new VectorConverter());
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
		return new Vector3(list.get(0).doubleValue(), list.get(1).doubleValue(), list.get(2).doubleValue());
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public static IVector valueOf(final String value)
	{
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<String>(3);
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
		return new VectorN(fvalues);
	}
	
	
	private static Double valueOfElement(final String val)
	{
		String value = val.replaceAll("pi", String.valueOf(Math.PI));
		String[] split = value.split("\\+", 2);
		if (split.length == 2)
		{
			return valueOfElement(split[0]) + valueOfElement(split[1]);
		}
		split = value.split("-", 2);
		if (split.length == 2)
		{
			return valueOfElement(split[0]) - valueOfElement(split[1]);
		}
		
		split = value.split("\\*", 2);
		if (split.length == 2)
		{
			return valueOfElement(split[0]) * valueOfElement(split[1]);
		}
		split = value.split("/", 2);
		if (split.length == 2)
		{
			return valueOfElement(split[0]) / valueOfElement(split[1]);
		}
		if (value.isEmpty())
		{
			return 0.0;
		}
		return Double.valueOf(value);
	}
	
	
	@Override
	public double x()
	{
		return get(0);
	}
	
	
	@Override
	public double y()
	{
		return get(1);
	}
	
	
	@Override
	public double z()
	{
		return get(2);
	}
	
	
	@Override
	public double w()
	{
		return get(3);
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
		for (int d = 0; d < getNumDimensions(); d++)
		{
			if (Math.abs(get(d)) > 1e-10)
			{
				return false;
			}
		}
		return true;
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
	public synchronized double[] toDoubleArray()
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
		DecimalFormat df = new DecimalFormat("0.000");
		StringBuilder sb = new StringBuilder();
		sb.append('[');
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
		Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
		for (int d = 0; d < getNumDimensions(); d++)
		{
			jsonMapping.put("dim" + d, x());
		}
		return new JSONObject(jsonMapping);
	}
	
	
	@Override
	public synchronized List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		for (int d = 0; d < getNumDimensions(); d++)
		{
			numbers.add(get(d));
		}
		return numbers;
	}
	
	
	@Override
	public synchronized boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof IVector)
		{
			final IVector vec = (IVector) obj;
			return equals(vec, SumatraMath.EQUAL_TOL);
		}
		return false;
	}
	
	
	@Override
	public synchronized boolean equals(final IVector vec, final double tolerance)
	{
		if (vec == null)
		{
			return false;
		}
		if (vec.getNumDimensions() == getNumDimensions())
		{
			for (int d = 0; d < getNumDimensions(); d++)
			{
				if (!SumatraMath.isEqual(vec.get(d), get(d), tolerance))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	@Override
	public synchronized int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		for (int d = 0; d < getNumDimensions(); d++)
		{
			temp = Double.doubleToLongBits(d);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
		}
		return result;
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
		return var.applyNew(a -> Math.sqrt(a));
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
	 * Mean value of a vector
	 * 
	 * @param values
	 * @return
	 */
	public static IVector meanVector(final List<IVector> values)
	{
		VectorNd sum = new VectorNd();
		for (IVector v : values)
		{
			sum.add(v);
		}
		return sum.multiply(1.0f / values.size());
	}
}
