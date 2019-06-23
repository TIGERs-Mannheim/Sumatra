/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.ExportDataContainer.FrameInfo;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBall;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBot;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Watch chip kicks and gather data to learn for future
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class VisionWatcher implements IWorldFrameObserver, Runnable
{
	private static final Logger					log						= Logger
			.getLogger(VisionWatcher.class.getName());
	private static final String					DATA_DIR					= "data/vision/";
	private final long								startTime				= System.nanoTime();
	private SimpleWorldFrame						currentFrame			= null;
	private TrackedBall								lastBall					= null;
	private IVector2									initBallPos				= null;
	private final List<ExportDataContainer>	data						= new ArrayList<>();
	private final String								fileName;
	private long										time2Stop				= 0;
	private boolean									processing				= false;
	private final List<IBallWatcherObserver>	observers				= new CopyOnWriteArrayList<IBallWatcherObserver>();
	private int											numFramesBallStopped	= 0;
	private boolean									stopAutomatically		= true;
	private double										timeout					= 30;
	
	
	/**
	 * @param fileName
	 */
	public VisionWatcher(final String fileName)
	{
		String moduli = SumatraModel.getInstance().getCurrentModuliConfig().split("\\.")[0];
		this.fileName = moduli + "/" + fileName;
	}
	
	
	/**
	 * @param observer
	 */
	public final void addObserver(final IBallWatcherObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public final void removeObserver(final IBallWatcherObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyExport(final Map<String, Object> jsonMapping)
	{
		for (IBallWatcherObserver observer : observers)
		{
			observer.beforeExport(jsonMapping);
		}
	}
	
	
	private void notifyCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
	{
		for (IBallWatcherObserver observer : observers)
		{
			observer.onAddCustomData(container, frame);
		}
	}
	
	
	private void notifyPostProcessing(final String filename)
	{
		for (IBallWatcherObserver observer : observers)
		{
			observer.postProcessing(filename);
		}
	}
	
	
	private void exportData()
	{
		stop();
		if (!processing)
		{
			processing = true;
			new Thread(this, "VisionWatcher").start();
		}
	}
	
	
	/**
	 * 
	 */
	protected void stop()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
		log.debug("Stopped");
	}
	
	
	/**
	 * @return
	 */
	public boolean start()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
			return false;
		}
		log.debug("Started.");
		return true;
	}
	
	
	protected boolean checkIsFailed(final CamBall ball)
	{
		return false;
	}
	
	
	protected boolean checkIsDone(final TrackedBall ball)
	{
		TrackedBall lastBall = this.lastBall;
		if (lastBall == null)
		{
			lastBall = ball;
		}
		
		this.lastBall = ball;
		
		if (initBallPos == null)
		{
			initBallPos = ball.getPos().getXYVector();
		}
		
		if ((time2Stop != 0) && ((System.nanoTime() - time2Stop) > 0))
		{
			log.debug("requested timeout reached. Done.");
			return true;
		}
		if (((System.nanoTime() - startTime) / 1e9) > timeout)
		{
			log.debug("Ball watcher timed out");
			return true;
		}
		if (!stopAutomatically)
		{
			return false;
		}
		if (lastBall.getPos().getXYVector().equals(ball.getPos().getXYVector(), 5)
				&& (!initBallPos.equals(ball.getPos().getXYVector(), 50))
				&& (currentFrame != null)
				&& (currentFrame.getBall().getVel().getLength2() < 0.01))
		{
			numFramesBallStopped++;
			if (numFramesBallStopped > 10)
			{
				log.debug("ball stopped, data size: " + data.size());
				return true;
			}
		} else
		{
			numFramesBallStopped = 0;
		}
		return false;
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		// do not collect more data if we are already processing the data!
		if (processing)
		{
			return;
		}
		
		if (currentFrame == null)
		{
			return;
		}
		
		CamBall curBall = frame.getBall();
		
		ExportDataContainer container = new ExportDataContainer();
		container.setFrameInfo(new FrameInfo(frame.getFrameNumber(), frame.getCameraId(),
				frame.gettCapture(), frame.gettSent(), System.nanoTime()));
		container.setCurBall(curBall);
		
		for (CamBall camBall : frame.getBalls())
		{
			container.getBalls().add(camBall);
		}
		for (CamRobot camRobot : frame.getRobotsBlue())
		{
			container.getRawBots().add(camRobot);
		}
		for (CamRobot camRobot : frame.getRobotsYellow())
		{
			container.getRawBots().add(camRobot);
		}
		
		// ### WP
		
		TrackedBall trackedBall = currentFrame.getBall();
		container.setWpBall(new WpBall(trackedBall.getPos3(), trackedBall.getVel3(), trackedBall.getAcc3(), currentFrame
				.getId(), currentFrame.getTimestamp(), trackedBall.getConfidence()));
		for (ITrackedBot tBot : currentFrame.getBots().values())
		{
			container.getWpBots().add(
					ExportDataContainer.trackedBot2WpBot(tBot, currentFrame.getId(), currentFrame.getTimestamp()));
			
			if (tBot.getBot().getSensoryPos().isPresent())
			{
				WpBot isBot = ExportDataContainer.trackedBot2WpBot(tBot, currentFrame.getId(), currentFrame.getTimestamp());
				isBot.setPos(tBot.getBot().getSensoryPos().get());
				if (tBot.getBot().getSensoryVel().isPresent())
				{
					isBot.setVel(tBot.getBot().getSensoryVel().get());
				} else
				{
					isBot.setVel(AVector3.ZERO_VECTOR);
				}
				container.getIsBots().add(isBot);
			}
		}
		
		ITrackedBot nearestBot = getBotNearestToBall(currentFrame, trackedBall);
		container.getCustomNumberListable().put("nearestBot",
				ExportDataContainer.trackedBot2WpBot(nearestBot, currentFrame.getId(), currentFrame.getTimestamp()));
		
		
		notifyCustomData(container, frame);
		data.add(container);
		
		if (checkIsFailed(curBall))
		{
			stop();
			data.clear();
			return;
		}
		
		if (checkIsDone(trackedBall))
		{
			exportData();
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		currentFrame = wfWrapper.getSimpleWorldFrame();
	}
	
	
	private ITrackedBot getBotNearestToBall(final SimpleWorldFrame frame, final TrackedBall curBall)
	{
		IVector2 ballPos = curBall.getPos().getXYVector();
		double minDist = Double.MAX_VALUE;
		ITrackedBot nearest = new TrackedBot(frame.getTimestamp(), BotID.get());
		for (ITrackedBot bot : frame.getBots().values())
		{
			double dist = GeoMath.distancePP(ballPos, bot.getPos());
			if (dist < minDist)
			{
				nearest = bot;
				minDist = dist;
			}
		}
		return nearest;
	}
	
	
	/**
	 * Stop after given delay
	 * 
	 * @param milliseconds
	 */
	public final void stopDelayed(final long milliseconds)
	{
		time2Stop = System.nanoTime() + (milliseconds * (long) 1e6);
	}
	
	
	/**
	 * 
	 */
	public final void stopExport()
	{
		stopDelayed(0);
	}
	
	
	@Override
	public final void run()
	{
		Map<String, Object> jsonMapping = new HashMap<>();
		// jsonMapping.put("data", data.stream().map(e -> e.toJSON()).collect(Collectors.toList()));
		
		exportCsvFiles(DATA_DIR + fileName);
		
		jsonMapping.put("timestamp", System.currentTimeMillis());
		jsonMapping.put("description", "no description available");
		jsonMapping.put("numSamples", data.size());
		notifyExport(jsonMapping);
		data.clear();
		
		JSONObject jsonObj = new JSONObject(jsonMapping);
		String fullFileName = DATA_DIR + fileName + "/info.json";
		try
		{
			Files.write(Paths.get(fullFileName), jsonObj.toJSONString().getBytes());
		} catch (IOException err)
		{
			log.error("Could not write file!", err);
		}
		
		notifyPostProcessing(fullFileName);
	}
	
	
	private void exportCsvFiles(final String folder)
	{
		File dir = new File(folder);
		if (dir.exists())
		{
			log.error("Target folder already exists: " + folder);
			return;
		}
		if (!dir.mkdirs())
		{
			log.error("Can not create target folder: " + folder);
			return;
		}
		CSVExporter.exportList(folder, "frameInfo", data.stream().map(c -> c.getFrameInfo()));
		CSVExporter.exportList(folder, "rawBall", data.stream().map(c -> c.getCurBall()));
		CSVExporter.exportList(folder, "wpBall", data.stream().map(c -> c.getWpBall()));
		CSVExporter.exportList(folder, "rawBalls", data.stream().flatMap(c -> c.getBalls().stream()));
		CSVExporter.exportList(folder, "rawBots", data.stream().flatMap(c -> c.getRawBots().stream()));
		CSVExporter.exportList(folder, "wpBots", data.stream().flatMap(c -> c.getWpBots().stream()));
		CSVExporter.exportList(folder, "isBots", data.stream().flatMap(c -> c.getIsBots().stream()));
		CSVExporter.exportList(folder, "skillBots", data.stream().flatMap(c -> c.getSkillBots().stream()));
		for (String key : data.get(0).getCustomNumberListable().keySet())
		{
			CSVExporter.exportList(folder, key, data.stream().map(c -> c.getCustomNumberListable().get(key)));
		}
	}
	
	
	/**
	 * @return the initBallPos
	 */
	public final IVector2 getInitBallPos()
	{
		return initBallPos;
	}
	
	
	/**
	 * @return the fileName
	 */
	public final String getFileName()
	{
		return fileName;
	}
	
	
	/**
	 * @return
	 */
	public int getDataSize()
	{
		return data.size();
	}
	
	
	/**
	 * @return the stopAutomatically
	 */
	public final boolean isStopAutomatically()
	{
		return stopAutomatically;
	}
	
	
	/**
	 * @param stopAutomatically the stopAutomatically to set
	 */
	public final void setStopAutomatically(final boolean stopAutomatically)
	{
		this.stopAutomatically = stopAutomatically;
	}
	
	
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(final double timeout)
	{
		this.timeout = timeout;
	}
}
