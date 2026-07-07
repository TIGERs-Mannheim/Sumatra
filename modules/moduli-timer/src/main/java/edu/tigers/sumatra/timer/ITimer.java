package edu.tigers.sumatra.timer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITimer
{
	
	/**
	 * @param timable
	 * @param id
	 */
	void stop(String timable, long id);
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	void stop(String timable, long id, int customId);
	
	
	/**
	 * @param timable
	 * @param id
	 */
	void start(String timable, long id);
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	void start(String timable, long id, int customId);
}
