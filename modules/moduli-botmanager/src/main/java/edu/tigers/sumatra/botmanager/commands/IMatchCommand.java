/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 30, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;


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
	 * @param freq
	 */
	void setFeedbackFreq(final int freq);
	
	
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
	 * @param acqMode
	 */
	void setDataAcquisitionMode(final EDataAcquisitionMode acqMode);
}