/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.source.refbox;

import static edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.refbox.time.ITimeProvider;


/**
 * Internal RefBox implementation.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class RefBox extends ARefereeMessageSource implements Runnable
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RefBox.class.getName());
	
	private RefBoxEngine engine = new RefBoxEngine();
	private List<SSL_RefereeRemoteControlRequest> outstandingRequests = new CopyOnWriteArrayList<>();
	private Thread thread;
	
	
	/** Constructor */
	public RefBox()
	{
		super(ERefereeMessageSource.INTERNAL_REFBOX);
	}
	
	
	@Override
	public void start()
	{
		thread = new Thread(this, "RefBox");
		thread.start();
	}
	
	
	@Override
	public void stop()
	{
		if (thread != null)
		{
			thread.interrupt();
			thread = null;
		}
	}
	
	
	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			// ...zzzZZZzzzz...
			try
			{
				Thread.sleep(20);
			} catch (InterruptedException e1)
			{
				Thread.currentThread().interrupt();
				return;
			}
			
			// process all control requests
			while (!outstandingRequests.isEmpty())
			{
				Outcome outcome = engine.handleControlRequest(outstandingRequests.remove(0));
				if (outcome != Outcome.OK)
				{
					log.warn("Invalid outcome: " + outcome);
				}
			}
			
			// spin the engine
			SSL_Referee msg = engine.spin();
			
			// send referee message
			notifyNewRefereeMessage(msg);
		}
	}
	
	
	@Override
	public void handleControlRequest(final SSL_RefereeRemoteControlRequest request)
	{
		outstandingRequests.add(request);
	}
	
	
	/**
	 * Set new time provider.
	 * 
	 * @param provider
	 */
	public void setTimeProvider(final ITimeProvider provider)
	{
		engine.setTimeProvider(provider);
	}
}
