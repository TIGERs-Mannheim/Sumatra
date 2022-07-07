/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills.data;

import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author AndreR
 */
public class KickerDribblerCommands
{
	private static final Logger log = LogManager.getLogger(KickerDribblerCommands.class.getName());

	/**
	 * [0.04 m/s]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int kickSpeed;

	@SerialData(type = ESerialDataType.UINT8)
	private int flags;

	@SerialData(type = ESerialDataType.UINT8)
	private int dribbler = 0x30;

	private double origKickSpeed = 0;
	private EKickerDevice device = EKickerDevice.STRAIGHT;
	private EKickerMode mode = EKickerMode.DISARM;
	private double origDribblerSpeed = 0;
	private double origDribblerMaxCurrent = 3.0;


	/**
	 * @param speed Dribbler speed in RPM.
	 * @note Speed must always be positive and can only represent steps of 1000 RPM.
	 */
	private void setDribblerSpeed(final double speed)
	{
		origDribblerSpeed = speed;

		if (speed > 0)
		{
			int bits = ((int) (speed + 500.0)) / 1000;

			dribbler &= 0xF0;
			dribbler |= (bits >> 1) & 0x0F;

			if (mode != EKickerMode.ARM_TIME)
			{
				flags &= 0xFD;
				flags |= (bits & 0x01) << 1;
			}
		}
	}


	/**
	 * @param maxCurrent Dribbler maximum current in [A].
	 * @note Current ranges from 0.0 - 15.5A and can only represent steps of 0.5A.
	 */
	private void setDribblerMaxCurrent(final double maxCurrent)
	{
		origDribblerMaxCurrent = maxCurrent;

		if (maxCurrent > 0)
		{
			int bits = (int) ((maxCurrent + 0.25) * 2.0);

			dribbler &= 0x0F;
			dribbler |= (bits << 3) & 0xF0;

			if (mode != EKickerMode.ARM_TIME)
			{
				flags &= 0xFB;
				flags |= (bits & 0x01) << 2;
			}
		}
	}


	/**
	 * Set dribbler details.
	 *
	 * @param speed      [rpm], 0 - 31000, steps 1000
	 * @param maxCurrent [A], 0.0 - 15.5, steps 0.5
	 */
	public void setDribbler(final double speed, final double maxCurrent)
	{
		setDribblerSpeed(speed);
		setDribblerMaxCurrent(maxCurrent);
	}


	/**
	 * Set kick details.
	 *
	 * @param kickSpeed [m/s] (max. 10.2)
	 * @param device    STRAIGHT or CHIP
	 * @param mode      FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	public void setKick(final double kickSpeed, final EKickerDevice device, final EKickerMode mode)
	{
		this.device = device;
		this.mode = mode;
		setKickSpeed(kickSpeed);
	}


	public void setKickSpeed(final double kickSpeed)
	{
		if (kickSpeed < 0)
		{
			log.warn("Kickspeed is < 0: " + kickSpeed);
			origKickSpeed = 0;
		} else if (kickSpeed > 10 && mode != EKickerMode.ARM_TIME)
		{
			log.warn("Kickspeed is > 10: " + kickSpeed);
			origKickSpeed = 10;
		} else
		{
			origKickSpeed = kickSpeed;
		}
		setKick(origKickSpeed, device.getValue(), mode.getId());
	}


	public void setDevice(final EKickerDevice device)
	{
		this.device = device;
		setKick(origKickSpeed, device.getValue(), mode.getId());
	}


	public void setMode(final EKickerMode mode)
	{
		this.mode = mode;
		setKick(origKickSpeed, device.getValue(), mode.getId());
	}


	/**
	 * Set kick details.
	 *
	 * @param kickSpeed [m/s] (max. 10.2)
	 * @param device    STRAIGHT or CHIP
	 * @param mode      FORCE, ARM or DISARM
	 * @note This command requires a state-aware instance to keep track of older values.
	 */
	private void setKick(final double kickSpeed, final int device, final int mode)
	{
		if (mode == EKickerMode.ARM_TIME.getId())
		{
			// kick speed is interpreted as kick time in [ms] (max. 20480us, 10us steps)
			int timeIn10Us = (int) (kickSpeed * 1e2);
			if (timeIn10Us > 2047)
			{
				timeIn10Us = 2047;
			}

			this.kickSpeed = timeIn10Us & 0xFF;
			flags = (timeIn10Us & 0x700) >> 7;
			flags |= device | (mode << 4);
		} else
		{
			this.kickSpeed = (int) ((kickSpeed / 0.04) + 0.5);
			flags &= 0x0E;
			flags |= device | (mode << 4);
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


	/**
	 * @return the origDribblerMaxCurrent
	 */
	public final double getDribblerMaxCurrent()
	{
		return origDribblerMaxCurrent;
	}
}
