/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills.data;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.serial.SerialData;


public class DriveKickerDribbler
{
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	protected int velMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	protected int velMaxW = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	protected int accMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	protected int accMaxW = 0;

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	protected KickerDribblerCommands kickerDribbler = new KickerDribblerCommands();


	public KickerDribblerCommands getKickerDribbler()
	{
		return kickerDribbler;
	}


	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
		this.kickerDribbler = kickerDribbler;
	}


	public double getVelMax()
	{
		return DriveLimits.toDouble(velMax, DriveLimits.MAX_VEL);
	}


	public final void setVelMax(final double val)
	{
		velMax = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL);
	}


	public double getVelMaxW()
	{
		return DriveLimits.toDouble(velMaxW, DriveLimits.MAX_VEL_W);
	}


	public final void setVelMaxW(final double val)
	{
		velMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
	}


	public double getAccMax()
	{
		return DriveLimits.toDouble(accMax, DriveLimits.MAX_ACC);
	}


	public final void setAccMax(final double val)
	{
		accMax = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC);
	}


	public double getAccMaxW()
	{
		return DriveLimits.toDouble(accMaxW, DriveLimits.MAX_ACC_W);
	}


	public final void setAccMaxW(final double val)
	{
		accMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC_W);
	}


	public MoveConstraints getMoveConstraints()
	{
		MoveConstraints moveCon = new MoveConstraints();
		moveCon.setVelMax(getVelMax());
		moveCon.setVelMaxW(getVelMaxW());
		moveCon.setAccMax(getAccMax());
		moveCon.setAccMaxW(getAccMaxW());
		moveCon.setJerkMax(DriveLimits.MAX_JERK);
		moveCon.setJerkMaxW(DriveLimits.MAX_JERK_W);

		return moveCon;
	}
}
