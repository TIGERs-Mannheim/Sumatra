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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiverObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP.EConnectionState;

/**
 * Transceiver observer interface.
 * 
 * @author AndreR
 * 
 */
public interface ITransceiverTCPObserver extends ITransceiverObserver
{
	/**
	 * Called after the connection state of the transceiver changed.
	 * 
	 * @param state New connection state.
	 */
	public void onConnectionChanged(EConnectionState state);
}
