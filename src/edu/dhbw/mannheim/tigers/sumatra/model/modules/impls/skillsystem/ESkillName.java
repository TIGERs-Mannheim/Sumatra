/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

/**
 * An enumeration that uniquely identifies each {@link ASkill}-implementation
 * 
 * @author Gero
 */
public enum ESkillName
{
	DUMMY,
	
	// Stop skills
	IMMEDIATE_STOP,
	IMMEDIATE_DISARM,
	IMMEDIATE_STOP_DRIBBLER,
	
	// Dribble skills
	DRIBBLE_BALL,
	
	// Kick skill
	KICK_BALL,
	KICK_AUTO,
	
	// Move skills
	STRAIGHT_MOVE,
	MOVE_DATA_COLLECTOR,
	
	MOVE_FIXED_GLOBAL_ORIENTATION,
	MOVE_FIXED_CURRENT_ORIENTATION,
	MOVE_DYNAMIC_TARGET,
	MOVE_FIXED_TARGET,
	MOVE_AHEAD,
	SINUS,
	// MOVE_IN_CIRCLE,
	AIM,
	DIRECT_MOVE,
	BALL_MOVE,
	BALL_MOVE_V2,
	MOVE_ON_PATH,
	MOVE_FAST,
	MOVE_V2,
	GET_BALL_AND_AIM,
}
