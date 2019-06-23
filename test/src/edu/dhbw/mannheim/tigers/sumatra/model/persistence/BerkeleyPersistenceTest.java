/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistence;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.oval.Validator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * Unit tests for berkeley persistence
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	
	
	static
	{
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
	 * 
	 */
	@Test
	public void testLoadAllBerkeleyDbs()
	{
		String home = System.getProperty("user.home");
		String base = home + "/.sumatra-berkeley/";
		new File(base).mkdir();
		DateFormat dfmt = new SimpleDateFormat("yy-MM-dd");
		String dbName = "AI_" + dfmt.format(new Date());
		
		// create new db for today into home folder, if not already existing
		if (!new File(base + dbName).exists())
		{
			System.out.println("Create new db: " + dbName);
			RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(base, dbName, false);
			PersistenceTestHelper helper = new PersistenceTestHelper();
			List<IRecordFrame> origFrames = helper.createOneRecordFrame();
			PersistenceTestHelper.checkFrames(origFrames);
			pers.saveFrames(origFrames);
			pers.close();
		}
		
		// load all dbs from home folder
		for (File file : new File(base).listFiles())
		{
			System.out.println("Check db: " + file.getName());
			RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(base, file.getName(), true);
			List<IRecordFrame> frames = pers.load();
			pers.close();
			if (frames.size() == 1)
			{
				Validator validator = new Validator();
				// disable some fields that are not valid due to compatibility
				validator.disableProfile("compatibility");
				PersistenceTestHelper.checkFrames(frames, validator);
			} else
			{
				System.out.println("db + " + file.getName() + " broken.");
			}
		}
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
