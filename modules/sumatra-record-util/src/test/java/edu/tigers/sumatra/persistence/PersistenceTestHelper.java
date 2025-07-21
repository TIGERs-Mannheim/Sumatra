/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Some methods used by Junit Tests for persistence
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PersistenceTestHelper
{
	/**  */
	public static final String DB_NAME = System.getProperty("java.io.tmpdir") + "/test";
	
	private final FrameFactory frameFactory = new FrameFactory();
	
	
	/**
	  * 
	  */
	public PersistenceTestHelper()
	{
		
	}
	
	
	/**
	 * Delete dirs recursively
	 *
	 * @param file
	 * @throws IOException
	 */
	private static void delete(final File file)
	{
		String[] files = file.list();
		if (files != null)
		{
			// directory is empty, then delete it
			if (files.length == 0)
			{
				Validate.isTrue(file.delete());
			} else
			{
				for (String temp : files)
				{
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				files = file.list();
				if (files != null && files.length == 0)
				{
					Validate.isTrue(file.delete());
				}
			}
		} else if (file.isFile())
		{
			// if file, then delete it
			Validate.isTrue(file.delete());
		}
	}
	
	
	/**
	 * @param wfw
	 * @param teamColor
	 * @return
	 */
	private VisualizationFrame createVisualizationFrame(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		final AIInfoFrame aiFrame = frameFactory.createFullAiInfoFrame(teamColor, wfw);
		return new VisualizationFrame(aiFrame);
	}
	
	
	/**
	 * @param wfw
	 * @return
	 */
	private PersistenceAiFrame createRecordFrame(final WorldFrameWrapper wfw)
	{
		PersistenceAiFrame recFrame = new PersistenceAiFrame(wfw.getTimestamp());
		recFrame.addVisFrame(createVisualizationFrame(wfw, ETeamColor.YELLOW));
		recFrame.addVisFrame(createVisualizationFrame(wfw, ETeamColor.BLUE));
		return recFrame;
	}
	
	
	/**
	 * @param wfw
	 * @return
	 */
	private List<PersistenceAiFrame> createOneRecordFrame(final WorldFrameWrapper wfw)
	{
		List<PersistenceAiFrame> frames = new ArrayList<>(1);
		frames.add(createRecordFrame(wfw));
		return frames;
	}
	
	
	/**
	 * @return
	 */
	public List<PersistenceAiFrame> createOneRecordFrame()
	{
		return createOneRecordFrame(frameFactory.createWorldFrameWrapper(0, 0));
	}
	
	
	/**
	 */
	public static void cleanup()
	{
		PersistenceTestHelper.delete(new File(DB_NAME));
	}
}
