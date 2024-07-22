/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.process;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Log4j2
public class ProcessKillerWindows
{
	private static final String[] TASKLIST = { "tasklist" };


	private boolean isProcessRunning(String serviceName) throws IOException
	{
		Process pro = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(pro.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (line.startsWith(serviceName))
			{
				return true;
			}
		}

		return false;
	}


	public void killProcess(String serviceName) throws IOException
	{
		if (isProcessRunning(serviceName))
		{
			ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", serviceName);
			pb.inheritIO();
			Process p = pb.start();
			try
			{
				p.waitFor();
			} catch (InterruptedException e)
			{
				log.warn("Interrupted while waiting to kill process", e);
				Thread.currentThread().interrupt();
			}
		}
	}
}
