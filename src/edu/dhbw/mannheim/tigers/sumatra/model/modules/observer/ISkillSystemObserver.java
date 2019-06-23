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

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;


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
	 * @param botID
	 */
	void onSkillStarted(ASkill skill, BotID botID);
	
	
	/**
	 * Called after a Skill is DONE.
	 * 
	 * @param skill Completed Skill.
	 * @param botID
	 */
	void onSkillCompleted(ASkill skill, BotID botID);
}
