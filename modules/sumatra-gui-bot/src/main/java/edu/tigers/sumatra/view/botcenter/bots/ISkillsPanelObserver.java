/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter.bots;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ASkill;


/**
 * TigerBotSkills observer.
 * 
 * @author AndreR
 */
public interface ISkillsPanelObserver
{
	/**
	 * @param x
	 * @param y
	 */
	void onMoveToXY(double x, double y);
	
	
	/**
	 * @param x
	 * @param y
	 * @param angle
	 */
	void onRotateAndMoveToXY(double x, double y, double angle);
	
	
	/**
	 * @param lookAtTarget
	 */
	void onLookAt(Vector2 lookAtTarget);
	
	
	/**
	 * @param rpm
	 */
	void onDribble(int rpm);
	
	
	/**
	 * @param skill
	 */
	void onSkill(ASkill skill);
	
	
	/**
	 * @param skill
	 */
	void onBotSkill(ABotSkill skill);
}
