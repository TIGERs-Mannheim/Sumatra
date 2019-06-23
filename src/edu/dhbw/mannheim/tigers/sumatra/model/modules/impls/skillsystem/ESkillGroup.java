/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

/**
 * Skill Group enum.
 * 
 * @author AndreR
 * 
 */
public enum ESkillGroup
{
	MOVE,
	DRIBBLE,
	/** Straigt and chip kick group */
	KICK;
	
	
	static int count()
	{
		return ESkillGroup.values().length;
	}
}
