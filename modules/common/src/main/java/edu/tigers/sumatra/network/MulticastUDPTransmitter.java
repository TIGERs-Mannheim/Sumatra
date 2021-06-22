/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Transmit data to a multicast group on one or more network interfaces.
 */
@Log4j2
public class MulticastUDPTransmitter implements AutoCloseable
{
	private final List<TargetSocket> sockets = new ArrayList<>();
	private final SocketAddress targetAddr;


	/**
	 * @param targetAddr multicast address to send to
	 * @param targetPort network port to send to
	 */
	public MulticastUDPTransmitter(final String targetAddr, final int targetPort)
	{
		this.targetAddr = new InetSocketAddress(targetAddr, targetPort);
	}


	private List<NetworkInterface> getNetworkInterfaces()
	{
		try
		{
			return NetworkInterface.networkInterfaces().collect(Collectors.toUnmodifiableList());
		} catch (SocketException e)
		{
			log.error("Could not get available network interfaces", e);
		}
		return Collections.emptyList();
	}


	public void connectToAllInterfaces()
	{
		getNetworkInterfaces().forEach(this::connectTo);
	}


	public void connectTo(String nifName)
	{
		try
		{
			var nif = NetworkInterface.getByName(nifName);
			if (nif != null)
			{
				connectTo(nif);
			} else
			{
				log.warn("Specified nif not found: {}", nifName);
			}
		} catch (SocketException e)
		{
			log.error("Could not get an interface by name", e);
		}
	}


	private void connectTo(NetworkInterface nif)
	{
		try
		{
			if (nif.supportsMulticast())
			{
				@SuppressWarnings("squid:S2095") // closing resources: can not close resource here
				var socket = new MulticastSocket();
				socket.setNetworkInterface(nif);
				sockets.add(new TargetSocket(socket));
			}
		} catch (IOException e)
		{
			log.warn("Could not connect at {}", nif, e);
		}
	}


	public synchronized void send(final byte[] data)
	{
		DatagramPacket tempPacket = new DatagramPacket(data, data.length, targetAddr);

		for (var targetSocket : sockets)
		{
			try
			{
				targetSocket.socket.send(tempPacket);
				targetSocket.lastSendFailed = false;
			} catch (IOException err)
			{
				if (!targetSocket.lastSendFailed)
				{
					log.warn("Error while sending data to '{}'", targetAddr, err);
					targetSocket.lastSendFailed = true;
				}
			}
		}
	}


	@Override
	public synchronized void close()
	{
		sockets.forEach(TargetSocket::close);
		sockets.clear();
	}


	@RequiredArgsConstructor
	private static class TargetSocket
	{
		private final MulticastSocket socket;
		private boolean lastSendFailed;


		private void close()
		{
			socket.close();
		}
	}
}
