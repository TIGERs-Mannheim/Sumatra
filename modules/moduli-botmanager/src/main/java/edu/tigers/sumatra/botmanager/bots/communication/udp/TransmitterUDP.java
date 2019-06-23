/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;


/**
 * Transmitter for UDP packets.
 * 
 * @author AndreR
 */
public class TransmitterUDP implements ITransmitterUDP
{
	private static final Logger log = Logger.getLogger(TransmitterUDP.class.getName());
	
	private InetAddress destination = null;
	private int destPort = 0;
	private DatagramSocket socket = null;
	private final BlockingQueue<ACommand> sendQueue = new LinkedBlockingQueue<>();
	private Thread sendingThread = null;
	private final Statistics stats = new Statistics();
	private boolean running = true;
	
	
	@Override
	public void enqueueCommand(final ACommand cmd)
	{
		try
		{
			sendQueue.put(cmd);
		} catch (InterruptedException err)
		{
			log.debug("Could not queue cmd.", err);
			Thread.currentThread().interrupt();
		}
	}
	
	
	@Override
	public void start()
	{
		if (sendingThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		running = true;
		sendingThread = new Thread(new Sender(), "TransmitterUDP");
		sendingThread.start();
	}
	
	
	@Override
	public void stop()
	{
		if (sendingThread == null)
		{
			return;
		}
		
		int retries = 100;
		while (!sendQueue.isEmpty() && (retries > 0))
		{
			retries--;
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
		
		running = false;
		sendingThread.interrupt();
		try
		{
			sendingThread.join(100);
		} catch (InterruptedException err)
		{
			Thread.currentThread().interrupt();
		}
		
		sendingThread = null;
	}
	
	
	@Override
	public void setSocket(final DatagramSocket newSocket)
	{
		boolean start = false;
		
		if (sendingThread != null)
		{
			stop();
			start = true;
		}
		
		socket = newSocket;
		
		if (start)
		{
			start();
		}
	}
	
	
	@Override
	public void setDestination(final InetAddress dstIp, final int dstPort)
	{
		destination = dstIp;
		destPort = dstPort;
	}
	
	
	@Override
	public Statistics getStats()
	{
		return stats;
	}
	
	
	private class Sender implements Runnable
	{
		@Override
		public void run()
		{
			if (socket == null)
			{
				log.error("Cannot start a transmitter on a null socket");
				return;
			}
			
			Thread.currentThread().setName("Transmitter UDP " + socket.getInetAddress() + ":" + socket.getPort());
			
			while (running)
			{
				ACommand cmd;
				
				try
				{
					cmd = sendQueue.take();
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					continue;
				}
				
				try
				{
					byte[] data = CommandFactory.getInstance().encode(cmd);
					
					DatagramPacket packet = new DatagramPacket(data, data.length, destination, destPort);
					socket.send(packet);
					
					stats.packets++;
					// Ethernet + IP + UDP header length
					stats.raw += data.length + 54;
					stats.payload += data.length;
				} catch (IOException e)
				{
					log.error("Could not send packet.", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
