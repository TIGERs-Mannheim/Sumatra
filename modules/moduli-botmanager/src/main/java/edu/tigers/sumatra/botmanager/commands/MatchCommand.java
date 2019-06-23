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


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatchCommand implements IMatchCommand
{
	private ABotSkill skill = new BotSkillMotorsOff();
	private boolean autoCharge = false;
	private boolean strictVelocityLimit = false;
	private MultimediaControl multimediaControl = new MultimediaControl();
	
	
	@Override
	public void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
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
	 * @return the autoCharge
	 */
	public final boolean isAutoCharge()
	{
		return autoCharge;
	}
	
	
	@Override
	public void setMultimediaControl(final MultimediaControl control)
	{
		multimediaControl = control;
	}
	
	
	/**
	 * @return the multimediaControl
	 */
	public final MultimediaControl getMultimediaControl()
	{
		return multimediaControl;
	}
	
	
	@Override
	public void setStrictVelocityLimit(final boolean enable)
	{
		strictVelocityLimit = enable;
	}
	
	
	public final boolean isStrictVelocityLimit()
	{
		return strictVelocityLimit;
	}
}
