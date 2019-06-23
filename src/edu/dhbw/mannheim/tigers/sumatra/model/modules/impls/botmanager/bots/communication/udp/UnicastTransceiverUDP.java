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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;

/**
 * Transceiver communicating with UDP packets via unicast.
 * 
 * @author AndreR
 * 
 */
public class UnicastTransceiverUDP implements ITransceiverUDP, IReceiverUDPObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger log = Logger.getLogger(getClass());
	
	private int dstPort = 0;
	private int localPort = 0;
	private InetAddress destination = null;
	private DatagramSocket socket = null;
	
	private ITransmitterUDP transmitter = new BufferedTransmitterUDP();
	private ReceiverUDP receiver = new ReceiverUDP();
	
	private final List<ITransceiverUDPObserver> observers = new ArrayList<ITransceiverUDPObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public UnicastTransceiverUDP()
	{
	}
	
	public UnicastTransceiverUDP(String host, int port, boolean open)
	{
		if(open)
		{
			open(host, port);
		}
		else
		{
			setDestination(host, port);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ITransceiverUDPObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
	
	public void removeObserver(ITransceiverUDPObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}

	public void enqueueCommand(ACommand cmd)
	{
		if(socket == null)
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
				
			socket.connect(destination, dstPort);
			
			receiver.setSocket(socket);
			
			transmitter.setSocket(socket);
			transmitter.setDestination(destination, dstPort);
		}
		
		catch (SocketException err)
		{
			log.error("Could not create UDP socket: " + destination.getHostAddress() + ":" + dstPort, err);
			
			return;
		}
		
		catch (IOException err)
		{
			log.error("Transmitter or receiver setup failed", err);
			
			return;
		}
		
		receiver.addObserver(this);
			
		transmitter.start();
		receiver.start();
	}

	@Override
	public void open(String host, int newPort)
	{
		close();
		
		try
		{
			destination = InetAddress.getByName(host);

			this.dstPort = newPort;
			
			open();
		}
		
		catch (UnknownHostException err)
		{
			log.error("Could not resolve " + host, err);
		}
	}

	@Override
	public void close()
	{
		if(socket != null)
		{
			receiver.removeObserver(this);
			
			transmitter.stop();
			receiver.stop();
			
			socket.close();			
			socket.disconnect();
			
			if(!socket.isClosed())
			{
				socket.close();
			}
						
			socket = null;
		}
	}
	
	@Override
	public void setNetworkInterface(NetworkInterface network)
	{
	}
	
	public boolean isOpen()
	{
		return (socket != null);
	}
	
	@Override
	public void onNewCommand(ACommand cmd)
	{
		notifyIncommingCommand(cmd);
	}

	private void notifyIncommingCommand(ACommand cmd)
	{
		synchronized(observers)
		{
			for (ITransceiverUDPObserver observer : observers)
			{
				observer.onIncommingCommand(cmd);
			}
		}
	}
	
	private void notifyOutgoingCommand(ACommand cmd)
	{
		synchronized(observers)
		{
			for (ITransceiverUDPObserver observer : observers)
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
	
	public void setDestination(String dstAddr, int newPort)
	{
		boolean start = false;
		
		if(socket != null)
		{
			start = true;
			close();
		}
		
		try
		{
			destination = InetAddress.getByName(dstAddr);

			this.dstPort = newPort;
		}
		
		catch(UnknownHostException e)
		{
			log.error("Unknown host: " + dstAddr);
		}
		
		if(start)
		{
			open(dstAddr, newPort);
		}
	}
	
	@Override
	public void setLocalPort(int port)
	{
		boolean start = false;
		
		if(socket != null)
		{
			start = true;
			close();
		}
		
		this.localPort = port;
		
		if(start)
		{
			open();
		}
	}

	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
}
