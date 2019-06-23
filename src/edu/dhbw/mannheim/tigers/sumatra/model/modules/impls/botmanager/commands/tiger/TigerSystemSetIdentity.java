/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Assigns the network identity of a bot based on a unique CPU ID.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemSetIdentity extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(TigerSystemSetIdentity.class.getName());
	
	private final long				cpuId[]		= new long[3];
	private final int					mac[]			= new int[6];
	private final int					ip[]			= new int[4];
	private int							port			= 0;
	private int							serverPort	= 0;
	private int							botId			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerSystemSetIdentity()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set CPU ID.
	 * Expected format is a 24 digit HEX number.
	 * 
	 * @param id CPU ID
	 */
	public void setCpuId(String id)
	{
		if (id.length() != 24)
		{
			log.error("Invalid CPU ID: " + id);
			return;
		}
		
		try
		{
			cpuId[2] = Long.parseLong(id.substring(0, 8), 16);
			cpuId[1] = Long.parseLong(id.substring(8, 16), 16);
			cpuId[0] = Long.parseLong(id.substring(16, 24), 16);
		}
		
		catch (final NumberFormatException e)
		{
			log.error("Invalid CPU ID: " + id);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getCpuId()
	{
		String id;
		
		id = String.format("%08X", (cpuId[2]) & 0xFFFFFFFF);
		id += String.format("%08X", (cpuId[1]) & 0xFFFFFFFF);
		id += String.format("%08X", (cpuId[0]) & 0xFFFFFFFF);
		
		return id;
	}
	
	
	/**
	 * Set MAC Address.
	 * Expected format is a dash-seperated string of 6x2 HEX digits (11-22-33-44-55-66).
	 * 
	 * @param mac_ MAC Address
	 */
	public void setMac(String mac_)
	{
		if (mac_.length() != 17)
		{
			log.error("Invalid MAC: " + mac_);
			return;
		}
		
		try
		{
			for (int i = 0; i < 6; i++)
			{
				mac[i] = Integer.parseInt(mac_.substring(i * 3, (i * 3) + 2), 16);
			}
		}
		
		catch (final NumberFormatException e)
		{
			log.error("Invalid MAC: " + mac_);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getMac()
	{
		final StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < 6; i++)
		{
			result.append(String.format("%02X", mac[i]));
			if (i != 5)
			{
				result.append("-");
			}
		}
		
		return result.toString();
	}
	
	
	/**
	 * Set IP Address.
	 * Expected format is a point separated string of four numbers (1.2.3.4).
	 * 
	 * @param ip_ IP Address
	 */
	public void setIp(String ip_)
	{
		try
		{
			final byte[] ipBytes = InetAddress.getByName(ip_).getAddress();
			
			if (ipBytes.length != 4)
			{
				log.error("Not an IPv4 address: " + ip_);
			}
			
			for (int i = 0; i < 4; i++)
			{
				ip[i] = ipBytes[i];
			}
		}
		
		catch (final UnknownHostException err)
		{
			log.error("Invalid IP: " + ip_);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getIp()
	{
		return String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
	}
	
	
	/**
	 * 
	 * @param port_
	 */
	public void setPort(int port_)
	{
		port = port_;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getPort()
	{
		return port;
	}
	
	
	/**
	 * 
	 * @param port
	 */
	public void setServerPort(int port)
	{
		serverPort = port;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getServerPort()
	{
		return serverPort;
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public void setBotId(int id)
	{
		botId = id;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getBotId()
	{
		return botId;
	}
	
	
	@Override
	public void setData(byte[] data)
	{
		cpuId[0] = byteArray2UInt(data, 0);
		cpuId[1] = byteArray2UInt(data, 4);
		cpuId[2] = byteArray2UInt(data, 8);
		
		mac[0] = byteArray2UByte(data, 12);
		mac[1] = byteArray2UByte(data, 13);
		mac[2] = byteArray2UByte(data, 14);
		mac[3] = byteArray2UByte(data, 15);
		mac[4] = byteArray2UByte(data, 16);
		mac[5] = byteArray2UByte(data, 17);
		
		ip[0] = byteArray2UByte(data, 18);
		ip[1] = byteArray2UByte(data, 19);
		ip[2] = byteArray2UByte(data, 20);
		ip[3] = byteArray2UByte(data, 21);
		
		port = byteArray2UShort(data, 22);
		serverPort = byteArray2UShort(data, 24);
		
		botId = byteArray2UByte(data, 26);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, (int) cpuId[0]);
		int2ByteArray(data, 4, (int) cpuId[1]);
		int2ByteArray(data, 8, (int) cpuId[2]);
		
		byte2ByteArray(data, 12, mac[0]);
		byte2ByteArray(data, 13, mac[1]);
		byte2ByteArray(data, 14, mac[2]);
		byte2ByteArray(data, 15, mac[3]);
		byte2ByteArray(data, 16, mac[4]);
		byte2ByteArray(data, 17, mac[5]);
		
		byte2ByteArray(data, 18, ip[0]);
		byte2ByteArray(data, 19, ip[1]);
		byte2ByteArray(data, 20, ip[2]);
		byte2ByteArray(data, 21, ip[3]);
		
		short2ByteArray(data, 22, port);
		short2ByteArray(data, 24, serverPort);
		
		byte2ByteArray(data, 26, botId);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_SET_IDENTITY;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 27;
	}
}
