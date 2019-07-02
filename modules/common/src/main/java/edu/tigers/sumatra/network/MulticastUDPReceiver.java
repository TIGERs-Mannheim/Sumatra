/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;


import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;


/**
 * This class should be able to register to a multicast group, and receive {@link DatagramPacket}s
 * on it
 * 
 * @author Gero
 */
public class MulticastUDPReceiver implements IReceiver
{
	private static final Logger log = Logger.getLogger(MulticastUDPReceiver.class.getName());
	
	private static final int SO_TIMEOUT = 500;
	private static final String[] USELESS_PREFIXES = { "tap", "tun", "ham", "WAN" };
	private static final String[] PREFERRED_PREFIXES = { "eth", "enp" };
	// Connection
	private final List<MulticastSocket> sockets = new ArrayList<>();
	private final Set<MulticastSocket> socketsTimedOut = new HashSet<>();
	private final List<IReceiverObserver> observers = new CopyOnWriteArrayList<>();
	private MulticastSocket currentSocket = null;
	private InetAddress group = null;
	/** The internal state-switch of this transmitter */
	private boolean readyToReceive;
	
	
	/**
	 * @param port
	 * @param groupStr
	 */
	public MulticastUDPReceiver(final int port, final String groupStr)
	{
		List<NetworkInterface> ifaces = getNetworkInterfaces();
		for (NetworkInterface iface : ifaces)
		{
			try
			{
				if (isUselessInterface(iface))
				{
					log.debug("Filtered network interface: " + iface.getDisplayName());
					continue;
				}
				log.debug("Using network interface: " + iface.getDisplayName() + " with MTU " + iface.getMTU());
				addSocket(port, groupStr, iface);
			} catch (SocketException err)
			{
				log.error("Weird. Could not determine if network interface is p2p", err);
			}
		}
		
		setSocketTimeouts();
		
		readyToReceive = true;
	}
	
	
	/**
	 * Creates a MultiCastUDPReceiver with only the given nif
	 * 
	 * @param port
	 * @param groupStr
	 * @param iface
	 */
	public MulticastUDPReceiver(final int port, final String groupStr, final NetworkInterface iface)
	{
		addSocket(port, groupStr, iface);
		
		readyToReceive = true;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IReceiverObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IReceiverObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private List<NetworkInterface> getNetworkInterfaces()
	{
		List<NetworkInterface> ifaces = new LinkedList<>();
		try
		{
			ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			log.debug("Found " + ifaces.size() + " network interfaces");
		} catch (SocketException err)
		{
			log.error("Unable to get a list of network interfaces", err);
		}
		return ifaces;
	}
	
	
	private void setSocketTimeouts()
	{
		if (sockets.size() > 1)
		{
			for (MulticastSocket socket : sockets)
			{
				try
				{
					socket.setSoTimeout(SO_TIMEOUT);
				} catch (SocketException err)
				{
					log.error("Could not set SO_TIMEOUT", err);
				}
			}
		}
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
	
	
	@SuppressWarnings("squid:S2095") // we do not want to close the multicast socket here
	private void addSocket(final int port, final String groupStr, final NetworkInterface iface)
	{
		try
		{
			MulticastSocket socket = new MulticastSocket(new InetSocketAddress(port));
			socket.setNetworkInterface(iface);
			int i = findSocketIndex(socket);
			sockets.add(i, socket);
			joinNetworkGroup(socket, groupStr);
		} catch (IOException err)
		{
			log.info("Could not create multicast socket on iface " + iface.getDisplayName() + " and port " + port, err);
		}
	}
	
	
	private int findSocketIndex(final MulticastSocket socket) throws SocketException
	{
		for (int i = 0; i < sockets.size(); i++)
		{
			for (String prefix : PREFERRED_PREFIXES)
			{
				if (socket.getNetworkInterface().getDisplayName().contains(prefix))
				{
					return i;
				}
				if (sockets.get(i).getNetworkInterface().getDisplayName().contains(prefix))
				{
					break;
				}
			}
		}
		return sockets.size();
	}
	
	
	private void joinNetworkGroup(final MulticastSocket socket, final String groupStr)
	{
		// Parse group
		try
		{
			group = InetAddress.getByName(groupStr);
		} catch (UnknownHostException err)
		{
			log.error("Unable to read multicast group address!", err);
		}
		
		// Join group
		try
		{
			socket.setReuseAddress(true);
			socket.joinGroup(group);
			log.debug("Multicast group " + group + "joined");
		} catch (IOException err)
		{
			log.error("Could not resolve address: " + group, err);
		}
	}
	
	
	@SuppressWarnings("squid:S2583")
	@Override
	public DatagramPacket receive(final DatagramPacket store) throws IOException
	{
		if (!isReady())
		{
			log.error("Receiver is not ready to receive!");
			return null;
		}
		
		// first try current socket for performance reasons
		if (currentSocket != null)
		{
			try
			{
				currentSocket.receive(store);
				return store;
			} catch (EOFException eof)
			{
				log.error("EOF error, buffer may be too small!", eof);
			} catch (SocketTimeoutException e)
			{
				log.debug("Timed out on primary socket.", e);
				// go on below
				for (IReceiverObserver obs : observers)
				{
					obs.onInterfaceTimedOut();
				}
			}
		}
		
		while (isReady())
		{
			for (MulticastSocket socket : sockets)
			{
				try
				{
					tryReceiving(store, socket);
					return store;
				} catch (SocketTimeoutException e)
				{
					if (!socketsTimedOut.contains(socket))
					{
						log.debug("Socket timed out on iface " + socket.getNetworkInterface().getDisplayName(), e);
						socketsTimedOut.add(socket);
					}
				}
			}
		}
		
		return store;
	}
	
	
	private void tryReceiving(final DatagramPacket store, final MulticastSocket socket) throws IOException
	{
		// blocking receive with a timeout of SO_TIMEOUT
		socket.receive(store);
		
		// we have received something
		
		socketsTimedOut.remove(socket);
		if (socket != currentSocket)
		{
			sockets.remove(socket);
			sockets.add(0, socket);
			currentSocket = socket;
			log.info("MulticastSocket changed to " + socket.getNetworkInterface().getDisplayName() + " "
					+ socket.getLocalPort());
		}
	}
	
	
	@Override
	public void cleanup()
	{
		// No-working state after this line...
		readyToReceive = false;
		
		for (MulticastSocket socket : sockets)
		{
			if (!socket.isClosed())
			{
				leaveMulticastGroup(socket);
				socket.close();
			}
		}
		observers.clear();
	}
	
	
	private void leaveMulticastGroup(final MulticastSocket socket)
	{
		try
		{
			if (group != null)
			{
				socket.leaveGroup(group);
				log.debug("Multicast group left");
			}
		} catch (IOException err)
		{
			log.error("Error while leaving multicast group '" + group + "'!", err);
		}
	}
	
	
	@Override
	public boolean isReady()
	{
		return readyToReceive;
	}
}
