package edu.tigers.autoreferee.remote;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.remote.impl.ThreadedTCPRefboxRemote;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;


/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 10, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */

/**
 * @author "Lukas Magel"
 */
public class RemoteTest
{
	
	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	@Ignore
	public void test() throws IOException, InterruptedException
	{
		ThreadedTCPRefboxRemote remote = new ThreadedTCPRefboxRemote("localhost", 10007);
		remote.start();
		for (int i = 0; i < 10; i++)
		{
			remote.sendCommand(new RefboxRemoteCommand(Command.DIRECT_FREE_BLUE));
			Thread.sleep(1000);
			remote.sendCommand(new RefboxRemoteCommand(Command.STOP));
			Thread.sleep(1000);
		}
		remote.stop();
	}
	
}
