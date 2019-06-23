/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.data.LedControl;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.skillsystem.driver.IPathDriver;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ShapeMap;
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
	 * This is only necessary for initial skill in SkillExecutor!
	 * 
	 * @param botId
	 */
	void setBotId(final BotID botId);
	
	
	/**
	 */
	void calcActions();
	
	
	/**
	 */
	void calcExitActions();
	
	
	/**
	 */
	void calcEntryActions();
	
	
	/**
	 * @return
	 */
	ShapeMap getShapes();
	
	
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
	 * @return
	 */
	IPathDriver getPathDriver();
	
	
	/**
	 * @return
	 */
	LedControl getLedControl();
	
	
	/**
	 * @param ledControl
	 */
	void setLedControl(final LedControl ledControl);
}