/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source;

/**
 * @author AndreR <andre@ryll.cc>
 */
public enum ERefereeMessageSource
{
	/** Official refbox on network */
	NETWORK,
	
	/** Internal refbox */
	INTERNAL_REFBOX,
	
	/** Information via SSL gamelog file */
	INTERNAL_FORWARDER,
}
