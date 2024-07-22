/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveKickerDribbler;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author AndreR
 */
public class BotSkillKickBall extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private final int[] ballPos = new int[2];

	@SerialData(type = ESerialDataType.UINT16)
	private final int[] targetPosAndFieldSizeLSB = new int[2];

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	private DriveKickerDribbler driveKickerDribbler = new DriveKickerDribbler();

	@SerialData(type = ESerialDataType.UINT8)
	private int fieldSizeMSB = 0;


	public BotSkillKickBall()
	{
		super(EBotSkill.KICK_BALL);
	}


	public BotSkillKickBall(final IMoveConstraints mc)
	{
		this();

		setAccMax(mc.getAccMax());
		setAccMaxW(mc.getAccMaxW());
		setVelMax(mc.getVelMax());
		setVelMaxW(mc.getVelMaxW());
	}


	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return driveKickerDribbler.getKickerDribbler();
	}


	@Override
	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
		driveKickerDribbler.setKickerDribbler(kickerDribbler);
	}


	@Override
	public MoveConstraints getMoveConstraints()
	{
		return driveKickerDribbler.getMoveConstraints();
	}


	/**
	 * @param pos Ball position in [mm]
	 */
	public final void setBallPos(final IVector2 pos)
	{
		ballPos[0] = (int) pos.x();
		ballPos[1] = (int) pos.y();
	}


	/**
	 * @param pos Target position in [mm]
	 */
	public final void setTargetPos(final IVector2 pos)
	{
		int[] bits = new int[2];
		bits[0] = (int) pos.x();
		bits[1] = (int) pos.y();

		targetPosAndFieldSizeLSB[0] &= 0x0003;
		targetPosAndFieldSizeLSB[1] &= 0x0003;

		targetPosAndFieldSizeLSB[0] |= (bits[0] << 2);
		targetPosAndFieldSizeLSB[1] |= (bits[1] << 2);
	}


	/**
	 * @param size Field size in [mm]
	 */
	public final void setFieldSize(final IVector2 size)
	{
		int[] bits = new int[2];
		bits[0] = ((int) (size.x() * 4.0 * 0.001)) & 0x3F;
		bits[1] = ((int) (size.y() * 4.0 * 0.001)) & 0x3F;

		targetPosAndFieldSizeLSB[0] &= 0xFFFC;
		targetPosAndFieldSizeLSB[1] &= 0xFFFC;

		targetPosAndFieldSizeLSB[0] |= (bits[0] & 0x03);
		targetPosAndFieldSizeLSB[1] |= (bits[1] & 0x03);

		fieldSizeMSB = (bits[0] >> 2) | ((bits[1] << 2) & 0xF0);
	}


	public void setKickSpeed(double kickSpeed)
	{
		driveKickerDribbler.getKickerDribbler().setKickSpeed(kickSpeed);
	}


	public void setKickerDevice(EKickerDevice kickerDevice)
	{
		driveKickerDribbler.getKickerDribbler().setDevice(kickerDevice);
	}


	public void setKickerMode(EKickerMode kickerMode)
	{
		driveKickerDribbler.getKickerDribbler().setMode(kickerMode);
	}


	public void setDribblerSpeed(double speed)
	{
		driveKickerDribbler.getKickerDribbler()
				.setDribbler(speed, driveKickerDribbler.getKickerDribbler().getDribblerForce());
	}


	public void setDribblerForce(double force)
	{
		driveKickerDribbler.getKickerDribbler()
				.setDribbler(driveKickerDribbler.getKickerDribbler().getDribblerSpeed(), force);
	}


	public void setVelMax(double velMax)
	{
		driveKickerDribbler.setVelMax(velMax);
	}


	public void setVelMaxW(double velMaxW)
	{
		driveKickerDribbler.setVelMaxW(velMaxW);
	}


	public void setAccMax(double accMax)
	{
		driveKickerDribbler.setAccMax(accMax);
	}


	public void setAccMaxW(double accMaxW)
	{
		driveKickerDribbler.setAccMaxW(accMaxW);
	}
}
