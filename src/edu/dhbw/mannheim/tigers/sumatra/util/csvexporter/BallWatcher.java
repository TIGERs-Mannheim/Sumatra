/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.csvexporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.IMergedCamFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBall;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBot;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.WpBall;


/**
 * Watch chip kicks and gather data to learn for future
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class BallWatcher implements IWorldPredictorObserver, Runnable, IMergedCamFrameObserver
{
	private static final Logger					log						= Logger
																								.getLogger(BallWatcher.class.getName());
	private static final long						TIMEOUT_NS				= (long) 30e9;
	private static final String					DATA_DIR					= "data/ball/";
	private final long								startTime				= SumatraClock.nanoTime();
	private SimpleWorldFrame						currentFrame			= null;
	private CamBall									lastBall					= null;
	private IVector2									initBallPos				= null;
	private List<ExportDataContainer>			data						= new LinkedList<>();
	private final String								fileName;
	private long										time2Stop				= 0;
	private boolean									processing				= false;
	private final List<IBallWatcherObserver>	observers				= new CopyOnWriteArrayList<IBallWatcherObserver>();
	private int											numFramesBallStopped	= 0;
	private boolean									stopAutomatically		= true;
	
	
	/**
	 * @param fileName
	 */
	public BallWatcher(final String fileName)
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
	
	
	private void notifyCustomData(final ExportDataContainer container, final MergedCamDetectionFrame frame)
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
			new Thread(this, "BallWatcher").start();
		}
	}
	
	
	private void stop()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeObserver(this);
			wp.removeMergedFrameObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
		log.debug("Stopped");
	}
	
	
	/**
	 * @return
	 */
	public final boolean start()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(this);
			wp.addMergedFrameObserver(this);
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
	
	
	protected boolean checkIsDone(final CamBall ball)
	{
		if ((time2Stop != 0) && ((SumatraClock.nanoTime() - time2Stop) > 0))
		{
			log.debug("requested timeout reached. Done.");
			return true;
		}
		if ((SumatraClock.nanoTime() - startTime) > TIMEOUT_NS)
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
				&& (currentFrame.getBall().getVel().getLength2() < 0.01f))
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
	public void onNewCameraFrame(final MergedCamDetectionFrame frame)
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
		if (lastBall == null)
		{
			lastBall = curBall;
		}
		
		if (initBallPos == null)
		{
			initBallPos = curBall.getPos().getXYVector();
		}
		TrackedTigerBot nearestBot = getBotNearestToBall(currentFrame, curBall);
		TrackedBall trackedBall = currentFrame.getBall();
		
		ExportDataContainer container = new ExportDataContainer();
		container.setTimestampRecorded(System.nanoTime());
		container.setCurBall(new RawBall(curBall.getTimestamp(), curBall.getCameraId(), curBall.getPos(), frame
				.getFrameNumber()));
		container.setWpBall(new WpBall(trackedBall.getPos3(), trackedBall.getVel3(), trackedBall.getAcc3(), frame
				.getFrameNumber(), curBall.getTimestamp()));
		for (CamBall camBall : frame.getBalls())
		{
			container.getBalls().add(
					new RawBall(camBall.getTimestamp(), camBall.getCameraId(), camBall.getPos(), frame.getFrameNumber()));
		}
		for (CamRobot camRobot : frame.getRobotsBlue())
		{
			container.getRawBots().add(
					new RawBot(camRobot.getTimestamp(), camRobot.getCameraId(), camRobot.getRobotID(), ETeamColor.BLUE, new Vector3(camRobot.getPos(), camRobot
									.getOrientation()), frame.getFrameNumber()));
		}
		for (CamRobot camRobot : frame.getRobotsYellow())
		{
			container.getRawBots().add(
					new RawBot(camRobot.getTimestamp(), camRobot.getCameraId(), camRobot.getRobotID(), ETeamColor.YELLOW, new Vector3(camRobot.getPos(), camRobot
									.getOrientation()), frame.getFrameNumber()));
		}
		for (TrackedTigerBot tBot : currentFrame.getBots().values())
		{
			container.getWpBots().add(
					ExportDataContainer.trackedBot2WpBot(tBot, currentFrame.getId(), frame.getBall().getTimestamp()));
		}
		
		container.getCustomNumberListable().put("nearestBot",
				ExportDataContainer.trackedBot2WpBot(nearestBot, currentFrame.getId(), frame.getBall().getTimestamp()));
		notifyCustomData(container, frame);
		data.add(container);
		
		lastBall = curBall;
		
		if (checkIsFailed(curBall))
		{
			stop();
			data.clear();
			return;
		}
		
		if (checkIsDone(curBall))
		{
			exportData();
		}
	}
	
	
	private TrackedTigerBot getBotNearestToBall(final SimpleWorldFrame frame, final CamBall curBall)
	{
		IVector2 ballPos = curBall.getPos().getXYVector();
		float minDist = Float.MAX_VALUE;
		TrackedTigerBot nearest = TrackedTigerBot.defaultBot(BotID.createBotId(0, ETeamColor.BLUE), new DummyBot());
		for (TrackedTigerBot bot : frame.getBots().values())
		{
			float dist = GeoMath.distancePP(ballPos, bot.getPos());
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
		time2Stop = SumatraClock.nanoTime() + (milliseconds * (long) 1e6);
	}
	
	
	/**
	 * 
	 */
	public final void stopExport()
	{
		stopDelayed(0);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		currentFrame = wfWrapper.getSimpleWorldFrame();
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
		CSVExporter.exportList(folder, "rawBall", data.stream().map(c -> c.getCurBall()));
		CSVExporter.exportList(folder, "wpBall", data.stream().map(c -> c.getWpBall()));
		CSVExporter.exportList(folder, "rawBalls", data.stream().flatMap(c -> c.getBalls().stream()));
		CSVExporter.exportList(folder, "rawBots", data.stream().flatMap(c -> c.getRawBots().stream()));
		CSVExporter.exportList(folder, "wpBots", data.stream().flatMap(c -> c.getWpBots().stream()));
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
	 * @return the lastBall
	 */
	public final CamBall getLastBall()
	{
		return lastBall;
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
}
