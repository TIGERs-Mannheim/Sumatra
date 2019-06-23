/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.persistence;

import java.util.List;

import net.sf.oval.Validator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * Unit tests for berkeley persistence
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BerkeleyPersistenceTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public BerkeleyPersistenceTest()
	{
	}
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		SumatraSetupHelper.noLogging();
		SumatraSetupHelper.setupSumatra();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Save single record frame to berkeley db
	 */
	@Test
	public void testSaveLoadBerkeleyRecordFrame()
	{
		RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(PersistenceTestHelper.DB_NAME);
		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<IRecordFrame> origFrames = helper.createOneRecordFrame();
		PersistenceTestHelper.checkFrames(origFrames);
		pers.saveFrames(origFrames);
		pers.close();
		System.out.println("Saved");
		
		pers = new RecordBerkeleyPersistence(PersistenceTestHelper.DB_NAME, true);
		List<IRecordFrame> frames = pers.load();
		pers.close();
		
		Assert.assertEquals("Must be one frame", 1, frames.size());
		PersistenceTestHelper.checkFrames(frames);
	}
	
	
	/**
	 * Loads frames from an existing database.
	 * Aim: Test if structural changes eliminated support for old databases.
	 * Version must be increased in this case!
	 */
	@Test
	public void testLoadBerkeleyRecordFrame()
	{
		RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence("testdata", "berkeleyRecordFrame", true);
		List<IRecordFrame> frames = pers.load();
		pers.close();
		Assert.assertEquals("Must be one frame", 1, frames.size());
		Validator validator = new Validator();
		// disable some fields that are not valid due to compatibility
		validator.disableProfile("compatibility");
		PersistenceTestHelper.checkFrames(frames, validator);
	}
	
	
	/**
	 */
	@After
	public void after()
	{
		PersistenceTestHelper.cleanup();
	}
	
	
	/**
	 */
	@Before
	public void before()
	{
		PersistenceTestHelper.cleanup();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
