/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 22, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;


/**
 * Compare reflection vs direct invocation of constructor
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@BenchmarkMethodChart(filePrefix = "benchmark-reflection")
@BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, callgc = true)
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
@Ignore
public class RoleReflectionPerf extends AbstractBenchmark
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int			NUM_RUNS	= 1000000;
	/**  */
	protected DummyObject				object;
	private Constructor<DummyObject>	constructor;
												
												
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	static
	{
		System.setProperty("jub.customkey", String.valueOf(NUM_RUNS));
	}
	
	
	/**
	  * 
	  */
	public RoleReflectionPerf()
	{
		try
		{
			constructor = DummyObject.class.getConstructor();
		} catch (NoSuchMethodException err)
		{
			err.printStackTrace();
		} catch (SecurityException err)
		{
			err.printStackTrace();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Test
	public void testReflection()
	{
		List<Object> list = new ArrayList<Object>(NUM_RUNS);
		for (int i = 0; i < NUM_RUNS; i++)
		{
			try
			{
				object = constructor.newInstance();
				list.add(object);
			} catch (InstantiationException err)
			{
				err.printStackTrace();
			} catch (IllegalAccessException err)
			{
				err.printStackTrace();
			} catch (IllegalArgumentException err)
			{
				err.printStackTrace();
			} catch (InvocationTargetException err)
			{
				err.printStackTrace();
			}
		}
	}
	
	
	/**
	 */
	@Test
	public void testNormal()
	{
		for (int i = 0; i < NUM_RUNS; i++)
		{
			object = new DummyObject();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
