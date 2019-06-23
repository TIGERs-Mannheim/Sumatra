/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp;

/**
 * Transmitter observer.
 * 
 * @author AndreR
 * 
 */
public interface ITransmitterTCPObserver
{
	public void onConnectionLost();
}
