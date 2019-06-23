/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2018
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Version number and CRC32 of application code.
 * 
 * @author AndreR
 */
public class TigerSystemVersion extends ACommand
{
	@SerialData(type = ESerialDataType.UINT32)
	private long version = 0;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long crc = 0;
	
	
	/**
	 * Constructor.
	 */
	public TigerSystemVersion()
	{
		super(ECommand.CMD_SYSTEM_VERSION);
	}
	
	
	/**
	 * @return the version
	 */
	public long getVersion()
	{
		return version;
	}
	
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(final long version)
	{
		this.version = version;
	}
	
	
	/**
	 * @return the crc
	 */
	public long getCrc()
	{
		return crc;
	}
	
	
	/**
	 * @param crc the crc to set
	 */
	public void setCrc(final long crc)
	{
		this.crc = crc;
	}
	
	
	/**
	 * Return a full version string with major.minor-crc32.
	 * 
	 * @return
	 */
	public String getFullVersionString()
	{
		long minor = version & 0xFFFF;
		long major = version >> 16;
		
		return String.format("%d.%d-%08X", major, minor, crc);
	}
	
}
