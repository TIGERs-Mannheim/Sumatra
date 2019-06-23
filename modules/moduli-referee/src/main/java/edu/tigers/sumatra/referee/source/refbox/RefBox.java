/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.source.refbox;

import static edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
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
	
	private Map<ETeamColor, Integer> keeperIds = new EnumMap<>(ETeamColor.class);
	
	static
	{
		ConfigRegistration.registerClass("user", RefBox.class);
	}
	
	
	/** Constructor */
	public RefBox()
	{
		super(ERefereeMessageSource.INTERNAL_REFBOX);
		keeperIds.put(ETeamColor.YELLOW, 0);
		keeperIds.put(ETeamColor.BLUE, 0);
	}
	
	
	@Override
	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, "RefBox");
			thread.start();
		} else
		{
			log.warn("Refbox already started!");
		}
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
			
			// update keeper ids
			engine.setKeeperId(ETeamColor.YELLOW, keeperIds.get(ETeamColor.YELLOW));
			engine.setKeeperId(ETeamColor.BLUE, keeperIds.get(ETeamColor.BLUE));
			
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
	
	
	@Override
	public void updateKeeperId(BotID keeperId)
	{
		keeperIds.put(keeperId.getTeamColor(), keeperId.getNumber());
	}
}
