package edu.tigers.sumatra.persistence;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Record on a separate thread.
 * <p>
 * All scheduled flushes run on the {@link PersistenceDb#getIoExecutor() db's IO executor}, the same
 * single thread that opens and closes the table streams. This is required because
 * {@link edu.tigers.sumatra.persistence.serializer.MappedDataOutputStream} uses confined arenas.
 */
@Log4j2
public class PersistenceAsyncRecorder
{

	private static final int FLUSH_PERIOD_MS = 10;

	private final PersistenceDb db;
	private final List<IPersistenceRecorder> recorders = new ArrayList<>();
	private ScheduledFuture<?> flushTask;
	private boolean paused = false;


	public PersistenceAsyncRecorder(final PersistenceDb db)
	{
		this.db = db;
	}


	public void add(IPersistenceRecorder recorder)
	{
		recorders.add(recorder);
	}


	public void start()
	{
		log.debug("Starting recording");
		recorders.forEach(IPersistenceRecorder::start);
		flushTask = db.getIoExecutor().scheduleWithFixedDelay(
				this::flush, FLUSH_PERIOD_MS, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
		log.info("Started recording");
	}


	/**
	 * Initiate shutdown without waiting for the IO to drain. The final flush, period log and
	 * table close run on the IO thread; the executor is shut down so it terminates once those
	 * tasks finish. Use {@link #awaitStop()} to block until everything is on disk.
	 */
	public void stop()
	{
		recorders.forEach(IPersistenceRecorder::stop);
		if (flushTask != null)
		{
			flushTask.cancel(false);
			flushTask = null;
		}
		// Final flush + period log run on the IO thread, ahead of the close that is enqueued
		// onto the same single-threaded executor and shuts it down.
		db.getIoExecutor().execute(this::flush);
		db.getIoExecutor().execute(this::printPeriod);
		db.initiateClose();
	}


	public synchronized void pause()
	{
		if (!paused)
		{
			recorders.forEach(IPersistenceRecorder::stop);
			paused = true;
		}
	}


	public synchronized void resume()
	{
		if (paused)
		{
			recorders.forEach(IPersistenceRecorder::start);
			paused = false;
		}
	}


	/**
	 * Block until the database I/O has fully drained.
	 */
	public void awaitStop()
	{
		db.awaitClose(60, TimeUnit.SECONDS);
	}


	public PersistenceDb getDb()
	{
		return db;
	}


	private void flush()
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
}
