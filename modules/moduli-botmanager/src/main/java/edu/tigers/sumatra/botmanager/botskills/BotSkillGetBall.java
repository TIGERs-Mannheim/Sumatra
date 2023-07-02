/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;


import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveKickerDribbler;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Find a ball with onboard camera and get it on the dribbler.
 *
 * @author AndreR
 */
public class BotSkillGetBall extends ABotSkill
{

	@SerialData(type = SerialData.ESerialDataType.INT16)
	private final int[] searchOrigin = new int[2];

	@SerialData(type = SerialData.ESerialDataType.UINT16)
	private int searchRadius;

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	private DriveKickerDribbler driveKickerDribbler = new DriveKickerDribbler();

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int rotationSpeed;

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int dockSpeed;

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int aimSpeed;


	public BotSkillGetBall()
	{
		super(EBotSkill.GET_BALL);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	public BotSkillGetBall(final IVector2 searchOrigin, final double searchRadius,
			final double velMax, final double velMaxW, final double accMax, final double accMaxW,
			final double dribblerSpeed, final double dribblerCurrent, final double rotationSpeed, final double dockSpeed,
			final double aimSpeed)
	{
		this();

		this.searchOrigin[0] = (int) (searchOrigin.x());
		this.searchOrigin[1] = (int) (searchOrigin.y());
		this.searchRadius = (int) (searchRadius);

		driveKickerDribbler.setVelMax(velMax);
		driveKickerDribbler.setVelMaxW(velMaxW);
		driveKickerDribbler.setAccMax(accMax);
		driveKickerDribbler.setAccMaxW(accMaxW);

		driveKickerDribbler.getKickerDribbler().setDribbler(dribblerSpeed, dribblerCurrent);

		setRotationSpeed(rotationSpeed);
		setDockSpeed(dockSpeed);
		setAimSpeed(aimSpeed);
	}


	/**
	 * Get ball, with move constraints.
	 */
	public BotSkillGetBall(final IVector2 searchOrigin, final double searchRadius,
			final IMoveConstraints mc,
			final double dribblerSpeed, final double dribblerCurrent, final double rotationSpeed, final double dockSpeed)
	{
		this(searchOrigin, searchRadius, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(), mc.getAccMaxW(), dribblerSpeed,
				dribblerCurrent, rotationSpeed, dockSpeed, 0);
	}


	/**
	 * Get ball, with move constraints, unlimited search radius.
	 */
	public BotSkillGetBall(final IMoveConstraints mc, final double dribblerSpeed, final double dribblerCurrent,
			final double rotationSpeed, final double dockSpeed)
	{
		this(Vector2f.ZERO_VECTOR, 0, mc, dribblerSpeed, dribblerCurrent, rotationSpeed, dockSpeed);
	}


	public final void setDockSpeed(final double val)
	{
		dockSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL);
	}


	public final void setRotationSpeed(final double val)
	{
		rotationSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
	}


	public final void setAimSpeed(final double val)
	{
		aimSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
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
}