/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;


/**
 * This enum will be used to differ between the
 * different kicker modes.
 * 
 * <strong>force</strong> = forces the bot to shoot ;
 * <strong>arm</strong> = the bot shoots when a ball is armed / detected ;
 * <strong>disarm</strong> = disarms ball
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public enum EKickMode
{
	/** */
	FORCE(TigerKickerKickV2.Mode.FORCE),
	/** */
	ARM(TigerKickerKickV2.Mode.ARM),
	/** */
	DISARM(TigerKickerKickV2.Mode.DISARM),
	/** */
	DRIBBLER(TigerKickerKickV2.Mode.DRIBBLER);
	
	/** */
	private final int	value;
	
	
	private EKickMode(int value)
	{
		this.value = value;
	}
	
	
	/**
	 * @return the value
	 */
	public final int getValue()
	{
		return value;
	}
}
