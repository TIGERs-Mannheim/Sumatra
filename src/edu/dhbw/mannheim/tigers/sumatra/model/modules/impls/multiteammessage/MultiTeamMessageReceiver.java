/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.multiteammessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ReceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * @author JulianT
 */
public class MultiTeamMessageReceiver implements Runnable, IReceiver
{
	private static final Logger				log			= Logger.getLogger(MultiTeamMessageReceiver.class.getName());
	
	private static final int					BUFFER_SIZE	= 10000;
	
	private final MultiTeamMessageHandler	handler;
	
	private final int								port;
	private final NetworkInterface			nif;
	
	private Thread									multiTeamMessage;
	private IReceiver								receiver;
	
	private boolean								expectIOE	= false;
	
	
	/**
	 * @param handler
	 */
	public MultiTeamMessageReceiver(final MultiTeamMessageHandler handler)
	{
		this.handler = handler;
		
		port = Integer.valueOf(UserConfig.getMultiTeamMessagePort());
		String network = UserConfig.getMultiTeamMessageInterface();
		
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		if (nif == null)
		{
			log.debug("No nif for multi-team message specified, will try all.");
			// receiver = new MulticastUDPReceiver(port, address);
			
			try
			{
				DatagramSocket ds = new DatagramSocket(port);
				receiver = new ReceiverUDP(ds);
			} catch (SocketException err)
			{
				log.error("", err);
				err.printStackTrace();
			} catch (IOException err)
			{
				log.error("", err);
			}
		} else
		{
			log.info("Chose nif for multi-team message :" + nif.getDisplayName() + ".");
			// receiver = new MulticastUDPReceiver(port, address, nif);
			
			try
			{
				DatagramSocket ds = new DatagramSocket(port);
				// ds.setNetworkInterface(nif);
				receiver = new ReceiverUDP(ds);
			} catch (SocketException err)
			{
				log.error("", err);
			} catch (IOException err)
			{
				log.error("", err);
			}
		}
		
		multiTeamMessage = new Thread(this, "Multi-team message");
		multiTeamMessage.start();
	}
	
	
	@Override
	public DatagramPacket receive(final DatagramPacket store) throws IOException
	{
		return receiver.receive(store);
	}
	
	
	@Override
	public void cleanup()
	{
		if (multiTeamMessage != null)
		{
			multiTeamMessage.interrupt();
			multiTeamMessage = null;
		}
		
		if (receiver != null)
		{
			expectIOE = true;
			
			try
			{
				receiver.cleanup();
			} catch (final IOException ioe)
			{
				log.debug("Socket closed...");
			}
			
			receiver = null;
		}
	}
	
	
	@Override
	public boolean isReady()
	{
		return true;
	}
	
	
	@Override
	public void run()
	{
		try
		{
			handler.waitOnSignal();
		} catch (final InterruptedException err)
		{
			log.debug("Interrupted while waiting for multi-team-message consumer to be set!");
			return;
		}
		
		while (!Thread.currentThread().isInterrupted())
		{
			if (receiver == null)
			{
				break;
			}
			
			final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			
			try
			{
				receive(packet);
			} catch (final IOException ioe)
			{
				if (!expectIOE)
				{
					log.error("Error while receiving multi-team message!", ioe);
				}
				
				break;
			}
			
			
			final ByteArrayInputStream packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			TeamPlan teamPlan;
			
			try
			{
				teamPlan = TeamPlan.parseFrom(packetIn);
			} catch (IOException ioe)
			{
				log.error("Could not read multi-team message ", ioe);
				continue;
			}
			
			if (teamPlan.getPlansCount() > 0)
			{
				handler.onNewMultiTeamMessage(teamPlan);
				log.info("Team plan received");
			}
		}
		
		expectIOE = false;
	}
}
