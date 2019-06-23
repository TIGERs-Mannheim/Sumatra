/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * UDP Transmitter interface.
 * 
 * @author AndreR
 * 
 */
public interface ITransmitterUDP
{
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param cmd
	 */
	void enqueueCommand(ACommand cmd);
	
	
	/**
	 *
	 */
	void start();
	
	
	/**
	 *
	 */
	void stop();
	
	
	/**
	 * 
	 * @param newSocket
	 * @throws IOException
	 */
	void setSocket(DatagramSocket newSocket) throws IOException;
	
	
	/**
	 * 
	 * @param dstIp
	 * @param dstPort
	 */
	void setDestination(InetAddress dstIp, int dstPort);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	Statistics getStats();
	
	
	/**
	 * 
	 * @param legacy
	 */
	void setLegacy(boolean legacy);
}