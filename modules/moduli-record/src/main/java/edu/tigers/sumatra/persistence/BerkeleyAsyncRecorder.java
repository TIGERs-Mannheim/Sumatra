/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * Record on a separate thread
 */
public class BerkeleyAsyncRecorder
{
	private static final Logger log = Logger.getLogger(BerkeleyAsyncRecorder.class.getName());
	
	private static final int TIME_OFFSET = 100;
	
	private final RecordSaver recordSaver = new RecordSaver();
	private final BerkeleyDb db;
	private final List<IBerkeleyRecorder> recorders = new ArrayList<>();
	private boolean paused = false;
	
	
	/**
	 * Create recorder with given persistence
	 *
	 * @param db
	 */
	public BerkeleyAsyncRecorder(final BerkeleyDb db)
	{
		this.db = db;
	}
	
	
	public void add(IBerkeleyRecorder recorder)
	{
		recorders.add(recorder);
	}
	
	
	/**
	 * Start recording
	 */
	public void start()
	{
		log.info("Start recording");
		db.open();
		recorders.forEach(IBerkeleyRecorder::start);
	}
	
	
	/**
	 * Stop recording
	 */
	public void stop()
	{
		recorders.forEach(IBerkeleyRecorder::stop);
		recordSaver.close();
	}
	
	
	/**
	 * Pause all recorders by calling their stop method
	 */
	public synchronized void pause()
	{
		if (!paused)
		{
			recorders.forEach(IBerkeleyRecorder::stop);
			paused = true;
		}
	}
	
	
	/**
	 * Resume all recorders after they have been paused
	 */
	public synchronized void resume()
	{
		if (paused)
		{
			recorders.forEach(IBerkeleyRecorder::start);
			paused = false;
		}
	}
	
	
	/**
	 * Block until database is stopped.
	 */
	public void awaitStop()
	{
		try
		{
			Validate.isTrue(recordSaver.execService.awaitTermination(60, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	public BerkeleyDb getDb()
	{
		return db;
	}
	
	
	private class RecordSaver implements Runnable
	{
		private final ScheduledExecutorService execService;
		
		
		/**
		 */
		RecordSaver()
		{
			execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RecordSaver"));
			execService.scheduleWithFixedDelay(this, TIME_OFFSET, TIME_OFFSET, TimeUnit.MILLISECONDS);
		}
		
		
		@Override
		public void run()
		{
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			recorders.forEach(IBerkeleyRecorder::flush);
		}
		
		
		private void printPeriod()
		{
			Long firstKey = db.getFirstKey();
			Long lastKey = db.getLastKey();
			if (firstKey != null && lastKey != null)
			{
				long duration = (long) ((lastKey - firstKey) / 1e6);
				String period = DurationFormatUtils.formatDuration(duration, "HH:mm:ss", true);
				log.info("Stop recording with a period of " + period);
			}
		}
		
		
		private void close()
		{
			execService.execute(this);
			execService.execute(this::printPeriod);
			execService.execute(db::close);
			execService.shutdown();
		}
	}
}
