/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class DirectRefereeMsgForwarder extends ARefereeMessageSource
{
	/** Constructor. */
	public DirectRefereeMsgForwarder()
	{
		super(ERefereeMessageSource.INTERNAL_FORWARDER);
	}
	
	
	@Override
	public void start()
	{
		// empty
	}
	
	
	@Override
	public void stop()
	{
		// empty
	}
	
	
	@Override
	public void handleControlRequest(final SSL_RefereeRemoteControlRequest request)
	{
		// empty
	}
	
	
	/**
	 * @param msg
	 */
	public void send(final SSL_Referee msg)
	{
		notifyNewRefereeMessage(msg);
	}
}
