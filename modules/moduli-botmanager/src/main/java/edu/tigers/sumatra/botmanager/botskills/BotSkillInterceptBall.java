/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;


import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Intercept an incoming ball near a desired target position.
 *
 * @author AndreR
 */
public class BotSkillInterceptBall extends ABotSkill
{
	private static final int UNUSED_FIELD = 0x7FFF;

	/**
	 * Desired position where to intercept the ball.
	 * This is a location in front of the robot, not its center.
	 * [mm, mrad]
	 */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private final int[] interceptPose = new int[3];

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

	@SerialData(type = SerialData.ESerialDataType.UINT16)
	private int moveRadius;


	/**
	 * Constructor.
	 */
	public BotSkillInterceptBall()
	{
		super(EBotSkill.INTERCEPT_BALL);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	public BotSkillInterceptBall(final IVector2 interceptPos, final double interceptOrient, final boolean usePose,
			final double moveRadius, final double velMax, final double velMaxW, final double accMax, final double accMaxW,
			final double kickSpeed, final EKickerDevice kickDevice, final EKickerMode kickMode)
	{
		this();

		if (usePose)
		{
			this.interceptPose[0] = (int) (interceptPos.x());
			this.interceptPose[1] = (int) (interceptPos.y());
			this.interceptPose[2] = (int) (interceptOrient * 1000.0);
		} else
		{
			this.interceptPose[0] = UNUSED_FIELD;
			this.interceptPose[1] = UNUSED_FIELD;
			this.interceptPose[2] = UNUSED_FIELD;
		}

		this.moveRadius = (int) (moveRadius);

		setVelMax(velMax);
		setVelMaxW(velMaxW);
		setAccMax(accMax);
		setAccMaxW(accMaxW);

		kickerDribbler.setKick(kickSpeed, kickDevice, kickMode);
	}


	/**
	 * Intercept, with move constraints.
	 */
	public BotSkillInterceptBall(final IVector2 interceptPos, final double interceptOrient, final double moveRadius,
			final IMoveConstraints mc)
	{
		this(interceptPos, interceptOrient, true, moveRadius, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(),
				mc.getAccMaxW(), 0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
	}


	/**
	 * Intercept, with move constraints, unlimited move radius.
	 */
	public BotSkillInterceptBall(final IMoveConstraints mc)
	{
		this(Vector2f.ZERO_VECTOR, 0, false, 0, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(),
				mc.getAccMaxW(), 0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
	}


	/**
	 * Set move radius for intercept.
	 *
	 * @param radius [mm]
	 */
	public void setMoveRadius(double radius)
	{
		moveRadius = (int) (radius);
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