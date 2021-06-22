/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.network;

/**
 * Observer for network receivers.
 */
public interface IReceiverObserver
{
	/**
	 * The interface timed out.
	 */
	void onSocketTimedOut();
}
