/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AMoveBotSkill extends ABotSkill
{
	
	/**
	 * @param skill
	 */
	public AMoveBotSkill(final EBotSkill skill)
	{
		super(skill);
	}
	
	
	/**
	 * @param kickerDribbler the kickerDribbler to set
	 */
	public abstract void setKickerDribbler(final KickerDribblerCommands kickerDribbler);
	
	
	/**
	 * @return
	 */
	public abstract EDataAcquisitionMode getDataAcquisitionMode();
	
	
	/**
	 * @param acqMode
	 */
	public abstract void setDataAcquisitionMode(final EDataAcquisitionMode acqMode);
	
}
