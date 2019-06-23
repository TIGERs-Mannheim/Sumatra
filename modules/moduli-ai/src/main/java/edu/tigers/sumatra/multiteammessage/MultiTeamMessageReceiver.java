/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.multiteammessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.network.IReceiver;
import edu.tigers.sumatra.network.NetworkUtility;


/**
 * @author JulianT
 */
public class MultiTeamMessageReceiver implements Runnable, IReceiver
{
	private static final Logger				log			= Logger.getLogger(MultiTeamMessageReceiver.class.getName());
	
	private static final int					BUFFER_SIZE	= 10000;
	
	private final MultiTeamMessageHandler	handler;
	
	private Thread									multiTeamMessage;
	private DatagramSocket						ds;
	
	private boolean								expectIOE	= false;
	
	@Configurable
	private static int							port			= 10012;
	@Configurable
	private static String						network		= "";
	
	
	/**
	 * @param handler
	 */
	public MultiTeamMessageReceiver(final MultiTeamMessageHandler handler)
	{
		this.handler = handler;
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		NetworkInterface nif = NetworkUtility.chooseNetworkInterface(network, 3);
		
		if (nif == null)
		{
			log.debug("No nif for multi-team message specified, will try all.");
			
			try
			{
				ds = new DatagramSocket(port);
			} catch (SocketException err)
			{
				log.error("", err);
				err.printStackTrace();
			}
		} else
		{
			log.info("Chose nif for multi-team message :" + nif.getDisplayName() + ".");
			// receiver = new MulticastUDPReceiver(port, address, nif);
			
			try
			{
				ds = new DatagramSocket(port);
				// ds.setNetworkInterface(nif);
			} catch (SocketException err)
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
		ds.receive(store);
		return store;
	}
	
	
	@Override
	public void cleanup()
	{
		if (multiTeamMessage != null)
		{
			expectIOE = true;
			multiTeamMessage.interrupt();
			multiTeamMessage = null;
		}
		
		if (ds != null)
		{
			ds.close();
			ds = null;
		}
	}
	
	
	@Override
	public boolean isReady()
	{
		return (ds != null) && !ds.isClosed();
	}
	
	
	@Override
	public void run()
	{
		while (!Thread.currentThread().isInterrupted())
		{
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
			}
		}
		
		expectIOE = false;
	}
}
