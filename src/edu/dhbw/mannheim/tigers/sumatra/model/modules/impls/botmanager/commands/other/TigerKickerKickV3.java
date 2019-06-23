/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerKickerKickV3 extends ACommand
{
	private final EKickerMode		mode;
	private final EKickerDevice	device;
	private final float				kickSpeed;
	private final int					duration;
	
	
	/**
	 * 
	 */
	public TigerKickerKickV3()
	{
		this(EKickerMode.DISARM, EKickerDevice.STRAIGHT, 0, 0);
	}
	
	
	/**
	 * @param mode
	 * @param device
	 * @param kickSpeed
	 * @param duration
	 */
	public TigerKickerKickV3(final EKickerMode mode, final EKickerDevice device, final float kickSpeed,
			final int duration)
	{
		super(ECommand.CMD_KICKER_KICKV3);
		this.mode = mode;
		this.device = device;
		this.kickSpeed = kickSpeed;
		this.duration = duration;
	}
	
	
	/**
	 * @return the mode
	 */
	public final EKickerMode getMode()
	{
		return mode;
	}
	
	
	/**
	 * @return the device
	 */
	public final EKickerDevice getDevice()
	{
		return device;
	}
	
	
	/**
	 * @return the kickSpeed in [m/s]
	 */
	public final float getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @return the duration
	 */
	public final int getDuration()
	{
		return duration;
	}
}
