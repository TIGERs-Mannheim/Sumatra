/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Record on a separate thread
 *
 * @param <T> persistence type
 * @author nicolai.ommer
 */
public class ABerkeleyAsyncRecorder<T extends ABerkeleyPersistence>
{
	private static final Logger log = Logger.getLogger(ABerkeleyAsyncRecorder.class.getName());
	
	private static final int TIME_OFFSET = 1000;
	private boolean stopping = false;
	private final RecordSaver recordSaver = new RecordSaver();
	
	private final T pers;
	
	
	/**
	 * Create recorder with given persistence
	 * 
	 * @param pers to be used
	 */
	ABerkeleyAsyncRecorder(T pers)
	{
		this.pers = pers;
	}
	
	
	/**
	 * @return the db path
	 */
	public String getDbPath()
	{
		return pers.getDbPath();
	}
	
	
	/**
	 * Start recording
	 */
	public void start()
	{
		pers.open();
	}
	
	
	/**
	 * Stop recording
	 */
	public void stop()
	{
		recordSaver.close();
	}
	
	
	protected void cleanup()
	{
		// nothing to do here
	}
	
	
	protected void persist(T pers)
	{
		// nothing to do here
	}
	
	
	/**
	 * Delete the database
	 * 
	 * @throws IOException
	 */
	public void delete() throws IOException
	{
		pers.delete();
	}
	
	
	/**
	 * Compress the database
	 * 
	 * @throws IOException
	 */
	public void compress() throws IOException
	{
		pers.getEnv().compress();
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
	
	
	/**
	 * @return true if the recorder is going to stop or already stopped.
	 */
	protected boolean isStopping()
	{
		return stopping;
	}
	
	
	/**
	 * @return the timeoffset between syncs with database
	 */
	protected long getTimeOffset()
	{
		return TIME_OFFSET;
	}
	
	
	/**
	 * @param compressAutomatically compress database automatically after it was closed
	 */
	public void setCompressAutomatically(boolean compressAutomatically)
	{
		pers.setCompressOnClose(compressAutomatically);
	}
	
	private class RecordSaver implements Runnable
	{
		private final ScheduledExecutorService execService;
		
		
		/**
		 */
		RecordSaver()
		{
			execService = Executors.newSingleThreadScheduledExecutor();
			execService.scheduleWithFixedDelay(this, TIME_OFFSET, TIME_OFFSET, TimeUnit.MILLISECONDS);
		}
		
		
		@Override
		public void run()
		{
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			Thread.currentThread().setName("RecordSaver");
			
			persist(pers);
		}
		
		
		private void close()
		{
			stopping = true;
			execService.execute(this);
			execService.execute(() -> {
				cleanup();
				pers.close();
			});
			execService.shutdown();
		}
	}
}
