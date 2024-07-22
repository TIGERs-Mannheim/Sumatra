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
	private double origDribblerForce = 3.0;


	/**
	 * @param speed Dribbling bar surface speed in m/s.
	 * @note Speed must always be positive and can only represent steps of 0.25m/s.
	 */
	private void setDribblerSpeed(final double speed)
	{
		if(speed > 7.75)
		{
			log.warn("dribble speed is > 7.75: {}", speed, new Exception());
			origDribblerSpeed = 7.75;
		}
		else if (speed >= 0)
		{
			origDribblerSpeed = speed;
		}
		else
		{
			log.warn("dribble speed is < 0: {}", speed, new Exception());
			origDribblerSpeed = 0;
		}

		int bits = (int) (origDribblerSpeed * 4.0 + 0.5);

		dribbler &= 0xF0;
		dribbler |= (bits >> 1) & 0x0F;

		if (mode != EKickerMode.ARM_TIME)
		{
			flags &= 0xFD;
			flags |= (bits & 0x01) << 1;
		}
	}


	/**
	 * @param force Dribbler maximum force in [N].
	 * @note Force ranges from 0.0 - 15.5N and can only represent steps of 0.5N.
	 */
	private void setDribblerForce(final double force)
	{
		if(force > 15.5)
		{
			log.warn("dribble force is > 15.5: {}", force, new Exception());
			origDribblerForce = 15.5;
		}
		else if (force >= 0)
		{
			origDribblerForce = force;
		}
		else
		{
			log.warn("dribble force is < 0: {}", force, new Exception());
			origDribblerForce = 0;
		}

		int bits = (int) (origDribblerForce * 2.0 + 0.5);

		dribbler &= 0x0F;
		dribbler |= (bits << 3) & 0xF0;

		if (mode != EKickerMode.ARM_TIME)
		{
			flags &= 0xFB;
			flags |= (bits & 0x01) << 2;
		}
	}


	/**
	 * Set dribbler details.
	 *
	 * @param speed [m/s], 0 - 7.75, steps 0.25
	 * @param force [N], 0.0 - 15.5, steps 0.5
	 */
	public void setDribbler(final double speed, final double force)
	{
		setDribblerSpeed(speed);
		setDribblerForce(force);
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
			log.warn("Kickspeed is < 0: {}", kickSpeed, new Exception());
			origKickSpeed = 0;
		} else if (kickSpeed > 10 && mode != EKickerMode.ARM_TIME)
		{
			log.warn("Kickspeed is > 10: {}", kickSpeed, new Exception());
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
		} else
		{
			this.kickSpeed = (int) ((kickSpeed / 0.04) + 0.5);
			flags &= 0x0E;
		}

		flags |= device | (mode << 4);
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
	 * @return the origDribblerSpeed [m/s]
	 */
	public final double getDribblerSpeed()
	{
		return origDribblerSpeed;
	}


	/**
	 * @return the origDribblerMaxCurrent
	 */
	public final double getDribblerForce()
	{
		return origDribblerForce;
	}
}
