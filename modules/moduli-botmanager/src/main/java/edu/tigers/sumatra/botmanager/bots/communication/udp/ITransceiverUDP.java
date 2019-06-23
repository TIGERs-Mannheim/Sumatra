/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots.communication.udp;

import java.net.NetworkInterface;

import edu.tigers.sumatra.botmanager.bots.communication.ITransceiver;


/**
 * Transceiver interface.
 * 
 * @author AndreR
 */
public interface ITransceiverUDP extends ITransceiver
{
	/**
	 * @param o
	 */
	void addObserver(ITransceiverUDPObserver o);
	
	
	/**
	 * @param o
	 */
	void removeObserver(ITransceiverUDPObserver o);
	
	
	/**
	 * Open connection.
	 */
	void open();
	
	
	/**
	 * @param host
	 * @param dstPort
	 */
	void open(String host, int dstPort);
	
	
	/**
	 * Close connection.
	 */
	void close();
	
	
	/**
	 * @return
	 */
	boolean isOpen();
	
	
	/**
	 * @param host
	 * @param port
	 */
	void setDestination(String host, int port);
	
	
	/**
	 * @param port
	 */
	void setLocalPort(int port);
	
	
	/**
	 * @param network
	 */
	void setNetworkInterface(NetworkInterface network);
}
