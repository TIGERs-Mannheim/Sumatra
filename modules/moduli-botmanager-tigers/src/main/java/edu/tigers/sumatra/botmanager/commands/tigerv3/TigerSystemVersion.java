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
 * Version number and git ref of application code.
 *
 * @author AndreR
 */
public class TigerSystemVersion extends ACommand
{
	@SerialData(type = ESerialDataType.UINT32)
	private long version = 0;

	@SerialData(type = ESerialDataType.UINT32)
	private long gitRef = 0;


	/**
	 * Constructor.
	 */
	public TigerSystemVersion()
	{
		super(ECommand.CMD_SYSTEM_VERSION);
	}


	/**
	 * @return Version string
	 */
	public String getVersionString()
	{
		long major = (version >> 24) & 0xFF;
		long minor = (version >> 16) & 0xFF;
		long patch = (version >> 8) & 0xFF;
		long dirty = version & 0xFF;

		return String.format("v%d.%d.%d%s", major, minor, patch, dirty > 0 ? "-dirty" : "");
	}


	/**
	 * @return the gitRef (first 4 byte of SHA1)
	 */
	public long getGitRef()
	{
		return gitRef;
	}


	/**
	 * Return a full version string with major.minor.patch[-dirty]-ref.
	 *
	 * @return
	 */
	public String getFullVersionString()
	{
		return getVersionString() + String.format("-g%08x", getGitRef());
	}

}
