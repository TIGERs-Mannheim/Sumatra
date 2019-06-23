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
import edu.tigers.sumatra.botmanager.commands.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatchCommand implements IMatchCommand
{
	private ABotSkill					skill					= new BotSkillMotorsOff();
	private int							feedbackFreq		= 20;
	private boolean					autoCharge			= false;
	private MultimediaControl		multimediaControl	= new MultimediaControl();
	private EDataAcquisitionMode	acqMode				= EDataAcquisitionMode.NONE;
	
	
	@Override
	public void setSkill(final ABotSkill skill)
	{
		switch (skill.getType())
		{
			case GLOBAL_POSITION:
			case GLOBAL_VELOCITY:
			case GLOBAL_VEL_XY_POS_W:
			case LOCAL_VELOCITY:
			case WHEEL_VELOCITY:
				AMoveBotSkill moveSkill = (AMoveBotSkill) skill;
				moveSkill.setDataAcquisitionMode(acqMode);
				break;
			default:
				break;
		}
		
		this.skill = skill;
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
	public void setDataAcquisitionMode(final EDataAcquisitionMode acqMode)
	{
		this.acqMode = acqMode;
	}
	
	
	/**
	 * @return
	 */
	public EDataAcquisitionMode getDataAcquisitionMode()
	{
		return acqMode;
	}
}
