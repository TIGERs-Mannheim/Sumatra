/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IMatchCommand
{
	
	/**
	 * @param skill
	 */
	void setSkill(ABotSkill skill);
	
	
	/**
	 * @param enable
	 */
	void setKickerAutocharge(final boolean enable);
	
	
	/**
	 * @return
	 */
	ABotSkill getSkill();
	
	
	/**
	 * @param control
	 */
	void setMultimediaControl(final MultimediaControl control);
	
	
	/**
	 * @param enable
	 */
	void setStrictVelocityLimit(final boolean enable);
}