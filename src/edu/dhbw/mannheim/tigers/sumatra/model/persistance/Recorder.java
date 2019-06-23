/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;


/**
 * Records {@link RecordFrame}s ({@link AIInfoFrame}s)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Recorder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log					= Logger.getLogger(Recorder.class.getName());
	
	private final int							maxRecordFrames;
	private List<IRecordFrame>				recordFrames		= new LinkedList<IRecordFrame>();
	private final Object						sync					= new Object();
	private final ERecordMode				mode;
	/**  */
	public static final String				DATABASE_PREFIX	= "";
	
	private RecordSaver						recordSaver;
	
	private final BerkeleyLogAppender	logAppender;
	
	private IAIObserver						aiObserver			= new AiObserver();
	
	/**
	 */
	public enum ERecordMode
	{
		/**  */
		LIMITED_BUFFER,
		/**  */
		DATABASE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param recordMode
	 * @param bufferSize
	 */
	public Recorder(final ERecordMode recordMode, final int bufferSize)
	{
		mode = recordMode;
		maxRecordFrames = bufferSize;
		logAppender = new BerkeleyLogAppender();
		Logger.getRootLogger().addAppender(logAppender);
		switch (recordMode)
		{
			case DATABASE:
				recordSaver = new RecordSaver();
				break;
			case LIMITED_BUFFER:
				recordSaver = null;
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Start recording
	 */
	public void start()
	{
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agent.addObserver(aiObserver);
			agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.addObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
	}
	
	
	/**
	 * Stop recording
	 */
	public void stop()
	{
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agent.removeObserver(aiObserver);
			agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.removeObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent modules", err);
		}
		close();
	}
	
	
	/**
	 * Add a frame to the recorder.
	 * It will not be saved immediately, but only if buffer is full or recorder is closed
	 * 
	 * @param recFrame
	 */
	public void addRecordFrame(final RecordFrame recFrame)
	{
		synchronized (sync)
		{
			if (recordFrames.size() > 60)
			{
				// cleanup last frame
				recordFrames.get(recordFrames.size() - 60).cleanUp();
			}
			recordFrames.add(recFrame);
			
			
			if (recordFrames.size() >= maxRecordFrames)
			{
				switch (mode)
				{
					case DATABASE:
						save();
						break;
					case LIMITED_BUFFER:
						long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						long remMem = Runtime.getRuntime().maxMemory() - usedMem;
						long remMemTol = 1024 * 1024 * 700;
						double relMem = usedMem / 1000000.0
								/ (Runtime.getRuntime().maxMemory() / 1000000.0);
						if (((relMem > UserConfig.getMaxRelUsedMemRecord()) || (remMem < remMemTol))
								&& !recordFrames.isEmpty())
						{
							IRecordFrame rf = recordFrames.remove(0);
							rf.cleanUp();
						}
						break;
					default:
						throw new NotImplementedException();
				}
			}
		}
	}
	
	
	private void save()
	{
		synchronized (sync)
		{
			List<IRecordFrame> recFramesToSave = recordFrames;
			recordFrames = new LinkedList<IRecordFrame>();
			for (IRecordFrame rf : recFramesToSave)
			{
				rf.cleanUp();
			}
			recordSaver.save(recFramesToSave, logAppender.flush());
		}
	}
	
	
	/**
	 * Close this Recorder by persisting current record frames.
	 */
	private void close()
	{
		switch (mode)
		{
			case DATABASE:
				save();
				recordSaver.close();
				break;
			case LIMITED_BUFFER:
				break;
			default:
				break;
		}
		Logger.getRootLogger().removeAppender(logAppender);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the recordFrames
	 */
	public final List<IRecordFrame> getRecordFrames()
	{
		return new ArrayList<>(recordFrames);
	}
	
	
	/**
	 * @return
	 */
	public final List<BerkeleyLogEvent> getLogEvents()
	{
		return logAppender.getEvents();
	}
	
	private static class RecordSaver implements Runnable
	{
		private static final Logger		log	= Logger.getLogger(Recorder.RecordSaver.class.getName());
		private List<IRecordFrame>			frames;
		private List<BerkeleyLogEvent>	logEvents;
		private final IRecordPersistence	pers;
		private CountDownLatch				latch	= new CountDownLatch(0);
		
		
		/**
		 */
		public RecordSaver()
		{
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			dt.setTimeZone(TimeZone.getDefault());
			String dbname = dt.format(new Date());
			pers = new RecordBerkeleyPersistence(DATABASE_PREFIX + dbname);
		}
		
		
		@Override
		public void run()
		{
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			pers.saveFrames(frames);
			if (logEvents != null)
			{
				pers.saveLogEvent(logEvents);
				logEvents.clear();
			}
			frames.clear();
			latch.countDown();
		}
		
		
		/**
		 * @param frames
		 */
		public void save(final List<IRecordFrame> frames)
		{
			latch = new CountDownLatch(1);
			this.frames = new ArrayList<>(frames);
			new Thread(this, "RecordSaver").start();
		}
		
		
		/**
		 * @param frames
		 * @param events
		 */
		public void save(final List<IRecordFrame> frames, final List<BerkeleyLogEvent> events)
		{
			logEvents = events;
			save(frames);
		}
		
		
		/**
		 */
		public void close()
		{
			try
			{
				boolean timedOut = !latch.await(10, TimeUnit.SECONDS);
				if (timedOut)
				{
					log.warn("Timed out when closing database");
				} else
				{
					pers.close();
				}
			} catch (InterruptedException err)
			{
				log.error("Interrupted during await", err);
			}
		}
	}
	
	private class AiObserver implements IAIObserver
	{
		@Override
		public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
		{
			RecordFrame recFrame = new RecordFrame(lastAIInfoframe);
			addRecordFrame(recFrame);
		}
		
		
		@Override
		public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
		{
		}
	}
	
	
	/**
	 * @return
	 */
	public ERecordMode getRecordMode()
	{
		return mode;
	}
}
