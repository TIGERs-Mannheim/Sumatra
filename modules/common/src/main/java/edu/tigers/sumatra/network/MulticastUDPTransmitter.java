package edu.tigers.sumatra.network;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


/**
 * This class is an {@link ITransmitter} implementation capable of sending some {@code byte[]}-data via UDP to a
 * multicast-group.
 * 
 * @author Gero
 */
public class MulticastUDPTransmitter implements ITransmitter<byte[]>
{
	
	protected final Logger		log			= Logger.getLogger(getClass());
	
	
	// Communication
	private MulticastSocket		socket		= null;
	
	private InetAddress			targetAddr	= null;
	private final int				targetPort;
	
	private DatagramPacket		tempPacket	= null;
	
	/** The internal state-switch of this transmitter */
	private volatile boolean	readyToSend	= false;
	
	
	/**
	 * @see MulticastUDPTransmitter
	 * @param localPort
	 * @param targetAddr
	 * @param targetPort
	 */
	public MulticastUDPTransmitter(final int localPort, final String targetAddr, final int targetPort)
	{
		this(localPort, targetAddr, targetPort, null);
	}
	
	
	/**
	 * @see MulticastUDPTransmitter
	 * @param localPort
	 * @param targetAddr
	 * @param targetPort
	 * @param nif
	 */
	public MulticastUDPTransmitter(int localPort, final String targetAddr, final int targetPort,
			final NetworkInterface nif)
	{
		this.targetPort = targetPort;
		
		
		while (socket == null)
		{
			try
			{
				socket = new MulticastSocket(localPort);
				socket.setReuseAddress(true);
				
				// Set nif
				if (nif != null)
				{
					socket.setNetworkInterface(nif);
				}
				
			} catch (SocketException err)
			{
				log.info("Port " + localPort + " used, will try " + ++localPort + " instead!");
				continue;
			} catch (IOException err)
			{
				log.error("Error while creating MulticastSocket!", err);
			}
		}
		
		try
		{
			this.targetAddr = InetAddress.getByName(targetAddr);
		} catch (UnknownHostException err)
		{
			log.error("The Host could not be found!", err);
		}
		
		synchronized (this)
		{
			readyToSend = true;
		}
	}
	
	
	@Override
	public boolean send(final byte[] data)
	{
		// Synchronize access to socket as it belongs to the 'state'
		DatagramSocket socket = null;
		synchronized (this)
		{
			if (!isReady())
			{
				log.error("Transmitter is not ready to send!");
				return false;
			}
			
			socket = this.socket;
		}
		
		tempPacket = new DatagramPacket(data, data.length, targetAddr, targetPort);
		
		// Receive _outside_ the synchronized state, to prevent blocking of the state
		try
		{
			socket.send(tempPacket); // DatagramPacket is sent...
		} catch (NoRouteToHostException nrh)
		{
			log.warn("No route to host: '" + targetAddr + "'. Dropping packet...");
			return false;
		} catch (IOException err)
		{
			log.error("Error while sending data to: '" + targetAddr + ":" + targetPort + "'!", err);
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public synchronized void cleanup()
	{
		readyToSend = false;
		
		targetAddr = null;
		tempPacket = null;
		
		if (socket != null)
		{
			socket.close();
			socket = null;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public synchronized int getLocalPort()
	{
		return readyToSend ? socket.getLocalPort() : UNDEFINED_PORT;
	}
	
	
	@Override
	public synchronized InetAddress getLocalAddress()
	{
		return targetAddr;
	}
	
	
	@Override
	public synchronized int getTargetPort()
	{
		return targetPort;
	}
	
	
	@Override
	public synchronized boolean isReady()
	{
		return readyToSend;
	}
	
	
	synchronized MulticastSocket getSocket()
	{
		return socket;
	}
}
