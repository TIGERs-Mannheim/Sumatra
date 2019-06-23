/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistence;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Tests different persistance frameworks (databases)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@BenchmarkMethodChart(filePrefix = "benchmark-listsRead")
@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 10, callgc = true)
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
public class PersistenceReadPerf
{
	static
	{
		System.setProperty("jub.customkey", String.valueOf(PersistenceTestHelper.MANY_FRAMES));
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	@Rule
	public TestRule	benchmarkRun	= new BenchmarkRule();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@AfterClass
	public static void afterClass()
	{
		PersistenceTestHelper.cleanup();
	}
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		Logger.getRootLogger().setLevel(Level.ERROR);
		SumatraSetupHelper.setupSumatra();
		
		PersistenceTestHelper.cleanup();
		
		PersistenceTestHelper helper = new PersistenceTestHelper();
		
		long startTime = SumatraClock.nanoTime();
		System.out.println("Start creating record frames");
		List<IRecordFrame> frames = helper.createManyRecordFrames();
		System.out.println("Creating record frames took " + (SumatraClock.nanoTime() - startTime));
		
		startTime = SumatraClock.nanoTime();
		RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(PersistenceTestHelper.DB_NAME);
		pers.saveFrames(frames);
		pers.close();
		System.out.println("Saving to berkeley took " + (SumatraClock.nanoTime() - startTime));
	}
	
	
	/**
	 */
	@Test
	public void testReadAllBerkeley()
	{
		RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(PersistenceTestHelper.DB_NAME);
		List<IRecordFrame> frames = pers.load();
		pers.close();
		checkSize(frames);
		PersistenceTestHelper.checkFrames(frames);
	}
	
	
	private void checkSize(List<IRecordFrame> frames)
	{
		Assert.assertEquals("Number of loaded frames does not match number of saved ones",
				PersistenceTestHelper.MANY_FRAMES, frames.size());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
