/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;


/**
 * Observer interface for the SkillExecutor
 * 
 * @author AndreR
 */
public interface ISkillExecutorObserver
{
	/**
	 * @param command
	 */
	void onNewCommand(ACommand command);
	
	
	/**
	 * @param command
	 */
	void onNewMatchCommand(List<ACommand> command);
	
	
	/**
	 * @param skill
	 */
	void onSkillStarted(ISkill skill);
	
	
	/**
	 * @param skill
	 */
	void onSkillCompleted(ISkill skill);
	
	
	/**
	 * @param skill
	 */
	void onSkillCompletedItself(final ISkill skill);
	
	
	/**
	 * Called after each iteration of {@link SkillExecutor}
	 * 
	 * @param skill
	 */
	void onSkillProcessed(ISkill skill);
}
