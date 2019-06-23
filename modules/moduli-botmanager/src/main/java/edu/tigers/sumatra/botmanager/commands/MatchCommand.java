/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatchCommand implements IMatchCommand
{
	private ABotSkill			skill							= new BotSkillMotorsOff();
	private double				dribbleSpeed				= 0;
	private double				kickSpeed					= 0;
	private EKickerDevice	device						= EKickerDevice.STRAIGHT;
	private EKickerMode		mode							= EKickerMode.DISARM;
	private boolean			cheer							= false;
	private int					feedbackFreq				= 20;
	private boolean			autoCharge					= false;
	
	private boolean			leftRed, leftGreen, rightRed, rightGreen;
	private boolean			setSongFinalCountdown	= false;
	
	
	@Override
	public void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
		setLEDs(true, false, true, false);
	}
	
	
	@Override
	public void setDribblerSpeed(final double speed)
	{
		dribbleSpeed = speed;
	}
	
	
	@Override
	public void setKick(final double kickSpeed, final EKickerDevice device, final EKickerMode mode)
	{
		this.kickSpeed = kickSpeed;
		this.device = device;
		this.mode = mode;
	}
	
	
	@Override
	public void setCheering(final boolean enable)
	{
		cheer = enable;
	}
	
	
	@Override
	public void setFeedbackFreq(final int freq)
	{
		feedbackFreq = freq;
	}
	
	
	@Override
	public void setKickerAutocharge(final boolean enable)
	{
		autoCharge = enable;
	}
	
	
	/**
	 * @return the skill
	 */
	@Override
	public final ABotSkill getSkill()
	{
		return skill;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public final double getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public final double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @return the device
	 */
	public final EKickerDevice getDevice()
	{
		return device;
	}
	
	
	/**
	 * @return the mode
	 */
	public final EKickerMode getMode()
	{
		return mode;
	}
	
	
	/**
	 * @return the cheer
	 */
	public final boolean isCheer()
	{
		return cheer;
	}
	
	
	/**
	 * @return the feedbackFreq
	 */
	public final int getFeedbackFreq()
	{
		return feedbackFreq;
	}
	
	
	/**
	 * @return the autoCharge
	 */
	public final boolean isAutoCharge()
	{
		return autoCharge;
	}
	
	
	@Override
	public void setLEDs(final boolean leftRed, final boolean leftGreen, final boolean rightRed, final boolean rightGreen)
	{
		this.leftGreen = leftGreen;
		this.rightGreen = rightGreen;
		this.leftRed = leftRed;
		this.rightRed = rightRed;
	}
	
	
	/**
	 * @return the leftRed
	 */
	public boolean isLeftRed()
	{
		return leftRed;
	}
	
	
	/**
	 * @param leftRed the leftRed to set
	 */
	public void setLeftRed(final boolean leftRed)
	{
		this.leftRed = leftRed;
	}
	
	
	/**
	 * @return the leftGreen
	 */
	public boolean isLeftGreen()
	{
		return leftGreen;
	}
	
	
	/**
	 * @param leftGreen the leftGreen to set
	 */
	public void setLeftGreen(final boolean leftGreen)
	{
		this.leftGreen = leftGreen;
	}
	
	
	/**
	 * @return the rightRed
	 */
	public boolean isRightRed()
	{
		return rightRed;
	}
	
	
	/**
	 * @param rightRed the rightRed to set
	 */
	public void setRightRed(final boolean rightRed)
	{
		this.rightRed = rightRed;
	}
	
	
	/**
	 * @return the rightGreen
	 */
	public boolean isRightGreen()
	{
		return rightGreen;
	}
	
	
	/**
	 * @param rightGreen the rightGreen to set
	 */
	public void setRightGreen(final boolean rightGreen)
	{
		this.rightGreen = rightGreen;
	}
	
	
	@Override
	public void setSongFinalCountdown(final boolean enable)
	{
		setSongFinalCountdown = enable;
	}
	
	
	/**
	 * @return the setSongFinalCountdown
	 */
	public boolean isSetSongFinalCountdown()
	{
		return setSongFinalCountdown;
	}
}
