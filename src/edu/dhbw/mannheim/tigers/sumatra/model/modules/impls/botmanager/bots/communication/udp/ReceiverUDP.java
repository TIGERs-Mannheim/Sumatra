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
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;


/**
 * Receiver for UDP packets.
 * 
 * @author rYan
 * 
 */
public class ReceiverUDP
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** {@value} [ms] */
	private static final int						PORT_UNREACHABLE_RETRY_WAIT	= 1500;
	
	private final Logger								log									= Logger.getLogger(getClass());
	
	private DatagramSocket							socket								= null;
	private Thread										receiverThread						= null;
	private Statistics								stats									= new Statistics();
	
	private final List<IReceiverUDPObserver>	observers							= new ArrayList<IReceiverUDPObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ReceiverUDP()
	{
	}
	

	public ReceiverUDP(DatagramSocket newSocket) throws IOException
	{
		setSocket(newSocket);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IReceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IReceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	public void start()
	{
		if (receiverThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		receiverThread = new Thread(new Receiver());
		
		receiverThread.start();
	}
	

	public void stop()
	{
		if (receiverThread == null)
		{
			return;
		}
		
		receiverThread.interrupt();
		try
		{
			receiverThread.join(100);
		} catch (InterruptedException err)
		{
		}
		
		receiverThread = null;
	}
	

	public void setSocket(DatagramSocket newSocket) throws IOException
	{
		boolean start = false;
		
		if (receiverThread != null)
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
	

	private void notifyNewCommand(ACommand cmd)
	{
		synchronized (observers)
		{
			for (IReceiverUDPObserver observer : observers)
			{
				observer.onNewCommand(cmd);
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public Statistics getStats()
	{
		return stats;
	}
	
	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Receiver implements Runnable
	{
		public void run()
		{
			Thread.currentThread().setName("Receiver UDP");
			
			if (socket == null)
			{
				log.error("Cannot start a receiver on a null socket");
				return;
			}
			
			byte[] buf = null;
			
			try
			{
				buf = new byte[socket.getReceiveBufferSize()];
			}
			catch (SocketException err)
			{
				log.error("Could not get receive buffer size", err);
				return;
			}
			
			log.debug("Receive buffer size set to: " + buf.length);
			
			while (!Thread.currentThread().isInterrupted())
			{
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				try
				{
					socket.receive(packet);
					
					byte[] packetData = packet.getData();
					
					byte[] header = new byte[CommandConstants.HEADER_SIZE];
					
					System.arraycopy(packetData, 0, header, 0, CommandConstants.HEADER_SIZE);
					
					ACommand cmd = CommandFactory.createEmptyPacket(header);
					if (cmd == null)
					{
						log.debug("Error while parsing header, message dropped. " + Arrays.toString(header));
						continue;
					}
					
					byte[] data = new byte[cmd.getDataLength()];
					
					System.arraycopy(packetData, CommandConstants.HEADER_SIZE, data, 0, cmd.getDataLength());
					
					cmd.setData(data);
					
					stats.packets++;
					stats.payload += data.length;
					stats.raw += data.length + 54;
					
					notifyNewCommand(cmd);
				} catch (PortUnreachableException e)
				{
					long waits = TimeUnit.MILLISECONDS.toSeconds(PORT_UNREACHABLE_RETRY_WAIT);
					log.debug(socket.getLocalPort() + "->" + socket.getPort() + ": ICMP port unreachable, retry in "
							+ waits + "s.");
					
					try
					{
						Thread.sleep(PORT_UNREACHABLE_RETRY_WAIT);
					} catch (InterruptedException err)
					{
						log.debug("Interrupted while waiting after ICMP port unreachable.");
					}
					continue;
					
				} catch (SocketException e)
				{
					log.info("UDP transceiver terminating");
					Thread.currentThread().interrupt();
				} catch (IOException err)
				{
					log.error("Some IOException", err);
				}
			}
		}
	}
}
