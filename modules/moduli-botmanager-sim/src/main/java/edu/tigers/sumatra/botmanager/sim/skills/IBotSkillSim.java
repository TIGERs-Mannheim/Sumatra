/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;


/**
 * Interface for implementing bot skills in Sumatra.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IBotSkillSim
{
	/**
	 * Called when bot skill is executed for the first time.
	 * 
	 * @param input
	 */
	default void init(final BotSkillInput input)
	{
	}
	
	
	/**
	 * Simulate this bot skill.
	 * 
	 * @param input current robot position/velocity/acceleration (simulated sensors)
	 * @return drive/kicker/dribbler output
	 */
	BotSkillOutput execute(final BotSkillInput input);
}
