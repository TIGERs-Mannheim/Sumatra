/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.10.2010
 * Author(s): Yakisoba
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;


/**
 * - Class provides a synchronized buffer which handles the possible merge of multiple camFrames
 * - merges when one of the other cameras sent a frame
 * - if no frames from other cameras available, we pack a worldframe with all existing processed frames
 * ~~~~~~~~~
 * - get a Frame with take()
 * - check if you continue process frames with merge()
 * - afterwards you need to clear() this buffer and send woldframes to your observer-listeners
 * 
 * @author Maren
 * 
 */
public class SyncedCamFrameBuffer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final int							CAMS;
	private final long						tMinDelay;
	
	// -- saves the information from all cams
	private final CamDetectionFrame[]	cam;
	
	// -- saves state of each cam
	// -- 0: no frame recieved yet
	// -- 1: queued frame
	// -- 2: processed frame
	// -- 3: processed frame with new info (remember it for next process round)
	enum Status
	{
		NOTSEEN,
		QUEUED,
		PROCESSED,
		PROCESSAGAIN;
	}
	
	private final Status[]						state;
	
	// -- saves order of cam recieves
	private final BlockingDeque<Integer>	idQueue;
	
	
	// --------------------------------------------------------------------------
	// --- constructor ----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param cams
	 * @param tMinDelay
	 */
	public SyncedCamFrameBuffer(int cams, long tMinDelay)
	{
		CAMS = cams;
		this.tMinDelay = tMinDelay;
		
		cam = new CamDetectionFrame[CAMS];
		state = new Status[CAMS];
		idQueue = new LinkedBlockingDeque<Integer>();
		
		reset();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Reset cam buffer to initial state
	 */
	public final void reset()
	{
		for (int i = 0; i < CAMS; i++)
		{
			cam[i] = null;
		}
		
		idQueue.clear();
		
		for (int i = 0; i < CAMS; i++)
		{
			state[i] = Status.NOTSEEN;
		}
	}
	
	
	/**
	 * @param camFrame
	 */
	public void put(CamDetectionFrame camFrame)
	{
		final int camId = camFrame.cameraId;
		
		// -- check if frame is too old and ignore it
		// -- extension by Peter: new frame have to be at least tMinDelay ns newer than the old one
		if ((cam[camId] != null) && (cam[camId].tCapture >= (camFrame.tCapture - tMinDelay)))
		{
			return;
		}
		
		cam[camId] = camFrame;
		
		// -- check if we saw the frame and already processed it
		if ((state[camId] == Status.PROCESSED) || (state[camId] == Status.PROCESSAGAIN))
		{
			state[camId] = Status.PROCESSAGAIN;
			return;
		}
		
		// -- conditions for not seen frames yet
		
		// -- if we get a newer frame, we put it to the end of queue
		// -- and remove doubled entries
		idQueue.remove(camId);
		idQueue.add(camId);
		
		// -- change state to "seen and not processed yet"
		state[camId] = Status.QUEUED;
	}
	
	
	/**
	 * @param timeout
	 * @param unit
	 * @return {@link CamDetectionFrame} or null if timeout occurred
	 * @throws InterruptedException
	 */
	public CamDetectionFrame take(int timeout, TimeUnit unit) throws InterruptedException
	{
		// -- we take the frame, remove it from the queue and set state to "processed"
		final Integer tmpID = idQueue.poll(timeout, unit);
		if (tmpID == null)
		{
			return null;
		}
		state[tmpID] = Status.PROCESSED;
		return cam[tmpID];
	}
	
	
	/**
	 * @return
	 */
	public boolean merge()
	{
		// -- if we have at least one "seen but not processed" frame
		// -- we want to merge
		for (int i = 0; i < CAMS; i++)
		{
			if (state[i] == Status.QUEUED)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 */
	public void clear()
	{
		// -- after we merged all possible frames we clear the queue
		// -- and set the state to "not seen yet"
		idQueue.clear();
		for (int i = 0; i < CAMS; i++)
		{
			// -- but if we have a frame we processed and seen it again
			// -- we want it to be processed the next round
			if ((state[i] == Status.PROCESSAGAIN) || (state[i] == Status.QUEUED))
			{
				state[i] = Status.QUEUED;
				idQueue.add(i);
			} else
			{
				state[i] = Status.NOTSEEN;
			}
		}// end for
	}
}
