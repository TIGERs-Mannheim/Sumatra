/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;


import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
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

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int velMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int velMaxW = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMaxW = 0;

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	private KickerDribblerCommands kickerDribbler = new KickerDribblerCommands();

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int rotationSpeed;

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int dockSpeed;


	public BotSkillGetBall()
	{
		super(EBotSkill.GET_BALL);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	public BotSkillGetBall(final IVector2 searchOrigin, final double searchRadius,
			final double velMax, final double velMaxW, final double accMax, final double accMaxW,
			final double dribblerSpeed, final double rotationSpeed, final double dockSpeed)
	{
		this();

		this.searchOrigin[0] = (int) (searchOrigin.x());
		this.searchOrigin[1] = (int) (searchOrigin.y());
		this.searchRadius = (int) (searchRadius);

		setVelMax(velMax);
		setVelMaxW(velMaxW);
		setAccMax(accMax);
		setAccMaxW(accMaxW);

		kickerDribbler.setDribblerSpeed(dribblerSpeed);

		setRotationSpeed(rotationSpeed);
		setDockSpeed(dockSpeed);
	}


	/**
	 * Get ball, with move constraints.
	 */
	public BotSkillGetBall(final IVector2 searchOrigin, final double searchRadius,
			final IMoveConstraints mc,
			final double dribblerSpeed, final double rotationSpeed, final double dockSpeed)
	{
		this(searchOrigin, searchRadius, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(), mc.getAccMaxW(), dribblerSpeed,
				rotationSpeed, dockSpeed);
	}


	/**
	 * Get ball, with move constraints, unlimited search radius.
	 */
	public BotSkillGetBall(final IMoveConstraints mc, final double dribblerSpeed,
			final double rotationSpeed, final double dockSpeed)
	{
		this(Vector2f.ZERO_VECTOR, 0, mc, dribblerSpeed, rotationSpeed, dockSpeed);
	}


	public final void setDockSpeed(final double val)
	{
		dockSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL);
	}


	public final void setRotationSpeed(final double val)
	{
		rotationSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
	}


	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return kickerDribbler;
	}


	@Override
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


	@Override
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