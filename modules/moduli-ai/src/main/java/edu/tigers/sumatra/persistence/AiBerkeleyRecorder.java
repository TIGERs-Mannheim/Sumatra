/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiBerkeleyRecorder extends ABerkeleyAsyncRecorder<AiBerkeleyPersistence>
{
	private static final Logger log = Logger
			.getLogger(AiBerkeleyRecorder.class.getName());
	
	private final Map<Long, RecordFrame> recordFrames = new ConcurrentSkipListMap<>();
	private final Queue<ExtendedCamDetectionFrame> camFrames = new ConcurrentLinkedQueue<>();
	private final Queue<BerkeleyLogEvent> events = new ConcurrentLinkedQueue<>();
	private long latestValidTimestamp = 0;
	
	
	private final BerkeleyLogAppender logAppender;
	
	private final AiObserver aiObserver = new AiObserver();
	private final CamFrameObserver camObserver = new CamFrameObserver();
	
	
	/**
	 * @param basePath where database should be stored
	 */
	public AiBerkeleyRecorder(final String basePath)
	{
		this(basePath, null);
	}
	
	
	/**
	 * @param basePath where database should be stored
	 * @param dbNamePostfix a postfix for the database name
	 */
	public AiBerkeleyRecorder(final String basePath, final String dbNamePostfix)
	{
		super(createPersistence(basePath, dbNamePostfix));
		logAppender = new BerkeleyLogAppender();
		logAppender.setThreshold(Level.ALL);
	}
	
	
	private static AiBerkeleyPersistence createPersistence(final String basePath, final String dbNamePostfix)
	{
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		dt.setTimeZone(TimeZone.getDefault());
		String dbname = dt.format(new Date());
		
		if (StringUtils.isNotBlank(dbNamePostfix))
		{
			dbname += "_" + dbNamePostfix;
		}
		
		return new AiBerkeleyPersistence(basePath + "/" + dbname);
	}
	
	
	@Override
	public void start()
	{
		super.start();
		Logger.getRootLogger().addAppender(logAppender);
		
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.addVisObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(camObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp module", err);
		}
	}
	
	
	@Override
	public void stop()
	{
		Logger.getRootLogger().removeAppender(logAppender);
		
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.removeVisObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeObserver(camObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp module", err);
		}
		super.stop();
	}
	
	
	@Override
	protected void cleanup()
	{
		if (!recordFrames.isEmpty())
		{
			log.error("Record frames left!");
		}
		if (!events.isEmpty())
		{
			log.error("Log events left!");
		}
	}
	
	
	@Override
	protected void persist(final AiBerkeleyPersistence pers)
	{
		long curTimestamp = System.currentTimeMillis();
		List<RecordFrame> toSave = new ArrayList<>();
		for (Map.Entry<Long, RecordFrame> entry : recordFrames.entrySet())
		{
			long timestamp = entry.getValue().getTimestampMs();
			if (isStopping() || ((timestamp + getTimeOffset()) < curTimestamp))
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
		
		if (toSave.isEmpty())
		{
			/*
			 * there is some weird bug that causes the thread to hang sometimes if pers.saveRecordFrames(toSave)
			 * is called with an empty list. Returning here early seams to fix this issue.
			 */
			return;
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
	
	
	private class AiObserver implements IVisualizationFrameObserver
	{
		@Override
		public void onNewVisualizationFrame(final VisualizationFrame frame)
		{
			addRecordFrame(frame);
		}
		
		
		/**
		 * Add a frame to the recorder.
		 * It will not be saved immediately, but only if buffer is full or recorder is closed
		 *
		 * @param visFrame to be added
		 */
		private void addRecordFrame(final VisualizationFrame visFrame)
		{
			if (SumatraModel.getInstance().isProductive() && visFrame.getWorldFrameWrapper().getGameState().isIdleGame())
			{
				return;
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
