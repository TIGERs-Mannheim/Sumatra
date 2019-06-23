/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.multiteammessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.network.IReceiver;
import edu.tigers.sumatra.network.NetworkUtility;


/**
 * @author JulianT
 */
public class MultiTeamMessageReceiver implements Runnable, IReceiver
{
	private static final Logger log = Logger.getLogger(MultiTeamMessageReceiver.class.getName());
	private static final int BUFFER_SIZE = 10000;
	
	private TeamPlan teamPlan = TeamPlan.newBuilder().build();
	private ExecutorService executorService;
	private DatagramSocket ds;
	private final String network;
	private final int port;
	
	
	/**
	 * @param network
	 * @param port
	 */
	public MultiTeamMessageReceiver(String network, final int port)
	{
		this.network = network;
		this.port = port;
	}
	
	
	/**
	 * Start the receiver
	 */
	public void start()
	{
		NetworkInterface nif = NetworkUtility.chooseNetworkInterface(network, 3);
		
		if (nif == null || !nif.getInetAddresses().hasMoreElements())
		{
			log.debug("No nif for multi-team message specified, will try all.");
			
			try
			{
				ds = new DatagramSocket(port);
			} catch (SocketException err)
			{
				log.error("Could not create datagram socket.", err);
			}
		} else
		{
			log.info("Chose nif for multi-team message :" + nif.getDisplayName() + ".");
			
			try
			{
				ds = new DatagramSocket(port, nif.getInetAddresses().nextElement());
			} catch (SocketException err)
			{
				log.error("Could not create datagram socket.", err);
			}
		}
		
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(this);
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
		if (executorService != null)
		{
			executorService.shutdownNow();
			executorService = null;
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
		Thread.currentThread().setName("MultiTeamMessageReceiver");
		final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				receive(packet);
			} catch (final IOException ioe)
			{
				if (ds != null && !ds.isClosed())
				{
					log.error("Error while receiving multi-team message!", ioe);
				}
				return;
			}
			
			final ByteArrayInputStream packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			
			try
			{
				teamPlan = TeamPlan.parseFrom(packetIn);
			} catch (IOException ioe)
			{
				log.error("Could not read multi-team message ", ioe);
			}
		}
	}
	
	
	public TeamPlan getTeamPlan()
	{
		return teamPlan;
	}
}
