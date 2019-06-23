/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;

/**
 * Transmitter for UDP packets.
 * 
 * @author AndreR
 * 
 */
public class TransmitterUDP implements ITransmitterUDP
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger log = Logger.getLogger(getClass());
	
	private InetAddress destination = null;
	private int destPort = 0;
	private DatagramSocket socket = null;
	private BlockingQueue<ACommand> sendQueue = new LinkedBlockingQueue<ACommand>();
	private Thread sendingThread = null;
	private Statistics stats = new Statistics();
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void enqueueCommand(ACommand cmd)
	{
		try
		{
			sendQueue.put(cmd);
		}
		catch (InterruptedException err)
		{
		}
	}
	
	@Override
	public void start()
	{
		if(sendingThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		sendingThread = new Thread(new Sender());
		
		sendingThread.start();
	}
	
	@Override
	public void stop()
	{
		if(sendingThread == null)
		{
			return;
		}
		
		while(!sendQueue.isEmpty())
		{
			try
			{
				Thread.sleep(10);
			}
			
			catch(InterruptedException e)
			{
			}
		}
		
		sendingThread.interrupt();
		try
		{
			sendingThread.join(100);
		}
		catch (InterruptedException err)
		{
		}
		
		sendingThread = null;
	}

	@Override
	public void setSocket(DatagramSocket newSocket) throws IOException
	{
		boolean start = false;
		
		if(sendingThread != null)
		{
			stop();
			start = true;
		}
		
		socket = newSocket;

		if(start)
		{
			start();
		}
	}
	
	@Override
	public void setDestination(InetAddress dstIp, int dstPort)
	{
		destination = dstIp;
		destPort = dstPort;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Statistics getStats()
	{
		return stats;
	}

	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Sender implements Runnable
	{
		public void run()
		{
			Thread.currentThread().setName("Transmitter UDP");
			
			if(socket == null)
			{
				log.error("Cannot start a transmitter on a null socket");
			}
			
			while(!Thread.currentThread().isInterrupted())
			{
				ACommand cmd;
				
				try
				{
					cmd = sendQueue.take();
				}
				catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
					continue;
				}
				
				try
				{
					byte data[] = cmd.getTransferData();
					
					DatagramPacket packet = new DatagramPacket(data, data.length, destination, destPort);
					socket.send(packet);
					
					stats.packets++;
					stats.raw += data.length + 54;	// Ethernet + IP + UDP header length
					stats.payload += data.length;
				}
				catch (IOException e)
				{
					Thread.currentThread().interrupt();
					continue;
				}
			}
		}
	}
}
