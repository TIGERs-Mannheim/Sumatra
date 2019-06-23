/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.util.collection.Pair;


/**
 * This class provides some helpful methods for choosing the right network-interface (nif) for a given IPv4-address
 * 
 * @author Gero
 */
public class NetworkUtility
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(NetworkUtility.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param networkStr
	 * @param compareBytes Number of bytes to check (from most to least significant byte)
	 * @return The first {@link NetworkInterface} in {@link NetworkInterface#getNetworkInterfaces()} that has a
	 *         {@link InterfaceAddress} whose first 3 bytes matches the first 3 bytes of the {@link Inet4Address} defined
	 *         by the given string. null, if networkStr is empty
	 */
	public static NetworkInterface chooseNetworkInterface(String networkStr, int compareBytes)
	{
		if (networkStr.trim().isEmpty())
		{
			return null;
		}
		
		InetAddress network = null;
		try
		{
			network = InetAddress.getByName(networkStr);
		} catch (UnknownHostException err1)
		{
			log.error("Unknown host: " + networkStr);
			return null;
		}
		
		
		NetworkInterface result = null;
		try
		{
			List<NetworkInterface> ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			Iterator<NetworkInterface> it = ifaces.iterator();
			while (it.hasNext() && (result == null))
			{
				NetworkInterface iface = it.next();
				if (!iface.isUp())
				{
					continue;
				}
				
				List<InetAddress> iAddrs = Collections.list(iface.getInetAddresses());
				for (InetAddress addr : iAddrs)
				{
					if (addr == null)
					{
						continue;
					}
					
					if (cmpIP4Addrs(addr, network, compareBytes))
					{
						result = iface;
						break;
					}
				}
			}
		} catch (SocketException err)
		{
			log.error("Error retrieving network-interfaces!", err);
		}
		
		return result;
	}
	
	
	private static boolean cmpIP4Addrs(InetAddress addr1, InetAddress addr2, int bytesToCompare)
	{
		byte[] addr1b = addr1.getAddress();
		byte[] addr2b = addr2.getAddress();
		
		if ((addr1b.length != 4) || (addr2b.length != 4))
		{
			return false;
		}
		
		for (int i = 0; i < bytesToCompare; i++)
		{
			if (addr1b[i] != addr2b[i])
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * @param networkStr
	 * @param listenGroup
	 * @param listenPort
	 * @param result
	 */
	public void addNifByAddress(String networkStr, String listenGroup, int listenPort,
			List<Pair<InetSocketAddress, NetworkInterface>> result)
	{
		try
		{
			InetSocketAddress socAddr = new InetSocketAddress(listenGroup, listenPort);
			NetworkInterface nif = chooseNetworkInterface(networkStr, 3);
			
			if (nif == null)
			{
				log.debug("No nif for '" + networkStr + "' found!");
				return;
			}
			
			if (nif.supportsMulticast())
			{
				// Check if nif already there:
				for (Pair<InetSocketAddress, NetworkInterface> p : result)
				{
					if (p.right.equals(nif))
					{
						// Done, jump off
						return;
					}
				}
				result.add(new Pair<InetSocketAddress, NetworkInterface>(socAddr, nif));
			} else
			{
				log.debug("Nif '" + nif + "' doesn't support multicast!");
			}
		} catch (SocketException err)
		{
			log.debug("Error detecting network to listen on incoming bot-discovery...");
		}
	}
}
