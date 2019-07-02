/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source;

import edu.tigers.sumatra.Referee.SSL_Referee;


@FunctionalInterface
public interface IRefereeSourceObserver
{
	/**
	 * New SSL Referee message.
	 * 
	 * @param message
	 */
	void onNewRefereeMessage(SSL_Referee message);
}
