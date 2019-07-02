/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source;

import edu.tigers.sumatra.Referee.SSL_Referee;


public class DirectRefereeMsgForwarder extends ARefereeMessageSource
{
	public DirectRefereeMsgForwarder()
	{
		super(ERefereeMessageSource.INTERNAL_FORWARDER);
	}
	
	
	public void send(final SSL_Referee msg)
	{
		if (msg != null)
		{
			notifyNewRefereeMessage(msg);
		}
	}
}
