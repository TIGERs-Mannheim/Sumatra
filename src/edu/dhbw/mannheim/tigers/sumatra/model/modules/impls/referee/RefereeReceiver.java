/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.NetworkInterface;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * New implementation of the referee receiver with the new protobuf message format (2013)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RefereeReceiver implements Runnable, IReceiver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger		log			= Logger.getLogger(RefereeReceiver.class.getName());
	
	private static final int			BUFFER_SIZE	= 10000;
	
	private final RefereeHandler		handler;
	
	private final int						port;
	private final String					address;
	private final NetworkInterface	nif;
	
	private long							lastId;
	
	private Thread							referee;
	private IReceiver						receiver;
	
	private boolean						expectIOE	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 * @param handler
	 */
	public RefereeReceiver(SubnodeConfiguration subnodeConfiguration, RefereeHandler handler)
	{
		this.handler = handler;
		
		lastId = -1;
		address = subnodeConfiguration.getString("address");
		port = Integer.valueOf(subnodeConfiguration.getInt("port"));
		String network = subnodeConfiguration.getString("interface", "");
		
		
		// --- Choose network-interface
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.info("No nif for referee specified, will try all.");
		} else
		{
			log.info("Chose nif for referee: " + nif.getDisplayName() + ".");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public void start()
	{
		if (nif == null)
		{
			receiver = new MulticastUDPReceiver(port, address);
		} else
		{
			receiver = new MulticastUDPReceiver(port, address, nif);
		}
		
		referee = new Thread(this, "Referee");
		referee.start();
	}
	
	
	@Override
	public void run()
	{
		try
		{
			handler.waitOnSignal();
		} catch (final InterruptedException err)
		{
			log.debug("Interrupted while waiting for referee-consumer to be set!");
			return;
		}
		
		
		while (!Thread.currentThread().isInterrupted())
		{
			if (receiver == null)
			{
				break;
			}
			
			// Fetch packet
			final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try
			{
				receive(packet);
			} catch (final IOException err)
			{
				lastId = -1;
				if (!expectIOE)
				{
					log.error("Error while receiving referee-message!", err);
				}
				break;
			}
			
			final ByteArrayInputStream packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			
			SSL_Referee sslRefereeMsg;
			try
			{
				sslRefereeMsg = SSL_Referee.parseFrom(packetIn);
			} catch (IOException err)
			{
				lastId = -1;
				log.error("Could not read referee message ", err);
				continue;
			}
			
			// Translate
			final TeamProps teamProps = handler.getTeamProperties();
			if (teamProps == null)
			{
				// Was interrupted...
				break;
			}
			final RefereeMsg msg = new RefereeMsg(sslRefereeMsg, teamProps);
			teamProps.setKeeperId(msg.getTeamInfoTigers().getGoalie());
			
			// If this message really contains new game-state information: Pass it to the agent!
			if (isNewMessage(msg))
			{
				handler.notifyConsumer(msg);
			}
			
			// Notify the receipt of a new RefereeMessage to any other observers
			handler.onNewRefereeMsg(msg);
		}
		
		// Cleanup
		expectIOE = false;
	}
	
	
	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 * @author FriederB
	 */
	private boolean isNewMessage(RefereeMsg msg)
	{
		if (lastId < msg.getCommandCounter())
		{
			lastId = msg.getCommandCounter();
			return true;
		}
		return false;
	}
	
	
	@Override
	public DatagramPacket receive(DatagramPacket store) throws IOException
	{
		return receiver.receive(store);
	}
	
	
	@Override
	public void cleanup()
	{
		if (referee != null)
		{
			referee.interrupt();
			referee = null;
		}
		
		if (receiver != null)
		{
			expectIOE = true;
			
			try
			{
				receiver.cleanup();
			} catch (final IOException err)
			{
				log.debug("Socket closed...");
			}
			
			receiver = null;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public boolean isReady()
	{
		return true;
	}
}
