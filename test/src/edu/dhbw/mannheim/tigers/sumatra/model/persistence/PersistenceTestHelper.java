/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;

import org.junit.Assert;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;
import edu.dhbw.mannheim.tigers.sumatra.util.FrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Some methods used by Junit Tests for persistence
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PersistenceTestHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	public static final int		MANY_FRAMES	= 1000;
	/**  */
	public static final String	DB_NAME		= "test";
	
	private final FrameFactory	frameFactory;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public PersistenceTestHelper()
	{
		frameFactory = new FrameFactory();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public IRecordFrame createRecordFrame()
	{
		final IRecordFrame aiFrame = frameFactory.createFullAiInfoFrame();
		return new RecordFrame(aiFrame);
	}
	
	
	/**
	 * @return
	 */
	public List<IRecordFrame> createOneRecordFrame()
	{
		List<IRecordFrame> frames = new ArrayList<IRecordFrame>(1);
		frames.add(createRecordFrame());
		return frames;
	}
	
	
	/**
	 * @return
	 */
	public List<IRecordFrame> createManyRecordFrames()
	{
		List<IRecordFrame> frames = new ArrayList<IRecordFrame>(MANY_FRAMES);
		for (int i = 0; i < MANY_FRAMES; i++)
		{
			frames.add(createRecordFrame());
		}
		return frames;
	}
	
	
	/**
	 * Perform validation on all frames
	 * 
	 * @param frames
	 */
	public static void checkFrames(final List<IRecordFrame> frames)
	{
		checkFrames(frames, new Validator());
	}
	
	
	/**
	 * Perform validation on all frames
	 * 
	 * @param frames
	 * @param validator
	 */
	public static void checkFrames(final List<IRecordFrame> frames, final Validator validator)
	{
		long startTime = SumatraClock.nanoTime();
		for (IRecordFrame frame : frames)
		{
			List<ConstraintViolation> violations = validator.validate(frame);
			if (violations.size() > 0)
			{
				System.err.println(violations);
				for (ConstraintViolation v : violations)
				{
					printConstraintViolation(v);
				}
				Assert.fail("frame not valid");
			}
		}
		System.out.println("Validation of frames took " + ((SumatraClock.nanoTime() - startTime) * 1e-6) + "ms");
	}
	
	
	private static void printConstraintViolation(final ConstraintViolation cv)
	{
		System.err.println(cv.getMessage());
		if (cv.getCauses() == null)
		{
			return;
		}
		for (ConstraintViolation v : cv.getCauses())
		{
			printConstraintViolation(v);
		}
	}
	
	
	/**
	 * Delete dirs recursively
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void delete(final File file) throws IOException
	{
		
		if (file.isDirectory())
		{
			
			// directory is empty, then delete it
			if (file.list().length == 0)
			{
				
				file.delete();
				
			} else
			{
				
				// list all the directory contents
				String files[] = file.list();
				
				for (String temp : files)
				{
					// construct the file structure
					File fileDelete = new File(file, temp);
					
					// recursive delete
					delete(fileDelete);
				}
				
				// check the directory again, if empty then delete it
				if (file.list().length == 0)
				{
					file.delete();
				}
			}
			
		} else
		{
			// if file, then delete it
			file.delete();
		}
	}
	
	
	/**
	 */
	public static void cleanup()
	{
		try
		{
			PersistenceTestHelper.delete(new File(RecordBerkeleyPersistence.BASE_PATH + DB_NAME));
		} catch (IOException err)
		{
			err.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 */
	public static void printMemoryUsage()
	{
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		double totalMB = (Runtime.getRuntime().totalMemory()) / 1048576.0D;
		double usedMB = (usedMem) / 1048576.0D;
		System.out.println(String.format("Memory Usage: %.1fMB / %.1fMB", usedMB, totalMB));
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
