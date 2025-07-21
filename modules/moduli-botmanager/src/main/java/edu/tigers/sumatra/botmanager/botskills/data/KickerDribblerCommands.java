/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills.data;

import edu.tigers.sumatra.botmanager.serial.SerialByteConverter;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import lombok.extern.log4j.Log4j2;


/**
 * Kicker and dribbler commands.
 */
@Log4j2
public class KickerDribblerCommands
{
	@SerialData(type = ESerialDataType.INT8)
	private final byte[] data = new byte[3];


	/**
	 * @param speed Dribbling bar surface speed in m/s.
	 * @note Speed must always be positive and can only represent steps of 0.125m/s. Maximum is 7.875m/s.
	 */
	private void setDribblerSpeed(final double speed)
	{
		double checkedSpeed;
		if (speed > 7.875)
		{
			log.warn("dribble speed is > 7.875: {}", speed, new Exception());
			checkedSpeed = 7.875;
		} else if (speed < 0)
		{
			log.warn("dribble speed is < 0: {}", speed, new Exception());
			checkedSpeed = 0;
		} else
		{
			checkedSpeed = speed;
		}

		int speedBits = (int) (checkedSpeed / 0.125 + 0.5);
		SerialByteConverter.packBits(data, 12, 6, speedBits);
	}


	/**
	 * @param force Dribbler maximum force in [N].
	 * @note Force ranges from 0.0 - 15.75N and can only represent steps of 0.25N.
	 */
	private void setDribblerForce(final double force)
	{
		double checkedForce;
		if (force > 15.75)
		{
			log.warn("dribble force is > 15.75: {}", force, new Exception());
			checkedForce = 15.75;
		} else if (force < 0)
		{
			log.warn("dribble force is < 0: {}", force, new Exception());
			checkedForce = 0;
		} else
		{
			checkedForce = force;
		}

		int forceBits = (int) (checkedForce / 0.25 + 0.5);
		SerialByteConverter.packBits(data, 18, 6, forceBits);
	}


	/**
	 * Set dribbler details.
	 *
	 * @param speed [m/s], 0 - 7.785, steps 0.125
	 * @param force [N], 0.0 - 15.75, steps 0.25
	 */
	public void setDribbler(final double speed, final double force)
	{
		setDribblerSpeed(speed);
		setDribblerForce(force);
	}


	/**
	 * Set kick details.
	 *
	 * @param kickValue Kick speed in [m/s] (max. 10.22) or kick time in [ms] (max. 12.775)
	 * @param device    STRAIGHT or CHIP
	 * @param mode      FORCE, ARM, DISARM OR ARM_TIME
	 * @note When using ARM_TIME the kickValue is interpreted as [ms].
	 */
	public void setKick(final double kickValue, final EKickerDevice device, final EKickerMode mode)
	{
		setKick(kickValue, device.getValue(), mode.getId());
	}


	public void setKickSpeed(final double kickSpeed)
	{
		setKick(kickSpeed, getDevice().getValue(), getMode().getId());
	}


	public void setDevice(final EKickerDevice device)
	{
		setKick(getKickSpeed(), device.getValue(), getMode().getId());
	}


	public void setMode(final EKickerMode mode)
	{
		setKick(getKickSpeed(), getDevice().getValue(), mode.getId());
	}


	private void setKick(final double kickValue, final int device, final int mode)
	{
		double checkedKickValue;
		if (kickValue < 0)
		{
			log.warn("Kick value is < 0: {}", kickValue, new Exception());
			checkedKickValue = 0;
		} else if (kickValue > 10.22 && mode != EKickerMode.ARM_TIME.getId())
		{
			log.warn("Kick speed is > 10.22: {}", kickValue, new Exception());
			checkedKickValue = 10.22;
		} else if (kickValue > 12.775)
		{
			log.warn("Kick time is > 12.775: {}", kickValue, new Exception());
			checkedKickValue = 12.775;
		} else
		{
			checkedKickValue = kickValue;
		}

		if (mode == EKickerMode.ARM_TIME.getId())
		{
			int kickTimeBits = (int) (checkedKickValue / 0.025 + 0.5);
			SerialByteConverter.packBits(data, 0, 9, kickTimeBits);
		} else
		{
			int kickSpeedBits = (int) (checkedKickValue / 0.02 + 0.5);
			SerialByteConverter.packBits(data, 0, 9, kickSpeedBits);
		}

		SerialByteConverter.packBits(data, 9, 1, device);
		SerialByteConverter.packBits(data, 10, 2, mode);
	}


	public final double getKickSpeed()
	{
		int kickBits = SerialByteConverter.unpackBits(data, 0, 9);

		if (getMode() == EKickerMode.ARM_TIME)
		{
			return kickBits * 0.025;
		}

		return kickBits * 0.02;
	}


	public final EKickerDevice getDevice()
	{
		int deviceBits = SerialByteConverter.unpackBits(data, 9, 1);
		return EKickerDevice.fromValue(deviceBits);
	}


	public final EKickerMode getMode()
	{
		int modeBits = SerialByteConverter.unpackBits(data, 10, 2);
		return EKickerMode.fromId(modeBits);
	}


	/**
	 * @return the dribblerSpeed [m/s]
	 */
	public final double getDribblerSpeed()
	{
		int speedBits = SerialByteConverter.unpackBits(data, 12, 6);
		return speedBits * 0.125;
	}


	/**
	 * @return the dribblerForce [N]
	 */
	public final double getDribblerForce()
	{
		int forceBits = SerialByteConverter.unpackBits(data, 18, 6);
		return forceBits * 0.25;
	}
}
