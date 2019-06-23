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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * Transceiver communicating with UDP packets via multicast.
 * 
 * @author AndreR
 */
public class MulticastTransceiverUDP implements ITransceiverUDP, IReceiverUDPObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log			= Logger.getLogger(MulticastTransceiverUDP.class.getName());
	
	private int												dstPort		= 0;
	private int												localPort	= 0;
	private InetAddress									group			= null;
	private MulticastSocket								socket		= null;
	private NetworkInterface							network		= null;
	
	private final TransmitterUDP						transmitter	= new TransmitterUDP();
	private final ReceiverUDP							receiver		= new ReceiverUDP();
	
	private final List<ITransceiverUDPObserver>	observers	= new ArrayList<ITransceiverUDPObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param legacy
	 */
	public MulticastTransceiverUDP(final boolean legacy)
	{
		transmitter.setLegacy(legacy);
		receiver.setLegacy(legacy);
	}
	
	
	/**
	 * @param group
	 * @param dstPort
	 * @param localPort
	 * @param open
	 */
	public MulticastTransceiverUDP(final String group, final int dstPort, final int localPort, final boolean open)
	{
		setDestination(group, dstPort);
		setLocalPort(localPort);
		if (open)
		{
			open();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addObserver(final ITransceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	@Override
	public void removeObserver(final ITransceiverUDPObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	@Override
	public void open()
	{
		close();
		
		try
		{
			socket = new MulticastSocket(localPort);
			
			socket.setNetworkInterface(network);
			socket.setTimeToLive(32);
			socket.joinGroup(group);
			
			receiver.setSocket(socket);
			
			transmitter.setSocket(socket);
			transmitter.setDestination(group, dstPort);
			
			receiver.addObserver(this);
			
			transmitter.start();
			receiver.start();
		}
		
		catch (final IOException e)
		{
			log.error("Could not create UDP multicast socket on port: " + localPort);
			
			socket = null;
		}
	}
	
	
	@Override
	public void open(final String host, final int dstPort)
	{
		try
		{
			close();
			
			group = InetAddress.getByName(host);
			this.dstPort = dstPort;
			
			open();
		}
		
		catch (final UnknownHostException err)
		{
			log.error("Could not resolve " + host, err);
		}
	}
	
	
	@Override
	public boolean isOpen()
	{
		return (socket != null);
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
	public void close()
	{
		if (socket == null)
		{
			return;
		}
		
		receiver.removeObserver(this);
		
		transmitter.stop();
		receiver.stop();
		
		try
		{
			socket.leaveGroup(group);
		}
		
		catch (final IOException e)
		{
			log.error("Could not leave UDP multicast group: " + group.getHostAddress());
		}
		
		if (!socket.isClosed())
		{
			socket.close();
		}
		
		socket = null;
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
	public void setDestination(final String group, final int port)
	{
		boolean start = false;
		
		if (socket != null)
		{
			start = true;
			close();
		}
		
		try
		{
			dstPort = port;
			this.group = InetAddress.getByName(group);
		}
		
		catch (final UnknownHostException e)
		{
			log.error("Unknown group: " + group);
			
			return;
		}
		
		if (start)
		{
			open();
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
	
	
	@Override
	public void setNetworkInterface(final NetworkInterface network)
	{
		boolean start = false;
		
		if (socket != null)
		{
			start = true;
			close();
		}
		
		this.network = network;
		
		if (start)
		{
			open();
		}
	}
	
	
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
	
	
	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
}
