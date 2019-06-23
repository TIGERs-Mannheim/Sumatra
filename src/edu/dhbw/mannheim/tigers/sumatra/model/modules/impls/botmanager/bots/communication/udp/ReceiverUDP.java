/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.network.IReceiver;


/**
 * Receiver for UDP packets.
 * 
 * @author rYan
 */
public class ReceiverUDP implements IReceiver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log									= Logger.getLogger(ReceiverUDP.class
																											.getName());
	
	/** [ms] */
	private static final int						PORT_UNREACHABLE_RETRY_WAIT	= 1500;
	
	private DatagramSocket							socket								= null;
	private Thread										receiverThread						= null;
	private final Statistics						stats									= new Statistics();
	private boolean									legacy								= false;
	
	private final List<IReceiverUDPObserver>	observers							= new ArrayList<IReceiverUDPObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public ReceiverUDP()
	{
	}
	
	
	/**
	 * @param newSocket
	 * @throws IOException
	 */
	public ReceiverUDP(final DatagramSocket newSocket) throws IOException
	{
		setSocket(newSocket);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IReceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IReceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		if (receiverThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		receiverThread = new Thread(new Receiver(), "ReceiverUDP");
		
		receiverThread.start();
	}
	
	
	/**
	 * 
	 */
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
		} catch (final InterruptedException err)
		{
		}
		
		receiverThread = null;
	}
	
	
	/**
	 * @param newSocket
	 */
	public final void setSocket(final DatagramSocket newSocket)
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
	
	
	/**
	 * @param cmd
	 */
	private void notifyNewCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final IReceiverUDPObserver observer : observers)
			{
				observer.onNewCommand(cmd);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public Statistics getStats()
	{
		return stats;
	}
	
	
	/**
	 * @return the legacy
	 */
	public boolean isLegacy()
	{
		return legacy;
	}
	
	
	/**
	 * @param legacy the legacy to set
	 */
	public void setLegacy(final boolean legacy)
	{
		this.legacy = legacy;
	}
	
	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Receiver implements Runnable
	{
		@Override
		public void run()
		{
			if (socket == null)
			{
				log.error("Cannot start a receiver on a null socket");
				return;
			}
			
			Thread.currentThread().setName("Receiver UDP " + socket.getInetAddress() + ":" + socket.getPort());
			
			byte[] buf = null;
			
			try
			{
				buf = new byte[socket.getReceiveBufferSize()];
			} catch (final SocketException err)
			{
				log.error("Could not get receive buffer size", err);
				return;
			}
			
			log.debug("Receive buffer size set to: " + buf.length);
			
			while (!Thread.currentThread().isInterrupted())
			{
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				try
				{
					socket.receive(packet);
					
					final byte[] packetData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
					
					final ACommand cmd = CommandFactory.getInstance().decode(packetData, legacy);
					if (cmd == null)
					{
						log.warn("Error decoding command.");
						continue;
					}
					
					stats.packets++;
					stats.payload += packetData.length;
					stats.raw += packetData.length + 54;
					
					notifyNewCommand(cmd);
				} catch (final PortUnreachableException e)
				{
					final long waits = TimeUnit.MILLISECONDS.toSeconds(PORT_UNREACHABLE_RETRY_WAIT);
					log.info(socket.getLocalPort() + "->" + socket.getPort() + ": ICMP port unreachable, retry in " + waits
							+ "s.");
					
					try
					{
						Thread.sleep(PORT_UNREACHABLE_RETRY_WAIT);
					} catch (final InterruptedException err)
					{
						log.debug("Interrupted while waiting after ICMP port unreachable.");
					}
					continue;
					
				} catch (final SocketException e)
				{
					log.info("UDP transceiver terminating");
					Thread.currentThread().interrupt();
				} catch (final IOException err)
				{
					log.error("Some IOException", err);
				} catch (final Exception err)
				{
					log.warn("Unexpected exception! See stacktrace", err);
				}
			}
		}
	}
	
	
	@Override
	public DatagramPacket receive(final DatagramPacket store) throws IOException
	{
		byte[] buf = null;
		
		try
		{
			buf = new byte[socket.getReceiveBufferSize()];
		} catch (final SocketException err)
		{
			log.error("Could not get receive buffer size", err);
			return null;
		}
		
		final DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		socket.receive(packet);
		return packet;
	}
	
	
	@Override
	public void cleanup() throws IOException
	{
		stop();
	}
	
	
	@Override
	public boolean isReady()
	{
		return socket.isConnected();
	}
}
