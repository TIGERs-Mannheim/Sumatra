/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.skills;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;


/**
 * TigerBotSkills observer.
 */
public interface ISkillsPanelObserver
{
	/**
	 * @param skill
	 */
	void onSkill(ASkill skill);
	
	
	/**
	 * @param skill
	 */
	void onBotSkill(ABotSkill skill);
}
