/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;


/**
 * TigerBotSkills observer.
 * 
 * @author AndreR
 * 
 */
public interface ISkillsPanelObserver
{
	/**
	 * 
	 * @param x
	 * @param y
	 */
	void onMoveToXY(float x, float y);
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param angle
	 */
	void onRotateAndMoveToXY(float x, float y, float angle);
	
	
	/**
	 * 
	 * @param distance [mm]
	 * @param angle [rad]
	 */
	void onStraightMove(int distance, float angle);
	
	
	/**
	 * 
	 * @param targetAngle
	 */
	void onRotate(float targetAngle);
	
	
	/**
	 * 
	 * @param kicklength
	 * @param device
	 */
	void onKick(float kicklength, EKickDevice device);
	
	
	/**
	 * 
	 * @param lookAtTarget
	 */
	void onLookAt(Vector2 lookAtTarget);
	
	
	/**
	 * 
	 * @param rpm
	 */
	void onDribble(int rpm);
	
	
	/**
	 * 
	 * @param skill
	 */
	void onSkill(AMoveSkill skill);
}
