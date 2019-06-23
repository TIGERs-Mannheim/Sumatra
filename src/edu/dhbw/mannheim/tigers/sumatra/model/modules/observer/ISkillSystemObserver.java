/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): Gero, AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;


/**
 * Notifies anyone who wants to know that this skill has been completed!
 * 
 * @author Gero, AndreR
 */
public interface ISkillSystemObserver
{
	/**
	 * Called after a skill is started.
	 * 
	 * @param skill The skill which state is now ACTIVE.
	 */
	public void onSkillStarted(ASkill skill, int botID);
	

	/**
	 * Called after a Skill is DONE.
	 * 
	 * @param skill Completed Skill.
	 */
	public void onSkillCompleted(ASkill skill, int botID);
}
