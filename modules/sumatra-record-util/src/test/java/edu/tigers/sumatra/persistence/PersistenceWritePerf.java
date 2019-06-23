/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;

import edu.tigers.sumatra.model.SumatraModel;


/**
 * Tests different persistence frameworks (databases)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@BenchmarkMethodChart(filePrefix = "benchmark-lists")
@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 5, callgc = true)
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
public class PersistenceWritePerf
{
	static
	{
		System.setProperty("jub.customkey", String.valueOf(PersistenceTestHelper.MANY_FRAMES));
	}
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final List<RecordFrame>	manyRecordFrames;
	private final List<RecordFrame>	oneRecordFrame;
	
	
	/**  */
	@Rule
	public TestRule						benchmarkRun	= new BenchmarkRule();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public PersistenceWritePerf()
	{
		SumatraModel.noLogging();
		
		PersistenceTestHelper helper = new PersistenceTestHelper();
		manyRecordFrames = helper.createManyRecordFrames();
		oneRecordFrame = helper.createOneRecordFrame();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Before
	public void before()
	{
		PersistenceTestHelper.cleanup();
	}
	
	
	/**
	 */
	@AfterClass
	public static void afterClass()
	{
		PersistenceTestHelper.cleanup();
	}
	
	
	/**
	 * Save single record frame to berkeley db
	 */
	@Test
	@BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 100)
	public void testBerkeley()
	{
		AiBerkeleyPersistence pers = new AiBerkeleyPersistence(
				System.getProperty("java.io.tmpdir") + "/" + PersistenceTestHelper.DB_NAME);
		pers.open();
		pers.saveRecordFrames(oneRecordFrame);
		pers.close();
	}
	
	
	/**
	 */
	@Test
	@BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 50)
	public void testManyBerkeley()
	{
		AiBerkeleyPersistence pers = new AiBerkeleyPersistence(
				System.getProperty("java.io.tmpdir") + "/" + PersistenceTestHelper.DB_NAME);
		pers.open();
		pers.saveRecordFrames(manyRecordFrames);
		pers.close();
	}
	
	
	/**
	 */
	// @Test
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
	public void testEndlessBerkeley()
	{
		PersistenceTestHelper.printMemoryUsage();
		AiBerkeleyPersistence pers = new AiBerkeleyPersistence(
				System.getProperty("java.io.tmpdir") + "/" + PersistenceTestHelper.DB_NAME);
		pers.open();
		PersistenceTestHelper helper = new PersistenceTestHelper();
		int sum = 0;
		try
		{
			while (true)
			{
				List<RecordFrame> frames = helper.createManyRecordFrames();
				pers.saveRecordFrames(frames);
				sum += frames.size();
				PersistenceTestHelper.printMemoryUsage();
				System.out.println("Sum: " + sum);
			}
		} finally
		{
			pers.close();
		}
	}
}
