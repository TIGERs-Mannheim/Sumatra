/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.ai.BerkeleyAiFrame;


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
		BerkeleyDb db = BerkeleyDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.open();
		
		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<BerkeleyAiFrame> origFrames = helper.createOneRecordFrame();
		db.write(BerkeleyAiFrame.class, origFrames);
		db.close();
		
		db = BerkeleyDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.open();
		db.get(BerkeleyAiFrame.class, db.getFirstKey());
		db.close();
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
}
