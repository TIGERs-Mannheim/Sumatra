/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for berkeley persistence
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyPersistenceTest
{
	/**
	 * Save single record frame to berkeley db
	 */
	@Test
	public void testSaveLoadBerkeleyRecordFrame()
	{
		AiBerkeleyPersistence pers = new AiBerkeleyPersistence(PersistenceTestHelper.DB_NAME);
		pers.open();
		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<RecordFrame> origFrames = helper.createOneRecordFrame();
		pers.saveRecordFrames(origFrames);
		pers.close();
		
		pers = new AiBerkeleyPersistence(PersistenceTestHelper.DB_NAME);
		pers.open();
		pers.getRecordFrame(pers.getFirstKey());
		pers.close();
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
