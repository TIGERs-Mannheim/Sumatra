/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * UDP Transmitter interface.
 * 
 * @author AndreR
 */
public interface ITransmitterUDP
{
	/**
	 * @param cmd
	 */
	void enqueueCommand(ACommand cmd);
	
	
	/**
	 * Start transmitter.
	 */
	void start();
	
	
	/**
	 * Stop transmitter.
	 */
	void stop();
	
	
	/**
	 * @param newSocket
	 * @throws IOException
	 */
	void setSocket(DatagramSocket newSocket) throws IOException;
	
	
	/**
	 * @param dstIp
	 * @param dstPort
	 */
	void setDestination(InetAddress dstIp, int dstPort);
	
	
	/**
	 * @return
	 */
	Statistics getStats();
}