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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;


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
	void setWorldFrame(WorldFrame newWorldFrame);
	
	
	/**
	 * @return
	 */
	boolean isComplete();
	
	
	/**
	 * @return
	 */
	List<ACommand> calcActions();
	
	
	/**
	 * @param cmds
	 * @return
	 */
	List<ACommand> calcActions(List<ACommand> cmds);
	
	
	/**
	 * @return
	 */
	List<ACommand> calcExitActions();
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	List<ACommand> calcExitActions(List<ACommand> cmds);
	
	
	/**
	 * @return
	 */
	List<ACommand> calcEntryActions();
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	List<ACommand> calcEntryActions(List<ACommand> cmds);
	
	
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
	 * @param tBot the tBot to set
	 */
	void settBot(TrackedTigerBot tBot);
	
	
	/**
	 * @param sisyphus
	 */
	void setSisyphus(Sisyphus sisyphus);
	
	
	/**
	 * @param period [ns]
	 */
	void setDt(final long period);
}