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
	public abstract void enqueueCommand(ACommand cmd);
	

	public abstract void start();
	

	public abstract void stop();
	

	public abstract void setSocket(DatagramSocket newSocket) throws IOException;
	

	public abstract void setDestination(InetAddress dstIp, int dstPort);
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public abstract Statistics getStats();
	
}