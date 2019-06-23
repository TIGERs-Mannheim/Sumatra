/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISkill
{
	/**
	 * @return
	 */
	ESkill getType();
	
	
	/**
	 * @param wfw
	 * @param bot
	 */
	void update(WorldFrameWrapper wfw, ABot bot);
	
	
	/**
	 * @param timestamp
	 */
	void calcActions(long timestamp);
	
	
	/**
	 * Called once upon end of this skill
	 */
	void calcExitActions();
	
	
	/**
	 * Called once upon start of this skill
	 */
	void calcEntryActions();
	
	
	/**
	 * @return
	 */
	ShapeMap exportShapeMap();
	
	
	/**
	 * @return
	 */
	MovementCon getMoveCon();
	
	
	/**
	 * @return
	 */
	BotID getBotId();
	
	
	/**
	 * @return
	 */
	IState getCurrentState();
	
	
	/**
	 * @return
	 */
	boolean isInitialized();
	
	
	/**
	 * @param control
	 */
	void setMultimediaControl(final MultimediaControl control);
	
	
	/**
	 * Create bot ai infos based on current state
	 *
	 * @return new bot ai info structure
	 */
	BotAiInformation getBotAiInfo();
}
