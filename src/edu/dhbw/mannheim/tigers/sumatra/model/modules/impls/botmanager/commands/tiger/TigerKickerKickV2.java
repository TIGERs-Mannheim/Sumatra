/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

public class TigerKickerKickV2 extends ACommand
{
	public static class Device
	{
		public static final int STRAIGHT = 0;
		public static final int CHIP = 1;
	}
	
	public static class Mode
	{
		public static final int FORCE = 0;
		public static final int ARM = 1;
		public static final int DISARM = 2;
		public static final int NONE = 3;
	}
	
	private int deviceAndMode;	// straight, chip // force, arm, disarm
	private int level;	// 0 - 240
	private int firingDuration;	// Fixed point 14:2 [s*e-7]
	
	public static final int MAX_LEVEL = 240;
	public static final int MAX_DURATION = 16383 | (3 << 14);
	
	private final Logger log = Logger.getLogger(getClass());
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerKickerKickV2()
	{
		setDevice(Device.STRAIGHT);
		setMode(Mode.NONE);
		setFiringDuration(0);
		setLevel(MAX_LEVEL);
	}

	public TigerKickerKickV2(float firingDuration, int mode)
	{
		setDevice(Device.STRAIGHT);
		setMode(mode);
		setFiringDuration(firingDuration);
		setLevel(MAX_LEVEL);
	}

	public TigerKickerKickV2(int device, int mode, float firingDuration)
	{
		setDevice(device);
		setMode(mode);
		setFiringDuration(firingDuration);
		setLevel(MAX_LEVEL);
	}

	public TigerKickerKickV2(int device, int mode, float firingDuration, int level)
	{
		setDevice(device);
		setMode(mode);
		setFiringDuration(firingDuration);
		setLevel(level);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, deviceAndMode);
		byte2ByteArray(data, 1, level);
		short2ByteArray(data, 2, firingDuration);
		
		return data;
	}
	
	public void setData(byte[] data)
	{
		deviceAndMode = byteArray2UByte(data, 0);
		level = byteArray2UByte(data, 1);
		firingDuration = byteArray2UShort(data, 2);
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_KICKV2;
	}
	
	@Override
	public int getDataLength()
	{
		return 4;
	}

	public int getDevice()
	{
		return deviceAndMode & 0x0F;
	}

	public void setDevice(int device)
	{
		this.deviceAndMode &= 0xF0;
		this.deviceAndMode |= device;
	}

	public int getMode()
	{
		return (deviceAndMode & 0xF0) >> 4;
	}

	public void setMode(int mode)
	{
		deviceAndMode &= 0x0F;
		deviceAndMode |= mode << 4;
	}

	public float getFiringDuration()
	{
		int duration = firingDuration & 0x3FFF;
		int exp = (firingDuration & 0xC000) >> 14;
		
		float result = (float) (duration * Math.pow(10, exp));
		result /= 10.0f;
		
		return result;
	}
	
	/**
	 * Set firing duration in [us]. One decimal place may be used.
	 * 
	 * @param firingDuration Duration in [us]
	 */
	public void setFiringDuration(float firingDuration)
	{
		if(firingDuration < 0)
		{
			log.warn("Firing Duration <= zero not permitted");
			return;
		}
		
		int duration = (int) (firingDuration*10);	// [s*e-7]
		int exp = 0;
		
		while(duration >= 16384)
		{
			duration /= 10;
			exp++;
		}
		
		if(exp > 3)
		{
			log.warn("Firing Duration of " + firingDuration + " too long. Set to MAX");
			this.firingDuration = MAX_DURATION;
		}
		else
		{
			this.firingDuration = duration | (exp << 14);
		}
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
		
		if(level > MAX_LEVEL)
		{
			log.warn("Level above " + MAX_LEVEL + ", cut off");
			this.level = MAX_LEVEL;
		}
		
		if(level == 0)
		{
			log.warn("Level 0 is not permitted, set to 1");
			this.level = 1;
		}
	}
}