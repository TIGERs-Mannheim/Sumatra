package edu.tigers.sumatra.botmanager.commands;

import org.junit.Test;

import edu.tigers.sumatra.botmanager.botskills.BotSkillFactory;


public class BotSkillFactoryTest
{
	@Test
	public void parserTest()
	{
		BotSkillFactory.getInstance().loadSkills();
	}
}