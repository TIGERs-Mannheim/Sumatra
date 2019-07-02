package edu.tigers.sumatra.botmanager.commands;

import org.junit.Test;


public class CommandFactoryTest
{
	@Test
	public void parserTest()
	{
		CommandFactory.getInstance().loadCommands();
	}
}