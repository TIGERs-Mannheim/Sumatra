/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISkill
{
	
	/**
	 * @return
	 */
	ESkillName getSkillName();
	
	
	/**
	 * @param newWorldFrame
	 */
	void update(WorldFrame newWorldFrame);
	
	
	/**
	 * @return
	 */
	boolean isComplete();
	
	
	/**
	 * @param cmds
	 */
	void calcActions(List<ACommand> cmds);
	
	
	/**
	 * @param cmds
	 */
	void calcExitActions(List<ACommand> cmds);
	
	
	/**
	 * @param cmds
	 */
	void calcEntryActions(List<ACommand> cmds);
	
	
	/**
	 * @param observer
	 */
	void addObserver(ISkillEventObserver observer);
	
	
	/**
	 * @param observer
	 */
	void removeObserver(ISkillEventObserver observer);
	
	
	/**
	 * Does this skill need vision?
	 * 
	 * @return
	 */
	boolean needsVision();
	
	
	/**
	 * @return the bot
	 */
	ABot getBot();
	
	
	/**
	 * @param bot the bot to set
	 */
	void setBot(ABot bot);
	
	
	/**
	 * Careful: This is not called from SkillExecuter thread!!
	 * 
	 * @return
	 */
	default DrawablePath getDrawablePath()
	{
		return new DrawablePath();
	}
	
	
	/**
	 * Careful: This is not called from SkillExecuter thread!!
	 * 
	 * @return
	 */
	default DrawablePath getLatestDrawablePath()
	{
		return new DrawablePath();
	}
	
	
	/**
	 * Careful: This is not called from SkillExecuter thread!!
	 * 
	 * @return
	 */
	default int getNewPathCounter()
	{
		return 0;
	}
	
	
	/**
	 * @param skillSystem
	 */
	void setSkillSystem(ASkillSystem skillSystem);
	
	
	/**
	 * @param minDt
	 */
	void setMinDt(final float minDt);
}