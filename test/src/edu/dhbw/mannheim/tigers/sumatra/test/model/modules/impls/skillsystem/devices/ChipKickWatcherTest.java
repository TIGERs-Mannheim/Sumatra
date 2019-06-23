/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.model.modules.impls.skillsystem.devices;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipBallWatcher;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * 
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ChipKickWatcherTest
{
	private static final Logger	log	= Logger.getLogger(ChipKickWatcherTest.class.getName());
	
	static
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	private List<CamDetectionFrame> readFrames()
	{
		Path path = FileSystems.getDefault().getPath("testdata", "ballData1396630002354.csv");
		final List<String> lines;
		try
		{
			lines = Files.readAllLines(path, Charset.defaultCharset());
		} catch (IOException err)
		{
			log.error("Could not read from file " + path, err);
			return new ArrayList<CamDetectionFrame>();
		}
		List<CamDetectionFrame> frames = new ArrayList<CamDetectionFrame>(lines.size());
		for (String line : lines)
		{
			String[] elements = line.split(",");
			List<Float> values = new ArrayList<Float>(elements.length);
			try
			{
				for (String element : elements)
				{
					values.add(Float.parseFloat(element));
				}
			} catch (NumberFormatException err)
			{
				log.error("Could not parse elements: " + elements);
				continue;
			}
			List<CamBall> balls = new ArrayList<CamBall>(1);
			balls.add(new CamBall(0, 0, values.get(1), values.get(2), values.get(3), 0, 0));
			CamDetectionFrame frame = new CamDetectionFrame(0, 0, 0, 0, 0, 0, balls, new ArrayList<CamRobot>(),
					new ArrayList<CamRobot>(), null);
			frames.add(frame);
		}
		return frames;
	}
	
	
	/**
	 */
	// @Test
	public void testDefault()
	{
		List<CamDetectionFrame> frames = readFrames();
		ChipBallWatcher watcher = new ChipBallWatcher(new ChipParams(0, 0));
		for (CamDetectionFrame frame : frames)
		{
			watcher.onNewCamDetectionFrame(frame);
			try
			{
				Thread.sleep(1000 / 60);
			} catch (InterruptedException err)
			{
				err.printStackTrace();
			}
		}
		watcher.run();
	}
}
