/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source;

/**
 * Possible referee message sources
 */
public enum ERefereeMessageSource
{
	/** Game-controller on network (either from inside Sumatra or from extern) */
	NETWORK,
	
	/** Information via SSL gamelog file */
	INTERNAL_FORWARDER,
	
	/** Direct TCP connection to game-controller */
	CI,
}
