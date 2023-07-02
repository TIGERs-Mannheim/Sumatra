/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.process;

import org.apache.commons.lang.SystemUtils;

import java.io.IOException;


public class ProcessKiller
{
	private final ProcessKillerWindows processKillerWindows = new ProcessKillerWindows();


	/**
	 * Kill a process with given name, if it is running.
	 *
	 * @param serviceName
	 * @throws IOException
	 */
	public void killProcess(String serviceName) throws IOException
	{
		// only implemented for Windows currently
		if (SystemUtils.IS_OS_WINDOWS)
		{
			processKillerWindows.killProcess(serviceName);
		}
	}
}
