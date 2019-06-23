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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP.EConnectionState;


/**
 * Transceiver interface.
 * 
 * @author AndreR
 * 
 */
public interface ITransceiverTCP extends ITransceiver
{
	void addObserver(ITransceiverTCPObserver o);
	void removeObserver(ITransceiverTCPObserver o);
	void connect(String host, int port);
	void disconnect();
	void reconnect();
	int getConnectTimeout();
	void setConnectTimeout(int connectTimeout);
	int getConnectInterval();
	void setConnectInterval(int connectInterval);
	boolean isConnected();
	boolean isConnecting();
	void setDestination(String host, int port);
	EConnectionState getConnectionState();
}