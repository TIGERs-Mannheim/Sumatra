/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.finisher;

import java.util.List;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;


/**
 * A finisher move is a static movement procedure that can be used in certain situations, especially for goal kicks,
 * to perform some special kick or other movement
 */
public interface IFinisherMove
{
	/**
	 * If this finisher move is applicable in the current situation, it will return true
	 * 
	 * @param aiFrame the current frame
	 * @param botID
	 * @return if applicable
	 */
	boolean isApplicable(final BaseAiFrame aiFrame, final BotID botID);

	/**
	 * generates the trajectory / path for a specific robot
	 *
	 * @param aiFrame
	 * @param botID
	 */
	void generateTrajectory(final BaseAiFrame aiFrame, final BotID botID);

	
	/**
	 * Get the location where this finisher move would do the kick from, based on the current situation
	 * 
	 * @param aiFrame the current frame
	 * @return the kick location and orientation
	 */
	Pose getKickLocation(final BaseAiFrame aiFrame);
	
	
	/**
	 * Get the skill commands of this finisher move that execute this move
	 * 
	 * @return the list of skill commands
	 */
	List<SkillCommand> getSkillCommands();
	
	
	/**
	 * The name of the finisher move, based on the NATO alphabet
	 * 
	 * @return the name
	 */
	EFinisherMove getType();
	
	
	/**
	 * @return a list of shapes of this finisher move
	 */
	List<IDrawableShape> generateShapes();
}
