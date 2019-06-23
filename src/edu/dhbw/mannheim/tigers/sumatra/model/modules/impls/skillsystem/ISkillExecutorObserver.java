/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;

/**
 * Observer interface for the SkillExecutor
 * 
 * @author AndreR
 * 
 */
public interface ISkillExecutorObserver
{
	public void onNewCommand(ACommand command);
	public void onSkillStarted(ASkill skill);
	public void onSkillCompleted(ASkill skill);
}
