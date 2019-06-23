/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.math.Vector2;
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
	 * @param distance [mm]
	 * @param angle [rad]
	 */
	void onStraightMove(int distance, double angle);
	
	
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
