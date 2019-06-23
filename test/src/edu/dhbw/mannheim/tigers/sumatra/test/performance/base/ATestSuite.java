/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.09.2010
 * Author(s):
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.performance.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This is a base class for a test-suite consisting of several {@link ATestCase}s.
 * TODO It seems as if the order in which the test-cases are performed
 * influences the performance. Maybe we should another way then execute the
 * test-cases one after the other: maybe round robin, or Monte-Carlo-Order...?
 * 
 * @author Gero
 */
public abstract class ATestSuite
{
	
	protected final String					name;
	
	private FileWriter						tmpout;
	

	// TestCases
	private final ArrayList<ATestCase>	testCases	= new ArrayList<ATestCase>();
	
	
	protected void addTestCase(ATestCase testCase)
	{
		testCases.add(testCase);
	}
	
	
	// Times
	private int				testNumber		= 0;
	
	private long			testStartTime	= 0;
	private List<Long>	startTimes;
	private List<Long>	endTimes;
	private List<Long>	durations;
	private long			testStopTime	= 0;
	private long			duration			= 0;
	
	// Statistics
	private long			durationMax;
	private double			durationAverage;
	
	
	public ATestSuite(String name)
	{
		this.name = name;
	}
	

	public void test(int numberOfTests)
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		File file = new File("tmpout");
		
		try
		{
			tmpout = new FileWriter(file);
		} catch (IOException err1)
		{
			err1.printStackTrace();
		}
		
		System.out.println("##### START TestSuite: " + name + " ############################################");
		
		for (ATestCase testCase : testCases)
		{
			prepareTest(numberOfTests);
			
			testStartTime = System.nanoTime();
			
			long tmpStartTime = 0;
			long tmpEndTime = 0;
			
			// Do test
			while (testNumber < numberOfTests)
			{
				
				testCase.prepare();
				
				tmpStartTime = System.nanoTime();
				
				testCase.run();
				
				endTimes.add(tmpEndTime = System.nanoTime());
				startTimes.add(tmpStartTime);
				durations.add(tmpEndTime - tmpStartTime);
				
				testCase.writeRandomData();	// Prevent JIT-Compiler to erase the whole testcase
				testCase.teardown();
				
				testNumber++;
			}
			
			testStopTime = System.nanoTime();
			duration = testStopTime - testStartTime;
			
			// Statistics
			calculateStatistics();
			printResults(testCase.getName(), numberOfTests);
		}
		
		System.out.println("##### END TestSuite: " + name + " ############################################");
		
		try
		{
			tmpout.close();
		} catch (IOException err)
		{
			err.printStackTrace();
		}
		tmpout = null;
		
		file.delete();
	}
	

	private void prepareTest(int numberOfTests)
	{
		// Prepare infrastructure
		testNumber = 0;
		
		startTimes = new ArrayList<Long>(numberOfTests);
		endTimes = new ArrayList<Long>(numberOfTests);
		durations = new ArrayList<Long>(numberOfTests);
		
		testStartTime = 0;
		testStopTime = 0;
		duration = 0;
		
		durationMax = 0;
		durationAverage = 0;
	}
	

	private void calculateStatistics()
	{
		
		final int size = durations.size();
		
		for (int i = 0; i < size; i++)
		{
			
			long currentDuration = durations.get(i);
			durationAverage += currentDuration;
			
			if (currentDuration > durationMax)
			{
				durationMax = currentDuration;
			}
		}
		
		durationAverage /= size;
	}
	

	public void printResults(String name, int numberOfTests)
	{
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println(new Date() + ": " + name);
		System.out.println("Total (tests): " + numberOfTests);
		System.out.println("Total (duration) : " + duration);
		System.out.println("Duration (max): " + durationMax + "ns");
		System.out.println("Duration (avg): " + durationAverage + "ns");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
	

	protected void write(String str)
	{
		if (tmpout != null)
		{
			try
			{
				tmpout.write(str);
			} catch (IOException err)
			{
				err.printStackTrace();
			}
		}
	}
	

	public long getTestStartTime()
	{
		return this.testStartTime;
	}
	

	public List<Long> getStartTimes()
	{
		return this.startTimes;
	}
	

	public List<Long> getEndTimes()
	{
		return this.endTimes;
	}
	

	public List<Long> getDurations()
	{
		return this.durations;
	}
	

	public long getTestStopTime()
	{
		return this.testStopTime;
	}
	

	public long getDuration()
	{
		return this.duration;
	}
}
