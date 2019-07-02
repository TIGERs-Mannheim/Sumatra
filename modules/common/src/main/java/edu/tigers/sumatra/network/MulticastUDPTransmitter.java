/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


/**
 * This class is an {@link ITransmitter} implementation capable of sending some {@code byte[]}-data via UDP to a
 * multicast-group.
 */
public class MulticastUDPTransmitter implements ITransmitter<byte[]>
{
	private static final Logger log = Logger.getLogger(MulticastUDPTransmitter.class);
	private final int targetPort;
	private final InetAddress targetAddr;
	
	private MulticastSocket socket = null;
	private boolean lastSendFailed = false;
	
	
	/**
	 * @param targetAddr multicast address to send to
	 * @param targetPort network port to send to
	 */
	public MulticastUDPTransmitter(final String targetAddr, final int targetPort)
	{
		this.targetPort = targetPort;
		this.targetAddr = addressByName(targetAddr);
		
		try
		{
			socket = new MulticastSocket();
		} catch (IOException err)
		{
			log.error("Error while creating MulticastSocket!", err);
		}
	}
	
	
	private InetAddress addressByName(final String targetAddr)
	{
		try
		{
			return InetAddress.getByName(targetAddr);
		} catch (UnknownHostException err)
		{
			log.error("The Host could not be found!", err);
		}
		return null;
	}
	
	
	@Override
	public synchronized boolean send(final byte[] data)
	{
		if (socket == null)
		{
			if (!lastSendFailed)
			{
				log.error("Transmitter is not ready to send!");
				lastSendFailed = true;
			}
			return false;
		}
		
		DatagramPacket tempPacket = new DatagramPacket(data, data.length, targetAddr, targetPort);
		
		// Receive _outside_ the synchronized state, to prevent blocking of the state
		try
		{
			socket.send(tempPacket); // DatagramPacket is sent...
			lastSendFailed = false;
		} catch (NoRouteToHostException nrh)
		{
			log.warn("No route to host: '" + targetAddr + "'. Dropping packet...", nrh);
			return false;
		} catch (IOException err)
		{
			if (!lastSendFailed)
			{
				log.error("Error while sending data to: '" + targetAddr + ":" + targetPort + "'. "
						+ "If you are not in any network, multicast is not supported by default. "
						+ "On Linux, you can enable multicast on the loopback interface by executing following commands as root: "
						+ "route add -net 224.0.0.0 netmask 240.0.0.0 dev lo && ifconfig lo multicast", err);
				lastSendFailed = true;
			}
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public synchronized void close()
	{
		if (socket != null)
		{
			socket.close();
			socket = null;
		}
	}
}
