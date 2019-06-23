/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.commands.botskills.data;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * @author AndreR
 */
public class KickerDribblerCommands
{
	private static final Logger	log					= Logger.getLogger(KickerDribblerCommands.class.getName());
	
	/** [0.04 m/s] */
	@SerialData(type = ESerialDataType.UINT8)
	private int							kickSpeed;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							kickFlags;
	
	/** [rpm/100] */
	@SerialData(type = ESerialDataType.UINT8)
	private int							dribblerSpeed;
	
	private double						origKickSpeed		= 0;
	private EKickerDevice			device				= EKickerDevice.STRAIGHT;
	private EKickerMode				mode					= EKickerMode.DISARM;
	private double						origDribblerSpeed	= 0;
	
	
	/**
	 * @param speed Dribbler speed in RPM.
	 * @note Speed must always be positive.
	 */
	public void setDribblerSpeed(final double speed)
	{
		origDribblerSpeed = speed;
		if (speed < 0)
		{
			dribblerSpeed = 0;
		} else
		{
			dribblerSpeed = ((int) (speed + 50.0)) / 100;
		}
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param kickSpeed [m/s] (max. 10.2)
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	public void setKick(final double kickSpeed, final EKickerDevice device, final EKickerMode mode)
	{
		if (kickSpeed < 0)
		{
			log.warn("Kickspeed is < 0: " + kickSpeed);
			origKickSpeed = 0;
		} else if (kickSpeed > 10)
		{
			log.warn("Kickspeed is > 10: " + kickSpeed);
			origKickSpeed = 10;
		} else
		{
			origKickSpeed = Math.min(8, Math.max(0, kickSpeed));
		}
		this.device = device;
		this.mode = mode;
		setKick(origKickSpeed, device.getValue(), mode.getId());
	}
	
	
	/**
	 * Set kick details.
	 * 
	 * @param kickSpeed [m/s] (max. 10.2)
	 * @param device STRAIGHT or CHIP
	 * @param mode FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	private void setKick(final double kickSpeed, final int device, final int mode)
	{
		if (mode == EKickerMode.ARM_TIME.getId())
		{
			// kick speed is interpreted as kick time in [s] (max. 20480us, 10us steps)
			int timeIn10Us = (int) (kickSpeed * 1e5);
			if (timeIn10Us > 2047)
			{
				timeIn10Us = 2047;
			}
			
			this.kickSpeed = timeIn10Us & 0xFF;
			kickFlags = (timeIn10Us & 0x700) >> 7;
			kickFlags |= device | (mode << 4);
		} else
		{
			this.kickSpeed = (int) ((kickSpeed / 0.04) + 0.5);
			kickFlags = device | (mode << 4);
		}
	}
	
	
	/**
	 * @return the origKickSpeed
	 */
	public final double getKickSpeed()
	{
		return origKickSpeed;
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
	 * @return the origDribblerSpeed
	 */
	public final double getDribblerSpeed()
	{
		return origDribblerSpeed;
	}
}
