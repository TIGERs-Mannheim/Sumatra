/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.PersistenceAiFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for persistence
 */
public class PersistenceTest
{
	@Test
	public void testSaveLoadRecordFrame()
	{
		PersistenceDb db = PersistenceDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		PersistenceTable<PersistenceAiFrame> aiTable = db.getTable(PersistenceAiFrame.class);

		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<PersistenceAiFrame> origFrames = helper.createOneRecordFrame();
		aiTable.write(origFrames);
		Long firstKey = db.getFirstKey();
		assertThat(firstKey).as("Inserted frame should be retrievable").isNotNull();
		aiTable.get(db.getFirstKey());
		db.close();

		db = PersistenceDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		aiTable = db.getTable(PersistenceAiFrame.class);
		firstKey = db.getFirstKey();
		assertThat(firstKey).as("Loaded database should contain the written frame").isNotNull();
		aiTable.get(db.getFirstKey());
		db.close();
	}


	@AfterEach
	public void after()
	{
		PersistenceTestHelper.cleanup();
	}


	@BeforeEach
	public void before()
	{
		PersistenceTestHelper.cleanup();
	}
}
