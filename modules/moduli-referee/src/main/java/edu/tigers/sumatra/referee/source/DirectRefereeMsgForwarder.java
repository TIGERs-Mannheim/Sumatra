/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.source;


import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;

public class DirectRefereeMsgForwarder extends ARefereeMessageSource
{
	public DirectRefereeMsgForwarder()
	{
		super(ERefereeMessageSource.INTERNAL_FORWARDER);
	}


	public void send(final SslGcRefereeMessage.Referee msg)
	{
		if (msg != null)
		{
			notifyNewRefereeMessage(msg);
		}
	}
}
