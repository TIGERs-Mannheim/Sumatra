/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.record;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.APersistence;


/**
 * Persists recorded recordFrames
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RecordPersistance extends APersistence implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log					= Logger.getLogger(RecordPersistance.class.getName());
	private static final String			BASE_PATH			= "logs/record/";
	
	private List<IRecordFrame>				recordFrames		= new LinkedList<IRecordFrame>();
	private List<IRecordFrame>				copyRecordFrames	= new LinkedList<IRecordFrame>();
	
	private Thread								saveThread			= new Thread(this);
	private int									counter				= 0;
	private int									MAX_RECORDFRAMES	= 3500;
	
	private Queue<List<IRecordFrame>>	saveQueue			= new ConcurrentLinkedQueue<List<IRecordFrame>>();
	private boolean							saving				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dbname
	 */
	public RecordPersistance(String dbname)
	{
		super(BASE_PATH, dbname);
		saveThread.start();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param recFrame
	 */
	public void addRecordFrame(RecordFrame recFrame)
	{
		synchronized (recordFrames)
		{
			recordFrames.add(recFrame);
			counter++;
			if (counter >= MAX_RECORDFRAMES)
			{
				log.trace("Counter full: Start copy");
				copyRecordFrames = new ArrayList<IRecordFrame>(recordFrames);
				saveQueue.offer(copyRecordFrames);
				recordFrames.clear();
				counter = 0;
				log.trace("End copy");
			}
			
		}
		
		
	}
	
	
	/**
	 * Load stats from database
	 */
	public final void load()
	{
		open();
		try
		{
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			final TypedQuery<IRecordFrame> q = getEm().createQuery("SELECT rec FROM RecordFrame rec", IRecordFrame.class);
			q.setMaxResults(8000);
			try
			{
				recordFrames = q.getResultList();
			} catch (final NoResultException e)
			{
				log.error("database does not contain statistics!");
			}
			t.commit();
		} catch (final PersistenceException e)
		{
			log.error("Could not load DB " + getDbFullPath() + ": " + e.getMessage());
		}
	}
	
	
	/**
	 * Save stats to database
	 */
	public void save()
	{
		log.trace("SAVING: normal start. recordFrames Size: " + recordFrames.size());
		
		saving = true;
		
		if (recordFrames.isEmpty())
		{
			return;
		}
		try
		{
			open();
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			
			
			synchronized (recordFrames)
			{
				for (IRecordFrame frame : recordFrames)
				{
					getEm().persist(frame);
				}
			}
			t.commit();
			close();
			
			log.debug("SAVING normal end");
			
		} catch (PersistenceException err)
		{
			log.error("Could not persist log", err);
		}
	}
	
	
	/**
	 * save a List of IRecordFrame
	 * 
	 * @param recordFrames to save at the database
	 */
	public void save(List<IRecordFrame> recordFrames)
	{
		if (recordFrames == null)
		{
			log.trace("SAVING List start. recordFrames Size: NULL");
			return;
		} else if (recordFrames.isEmpty())
		{
			log.trace("SAVING List start: recordFrames Size: Empty");
			return;
		}
		log.trace("SAVING: List start. recordFrames Size: " + recordFrames.size());
		try
		{
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			
			synchronized (recordFrames)
			{
				for (IRecordFrame frame : recordFrames)
				{
					getEm().persist(frame);
				}
			}
			t.commit();
			
			
			log.trace("SAVING List end");
			
		} catch (PersistenceException err)
		{
			log.error("Could not persist log", err);
		}
		
		
	}
	
	
	/**
	 * @return the recordFrames
	 */
	public final List<IRecordFrame> getRecordFrames()
	{
		return recordFrames;
	}
	
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("SaveRecordFrames");
		open();
		
		
		while (!saving)
		{
			
			while (!saveQueue.isEmpty())
			{
				log.trace("Queue Size: " + saveQueue.size());
				
				log.trace("SaveThread: Start saving");
				save(saveQueue.poll());
				log.trace("SaveThread: End saving");
			}
			
		}
		
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
