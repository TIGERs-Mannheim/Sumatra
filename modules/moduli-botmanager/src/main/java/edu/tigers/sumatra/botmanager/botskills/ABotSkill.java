/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;


/**
 */
public abstract class ABotSkill
{
	private final EBotSkill skill;
	
	
	protected ABotSkill(final EBotSkill skill)
	{
		this.skill = skill;
	}
	
	
	/**
	 * @return
	 */
	public EBotSkill getType()
	{
		return skill;
	}
	
	
	/**
	 * @return kick dribbler output
	 */
	public KickerDribblerCommands getKickerDribbler()
	{
		return new KickerDribblerCommands();
	}
	
	
	/**
	 * @return the kick speed
	 */
	public double getKickSpeed()
	{
		return getKickerDribbler().getKickSpeed();
	}
	
	
	/**
	 * @return the kicker mode
	 */
	public EKickerMode getMode()
	{
		return getKickerDribbler().getMode();
	}
	
	
	/**
	 * @return the kicker device
	 */
	public EKickerDevice getDevice()
	{
		return getKickerDribbler().getDevice();
	}
	
	
	/**
	 * @return the dribble speed
	 */
	public double getDribbleSpeed()
	{
		return getKickerDribbler().getDribblerSpeed();
	}
	
	
	public MoveConstraints getMoveConstraints()
	{
		// return dummy instance for compatibility
		return new MoveConstraints(new BotMovementLimits());
	}
	
	
	/**
	 * @param kickerDribbler the kickerDribbler to set
	 */
	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
	}
}
