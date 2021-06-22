/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.source;


import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;

@FunctionalInterface
public interface IRefereeSourceObserver
{
	/**
	 * New SSL Referee message.
	 *
	 * @param message
	 */
	void onNewRefereeMessage(SslGcRefereeMessage.Referee message);
}
