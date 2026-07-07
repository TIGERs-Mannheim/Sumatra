package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISkillSystemObserver
{
	/**
	 * @param bot
	 * @param timestamp
	 */
	default void onCommandSent(final ABot bot, final long timestamp)
	{
	}
}
