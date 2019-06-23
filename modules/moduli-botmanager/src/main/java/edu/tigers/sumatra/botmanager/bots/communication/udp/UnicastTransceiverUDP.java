/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * Transceiver communicating with UDP packets via unicast.
 * 
 * @author AndreR
 */
public class UnicastTransceiverUDP implements ITransceiverUDP, IReceiverUDPObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log			= Logger.getLogger(UnicastTransceiverUDP.class.getName());
	
	private int												dstPort		= 0;
	private int												localPort	= 0;
	private InetAddress									destination	= null;
	private DatagramSocket								socket		= null;
	
	private final ITransmitterUDP						transmitter	= new TransmitterUDP();
	private final ReceiverUDP							receiver		= new ReceiverUDP();
	
	private final List<ITransceiverUDPObserver>	observers	= new ArrayList<>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param legacy
	 */
	public UnicastTransceiverUDP(final boolean legacy)
	{
		transmitter.setLegacy(legacy);
		receiver.setLegacy(legacy);
	}
	
	
	/**
	 * @param host
	 * @param port
	 * @param open
	 */
	public UnicastTransceiverUDP(final String host, final int port, final boolean open)
	{
		if (open)
		{
			open(host, port);
		} else
		{
			setDestination(host, port);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addObserver(final ITransceiverUDPObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	@Override
	public void removeObserver(final ITransceiverUDPObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	@Override
	public void enqueueCommand(final ACommand cmd)
	{
		if (socket == null)
		{
			return;
		}
		
		notifyOutgoingCommand(cmd);
		
		transmitter.enqueueCommand(cmd);
	}
	
	
	@Override
	public void open()
	{
		close();
		
		try
		{
			socket = new DatagramSocket(localPort);
		} catch (final SocketException err)
		{
			log.error("Could not create UDP socket: " + localPort, err);
			return;
		}
		
		try
		{
			socket.connect(destination, dstPort);
			
			receiver.setSocket(socket);
			
			transmitter.setSocket(socket);
			transmitter.setDestination(destination, dstPort);
		}
		
		catch (final SocketException err)
		{
			log.error("Could not connect to UDP socket: " + destination.getHostAddress() + ":" + dstPort, err);
			
			return;
		}
		
		catch (final IOException err)
		{
			log.error("Transmitter or receiver setup failed", err);
			
			return;
		}
		
		receiver.addObserver(this);
		
		transmitter.start();
		receiver.start();
	}
	
	
	@Override
	public void open(final String host, final int newPort)
	{
		close();
		
		try
		{
			destination = InetAddress.getByName(host);
			
			dstPort = newPort;
			
			open();
		}
		
		catch (final UnknownHostException err)
		{
			log.error("Could not resolve " + host, err);
		}
	}
	
	
	@Override
	public void close()
	{
		if (socket != null)
		{
			receiver.removeObserver(this);
			
			transmitter.stop();
			receiver.stop();
			
			socket.close();
			socket.disconnect();
			
			if (!socket.isClosed())
			{
				socket.close();
			}
			
			socket = null;
		}
	}
	
	
	@Override
	public void setNetworkInterface(final NetworkInterface network)
	{
		// hint not used
	}
	
	
	@Override
	public boolean isOpen()
	{
		return socket != null;
	}
	
	
	@Override
	public void onNewCommand(final ACommand cmd)
	{
		notifyIncommingCommand(cmd);
	}
	
	
	private void notifyIncommingCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ITransceiverUDPObserver observer : observers)
			{
				observer.onIncommingCommand(cmd);
			}
		}
	}
	
	
	private void notifyOutgoingCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ITransceiverUDPObserver observer : observers)
			{
				observer.onOutgoingCommand(cmd);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Statistics getReceiverStats()
	{
		return receiver.getStats();
	}
	
	
	@Override
	public Statistics getTransmitterStats()
	{
		return transmitter.getStats();
	}
	
	
	@Override
	public void setDestination(final String dstAddr, final int newPort)
	{
		boolean start = false;
		
		if (socket != null)
		{
			start = true;
			close();
		}
		
		try
		{
			destination = InetAddress.getByName(dstAddr);
			
			dstPort = newPort;
		}
		
		catch (final UnknownHostException e)
		{
			log.error("Unknown host: " + dstAddr, e);
		}
		
		if (start)
		{
			open(dstAddr, newPort);
		}
	}
	
	
	@Override
	public void setLocalPort(final int port)
	{
		boolean start = false;
		
		if (socket != null)
		{
			start = true;
			close();
		}
		
		localPort = port;
		
		if (start)
		{
			open();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
}
