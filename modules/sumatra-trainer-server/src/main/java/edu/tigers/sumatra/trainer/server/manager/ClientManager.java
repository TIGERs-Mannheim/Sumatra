/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

public class ClientManager
{
	private long idCounter = 0;
	
	
	ClientManager()
	{
	}
	
	
	public long newClient()
	{
		return idCounter++;
	}
}
