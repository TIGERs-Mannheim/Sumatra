/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 4, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistence;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.persistance.RecordBerkeleyPersistence;
import edu.tigers.sumatra.persistance.RecordFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PersistenceDbSizeTest
{
	/**
	 * Save single record frame to berkeley db
	 */
	@Test
	@Ignore
	public void testSaveLoadBerkeleyRecordFrame()
	{
		PersistenceTestHelper helper = new PersistenceTestHelper();
		
		int[] numFramesArr = new int[] { 1 };
		for (int numFrames : numFramesArr)
		{
			RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(
					RecordBerkeleyPersistence.getDefaultBasePath() + "/dbSize-" + numFrames + "_"
							+ System.currentTimeMillis(),
					false);
			long t0 = System.nanoTime();
			List<RecordFrame> frames = helper.createManyRecordFrames(numFrames);
			long t1 = System.nanoTime();
			System.out.printf("Created %d frames in %.2fs\n", numFrames, (t1 - t0) / 1e9);
			pers.saveRecordFrames(frames);
			pers.close();
			long t2 = System.nanoTime();
			System.out.printf("Saved %d frames in %.2fs\n", numFrames, (t2 - t1) / 1e9);
		}
		helper.close();
	}
	
	
	/**
	 * 
	 */
	@Test
	@Ignore
	public void testCheckNumFrames()
	{
		RecordBerkeleyPersistence pers = new RecordBerkeleyPersistence(RecordBerkeleyPersistence.getDefaultBasePath() +
				"/2015-09-03_13-50-16", false);
		pers.close();
	}
}
