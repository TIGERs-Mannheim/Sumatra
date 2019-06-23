/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.BerkeleyLogAppender;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.BerkeleyLogEvent;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.IRecordPersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;


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
	
	private final int							maxRecordFrames;
	private List<IRecordFrame>				recordFrames		= new LinkedList<IRecordFrame>();
	private final Object						sync					= new Object();
	private final ERecordMode				mode;
	/**  */
	public static final String				DATABASE_PREFIX	= "record_";
	
	private RecordSaver						recordSaver;
	
	private final BerkeleyLogAppender	logAppender;
	
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
			default:
				break;
		
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
						IRecordFrame rf = recordFrames.remove(0);
						rf.cleanUp();
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
	public void close()
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
		// this is rather annoying and misleading (might not really by successful) :)
		// JOptionPane.showMessageDialog(null, "Saving successful", "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the recordFrames
	 */
	public final List<IRecordFrame> getRecordFrames()
	{
		return recordFrames;
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
			this.frames = frames;
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
}
