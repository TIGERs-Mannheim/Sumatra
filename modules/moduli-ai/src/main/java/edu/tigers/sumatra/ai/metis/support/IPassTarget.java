/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;

import static edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;


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
	 * @return the position where the bot will position - might be an approximation
	 */
	IVector2 getBotPos();
	
	
	/**
	 * @return the associated bot's id
	 */
	BotID getBotId();
	
	
	/**
	 * @return the score of this pass target
	 */
	double getScore();
	
	
	/**
	 * @return the timestamp when this pass target will be reached
	 */
	long getTimeReached();
	
	
	/**
	 * @return the score for a direct shoot on the goal
	 */
	double getShootScore();
	
	
	/**
	 * @return the score for how likely it is that a pass is received
	 */
	double getReceiveScore();
	
	
	/**
	 * @return the kick mode for this pass target
	 */
	EKickMode getKickMode();
	
	
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
	 * @return the intermediate scores
	 */
	List<Double> getIntermediateScores();
	
	
	/**
	 * @param currentTimestamp
	 * @return time in s
	 */
	double getTimeUntilReachedInS(long currentTimestamp);
}
