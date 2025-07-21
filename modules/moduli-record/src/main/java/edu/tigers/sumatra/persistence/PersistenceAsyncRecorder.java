/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.thread.NamedThreadFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Record on a separate thread
 */
@Log4j2
public class PersistenceAsyncRecorder
{

	private static final int TIME_OFFSET = 10;

	private final RecordSaver recordSaver = new RecordSaver();
	private final PersistenceDb db;
	private final List<IPersistenceRecorder> recorders = new ArrayList<>();
	private boolean paused = false;


	/**
	 * Create recorder with given persistence
	 *
	 * @param db
	 */
	public PersistenceAsyncRecorder(final PersistenceDb db)
	{
		this.db = db;
	}


	public void add(IPersistenceRecorder recorder)
	{
		recorders.add(recorder);
	}


	/**
	 * Start recording
	 */
	public void start()
	{
		log.debug("Starting recording");
		recorders.forEach(IPersistenceRecorder::start);
		log.info("Started recording");
	}


	/**
	 * Stop recording
	 */
	public void stop()
	{
		recorders.forEach(IPersistenceRecorder::stop);
		recordSaver.close();
	}


	/**
	 * Pause all recorders by calling their stop method
	 */
	public synchronized void pause()
	{
		if (!paused)
		{
			recorders.forEach(IPersistenceRecorder::stop);
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
			recorders.forEach(IPersistenceRecorder::start);
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


	public PersistenceDb getDb()
	{
		return db;
	}


	private class RecordSaver implements Runnable
	{
		private final ScheduledExecutorService execService;


		RecordSaver()
		{
			execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RecordSaver"));
			execService.scheduleWithFixedDelay(this, TIME_OFFSET, TIME_OFFSET, TimeUnit.MILLISECONDS);
		}


		@Override
		public void run()
		{
			try
			{
				recorders.forEach(IPersistenceRecorder::flush);
			} catch (Exception e)
			{
				log.error("Unexpected exception while flushing", e);
			}
		}


		private void printPeriod()
		{
			Long firstKey = db.getFirstKey();
			Long lastKey = db.getLastKey();
			if (firstKey != null && lastKey != null)
			{
				long duration = (long) ((lastKey - firstKey) / 1e6);
				String period = DurationFormatUtils.formatDuration(duration, "HH:mm:ss", true);
				log.info("Stop recording with a period of {}", period);
			}
		}


		private void close()
		{
			execService.execute(this);
			execService.execute(this::printPeriod);
			execService.execute(db::close);
			execService.shutdown();
			try
			{
				boolean terminated = execService.awaitTermination(10, TimeUnit.SECONDS);
				if (!terminated)
				{
					log.warn("Could not terminate record saver within 10s");
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}
}
