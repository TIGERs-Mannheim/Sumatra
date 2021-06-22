/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;


import lombok.extern.log4j.Log4j2;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Connect to a multicast group on all reasonable network interfaces, and receive {@link DatagramPacket}s on it.
 */
@Log4j2
public class MulticastUDPReceiver implements AutoCloseable
{
	private static final int SO_TIMEOUT = 500;
	private static final String[] USELESS_PREFIXES = { "tap", "tun", "ham", "WAN" };
	private final List<IReceiverObserver> observers = new CopyOnWriteArrayList<>();
	private MulticastSocket currentSocket;


	public MulticastUDPReceiver(String host, int port)
	{
		currentSocket = connect(port);
		addAllNetworkInterfaces(host, port);
	}


	/**
	 * Creates a MultiCastUDPReceiver with only the given nif
	 *
	 * @param host
	 * @param port
	 * @param iface
	 */
	public MulticastUDPReceiver(final String host, final int port, final NetworkInterface iface)
	{
		currentSocket = connect(port);
		joinOnInterface(port, host, iface);
	}


	private void addAllNetworkInterfaces(String host, int port)
	{
		for (NetworkInterface iface : getNetworkInterfaces())
		{
			if (isUselessInterface(iface))
			{
				log.debug("Filtered network interface: " + iface.getDisplayName());
				continue;
			}
			joinOnInterface(port, host, iface);
		}
	}


	public void addObserver(final IReceiverObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final IReceiverObserver observer)
	{
		observers.remove(observer);
	}


	private MulticastSocket connect(int port)
	{
		try
		{
			return new MulticastSocket(new InetSocketAddress(port));
		} catch (IOException err)
		{
			log.error("Could not create new multicast socket", err);
		}
		return null;
	}


	private List<NetworkInterface> getNetworkInterfaces()
	{
		try
		{
			var ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			log.debug("Found " + ifaces.size() + " network interfaces");
			return ifaces;
		} catch (SocketException err)
		{
			log.error("Unable to get a list of network interfaces", err);
		}
		return List.of();
	}


	private boolean isUselessInterface(final NetworkInterface iface)
	{
		try
		{
			if (iface.getInterfaceAddresses().isEmpty() || iface.isPointToPoint() || iface.isVirtual() || !iface.isUp())
			{
				return true;
			}
		} catch (SocketException err)
		{
			log.error("Could not determine iface properties", err);
		}

		for (String prefix : USELESS_PREFIXES)
		{
			if (iface.getDisplayName().contains(prefix))
			{
				return true;
			}
		}
		return false;
	}


	private void joinOnInterface(final int port, final String groupStr, final NetworkInterface iface)
	{
		try
		{
			log.debug("Using network interface '{}' with MTU {}", iface.getDisplayName(), iface.getMTU());
			currentSocket.joinGroup(new InetSocketAddress(groupStr, port), iface);
			log.debug("Multicast group {}:{} joined on nif {}", groupStr, port, iface.getDisplayName());
		} catch (IOException err)
		{
			log.info("Could not create multicast socket on iface " + iface.getDisplayName() + " and port " + port, err);
		}
	}


	public void receive(final DatagramPacket store) throws IOException
	{
		if (currentSocket == null)
		{
			throw new IOException("Connection is closed");
		}

		while (currentSocket != null)
		{
			try
			{
				currentSocket.receive(store);
				currentSocket.setSoTimeout(SO_TIMEOUT);
				return;
			} catch (EOFException eof)
			{
				log.error("EOF error, buffer may be too small", eof);
			} catch (SocketTimeoutException e)
			{
				log.debug("No data received for {} ms", SO_TIMEOUT, e);
				observers.forEach(IReceiverObserver::onSocketTimedOut);
				currentSocket.setSoTimeout(0);
			}
		}
		throw new IOException("No data received");
	}


	@Override
	public void close()
	{
		if (currentSocket != null)
		{
			currentSocket.close();
		}
		observers.clear();
	}
}
