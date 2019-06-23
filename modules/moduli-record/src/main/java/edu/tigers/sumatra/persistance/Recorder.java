/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Recorder
{
	private static final Logger							log						= Logger.getLogger(Recorder.class.getName());
	
	private final Map<Long, RecordFrame>				recordFrames			= new ConcurrentSkipListMap<>();
	private final Queue<ExtendedCamDetectionFrame>	camFrames				= new ConcurrentLinkedQueue<>();
	private final Queue<BerkeleyLogEvent>				events					= new ConcurrentLinkedQueue<>();
	private long												latestValidTimestamp	= 0;
	/**  */
	public static final String								DATABASE_PREFIX		= "";
	
	private final RecordSaver								recordSaver				= new RecordSaver();
	
	private final BerkeleyLogAppender					logAppender;
	
	private final AiObserver								aiObserver				= new AiObserver();
	private final CamFrameObserver						camObserver				= new CamFrameObserver();
	
	
	/**
	 */
	public Recorder()
	{
		logAppender = new BerkeleyLogAppender();
		logAppender.setThreshold(Level.ALL);
	}
	
	
	/**
	 * Start recording
	 */
	public void start()
	{
		Logger.getRootLogger().addAppender(logAppender);
		
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agent.addVisObserver(aiObserver);
			agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.addVisObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(camObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp module", err);
		}
	}
	
	
	/**
	 * Stop recording
	 */
	public void stop()
	{
		Logger.getRootLogger().removeAppender(logAppender);
		
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agent.removeVisObserver(aiObserver);
			agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.removeVisObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(camObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp module", err);
		}
		recordSaver.close();
	}
	
	
	/**
	 * Add a frame to the recorder.
	 * It will not be saved immediately, but only if buffer is full or recorder is closed
	 * 
	 * @param visFrame
	 */
	public void addRecordFrame(final VisualizationFrame visFrame)
	{
		if (SumatraModel.getInstance().isProductive())
		{
			EGameStateNeutral gameState = visFrame.getWorldFrameWrapper().getGameState();
			switch (gameState)
			{
				case BREAK:
				case HALTED:
				case TIMEOUT_BLUE:
				case TIMEOUT_YELLOW:
					return;
				default:
			}
		}
		
		// only accept frames that are not too old
		if (visFrame.getTimestamp() > latestValidTimestamp)
		{
			// copy frame, before modification
			VisualizationFrame frame = new VisualizationFrame(visFrame);
			// remove shape layers that should not be persisted
			frame.getShapes().removeNonPersistent();
			// create new record frame if absent
			RecordFrame recFrame = recordFrames.get(frame.getTimestamp());
			if (recFrame == null)
			{
				frame.getWorldFrameWrapper().getShapeMap().removeNonPersistent();
				recFrame = new RecordFrame(visFrame.getWorldFrameWrapper());
				recordFrames.put(frame.getTimestamp(), recFrame);
			}
			// add new frame
			recFrame.addVisFrame(frame);
		}
	}
	
	
	private class RecordSaver implements Runnable
	{
		private static final int					TIME_OFFSET	= 1000;
		private final IRecordPersistence			pers;
		private final ScheduledExecutorService	execService;
		private boolean								stopping		= false;
		
		
		/**
		 */
		public RecordSaver()
		{
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			dt.setTimeZone(TimeZone.getDefault());
			String dbname = dt.format(new Date());
			pers = new RecordBerkeleyPersistence(
					RecordBerkeleyPersistence.getDefaultBasePath() + "/" + DATABASE_PREFIX + dbname, false);
			
			execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"RecordSaver"));
			execService.scheduleWithFixedDelay(this, TIME_OFFSET, TIME_OFFSET, TimeUnit.MILLISECONDS);
		}
		
		
		@Override
		public void run()
		{
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			long curTimestamp = System.currentTimeMillis();
			List<RecordFrame> toSave = new ArrayList<>();
			for (Map.Entry<Long, RecordFrame> entry : recordFrames.entrySet())
			{
				long timestamp = entry.getValue().getTimestampMs();
				if (stopping || ((timestamp + TIME_OFFSET) < curTimestamp))
				{
					toSave.add(entry.getValue());
					latestValidTimestamp = entry.getKey();
					// we use a conc. map, so we can remove directly
					recordFrames.remove(entry.getKey());
				} else
				{
					break;
				}
			}
			
			pers.saveRecordFrames(toSave);
			
			List<RecordCamFrame> camFrameToSave = new ArrayList<>(camFrames.size());
			ExtendedCamDetectionFrame camFrame = camFrames.poll();
			while (camFrame != null)
			{
				camFrameToSave.add(new RecordCamFrame(camFrame));
				camFrame = camFrames.poll();
			}
			pers.saveCamFrames(camFrameToSave);
			
			List<BerkeleyLogEvent> eventsToSave = new ArrayList<>(events.size());
			BerkeleyLogEvent event = events.poll();
			while (event != null)
			{
				eventsToSave.add(event);
				event = events.poll();
			}
			
			pers.saveLogEvent(eventsToSave);
		}
		
		
		private void cleanUp()
		{
			if (!recordFrames.isEmpty())
			{
				log.error("Record frames left!");
			}
			if (!events.isEmpty())
			{
				log.error("Log events left!");
			}
			
			pers.close();
		}
		
		
		/**
		 */
		public void close()
		{
			stopping = true;
			execService.execute(this);
			execService.execute(() -> cleanUp());
			execService.shutdown();
		}
	}
	
	private class AiObserver implements IVisualizationFrameObserver
	{
		@Override
		public void onNewVisualizationFrame(final VisualizationFrame frame)
		{
			addRecordFrame(frame);
		}
	}
	
	private class CamFrameObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
		{
			camFrames.add(frame);
		}
	}
	
	
	private class BerkeleyLogAppender extends WriterAppender
	{
		@Override
		public void append(final LoggingEvent event)
		{
			events.add(new BerkeleyLogEvent(event));
		}
	}
}
