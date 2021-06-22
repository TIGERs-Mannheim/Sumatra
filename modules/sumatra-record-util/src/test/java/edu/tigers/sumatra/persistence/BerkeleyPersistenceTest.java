/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for berkeley persistence
 */
public class BerkeleyPersistenceTest
{
	@Test
	public void testSaveLoadBerkeleyRecordFrame()
	{
		BerkeleyDb db = BerkeleyDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.open();

		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<BerkeleyAiFrame> origFrames = helper.createOneRecordFrame();
		db.write(BerkeleyAiFrame.class, origFrames);
		Long firstKey = db.getFirstKey();
		assertThat(firstKey).as("Inserted frame should be retrievable").isNotNull();
		db.get(BerkeleyAiFrame.class, db.getFirstKey());
		db.close();

		db = BerkeleyDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.open();
		firstKey = db.getFirstKey();
		assertThat(firstKey).as("Loaded database should contain the written frame").isNotNull();
		db.get(BerkeleyAiFrame.class, db.getFirstKey());
		db.close();
	}


	@After
	public void after()
	{
		PersistenceTestHelper.cleanup();
	}


	@Before
	public void before()
	{
		PersistenceTestHelper.cleanup();
	}
}
