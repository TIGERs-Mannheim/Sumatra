/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;


import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sim.util.network.MulticastUDPTransmitter;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;

/**
 * This class sends given RefereeMessages via Multicast UDP.
 * The official SSL-Protocol is used.
 * 
 * @author Malte
 * 
 */
public class RefereeMsgTransmitter 
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private MulticastUDPTransmitter multicastUDPTransmitter; 
	private byte[] packet;
	
	private final int localPort = 17611;
	private final String address;
	private final int targetPort;
	
	protected final Logger		log			= Logger.getLogger(getClass());
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RefereeMsgTransmitter(SubnodeConfiguration config) 
	{
		address = config.getString("address", "224.5.23.1");
		targetPort = Integer.valueOf(config.getString("port", " 10001"));
		multicastUDPTransmitter = new MulticastUDPTransmitter(localPort, address, targetPort);	
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void sendOwnRefereeMsg(RefereeMsg msg)
	{
		if (multicastUDPTransmitter.isReady())
		{
			packet = RefereeMsgHandler.build(msg);
			multicastUDPTransmitter.send(packet);
		}
		else
		{
			log.warn("Unable to transmit Referee-Message! UDP Transmitter not ready!");
		}
		
	}
	
	public void stop()
	{
		multicastUDPTransmitter.cleanup();
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
