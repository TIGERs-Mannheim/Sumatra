/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * A pass target is a position to which a ball can be passed.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPassTarget extends Comparable<IPassTarget>
{
	/**
	 * @return the target position
	 */
	IVector2 getKickerPos();
	
	
	/**
	 * @return the target as a {@link DynamicPosition}
	 */
	DynamicPosition getDynamicTarget();
	
	
	/**
	 * @return the position where the bot will position - might be an approximation
	 */
	IVector2 getBotPos();
	
	
	/**
	 * @return the associated bot's id
	 */
	BotID getBotId();
	
	
	/**
	 * @return the timestamp when this pass target will be reached
	 */
	long getTimeReached();
	
	
	/**
	 * @return the score of this pass target in [0..1] where 1 is best
	 */
	double getScore();
	
	
	/**
	 * @return the score for a goal kick in [0..1] where 1 is best
	 */
	double getGoalKickScore();
	
	
	/**
	 * @return the score for how likely it is that a pass is received in [0..1] where 1 is best
	 */
	double getPassScore();
	
	
	/**
	 * @param passTarget
	 * @return true, if both passTargets are similar
	 */
	boolean isSimilarTo(IPassTarget passTarget);
	
	
	/**
	 * @return the timestamp when this pass target was born
	 */
	long getBirth();
	
	
	/**
	 * Calculate the time from given timestamp until pass target is reached
	 * 
	 * @param currentTimestamp
	 * @return time in s
	 */
	double getTimeUntilReachedInS(long currentTimestamp);
	
	
	/**
	 * @param currentTimestamp
	 * @return the current age of this pass target
	 */
	double getAge(long currentTimestamp);
	
	
	/**
	 * @return the range [rad] in which the pass can be played
	 */
	double getPassRange();
}
