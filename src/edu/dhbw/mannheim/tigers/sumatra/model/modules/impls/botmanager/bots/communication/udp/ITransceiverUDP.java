/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.03.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp;

import java.net.NetworkInterface;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;

/**
 * Transceiver interface.
 * 
 * @author AndreR
 * 
 */
public interface ITransceiverUDP extends ITransceiver
{
	void 			addObserver(ITransceiverUDPObserver o);
	void 			removeObserver(ITransceiverUDPObserver o);
	
	void			open();
	void 			open(String host, int dstPort);
	void 			close();
	boolean 		isOpen();
	void 			setDestination(String host, int port);
	void			setLocalPort(int port);
	void			setNetworkInterface(NetworkInterface network);
}
