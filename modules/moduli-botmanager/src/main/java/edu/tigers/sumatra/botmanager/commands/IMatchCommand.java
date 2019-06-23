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
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;


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
	 * @param speed Dribbler speed in RPM.
	 * @note Negative values reverse turning direction (push ball away).
	 */
	void setDribblerSpeed(double speed);
	
	
	/**
	 * Set kick details.
	 * 
	 * @param kickSpeed [m/s]
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 */
	void setKick(double kickSpeed, EKickerDevice device, EKickerMode mode);
	
	
	/**
	 * Enable the robots super-mega-top-secret and ultra-annoying cheering functionality.
	 * 
	 * @param enable
	 * @note Expect severe joy and enthusiasm in the crowd!
	 */
	void setCheering(boolean enable);
	
	
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
	 * @param leftRed
	 * @param leftGreen
	 * @param rightRed
	 * @param rightGreen
	 */
	void setLEDs(final boolean leftRed, final boolean leftGreen, final boolean rightRed, final boolean rightGreen);
	
	
	/**
	 * @param enable
	 */
	void setSongFinalCountdown(final boolean enable);
}