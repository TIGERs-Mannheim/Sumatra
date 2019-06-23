/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands;

import org.junit.Test;


/**
 * @author AndreR
 */
public class AnnotationParserTest
{
	@Test
	public void parserTest()
	{
		BotSkillFactory.getInstance().loadSkills();
		CommandFactory.getInstance().loadCommands();
	}
}
