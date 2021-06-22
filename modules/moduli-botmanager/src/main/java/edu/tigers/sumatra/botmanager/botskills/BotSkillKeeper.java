/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Bot skill for the keeper to protect the goal against the ball.
 * From Firmware:
 * ballPosX int16_t [mm] // 2
 * ballPoxY int16_t [mm] // 2
 * ballVelX int16_t [mm/s] // 2
 * ballVelY int16_t [mm/s] // 2
 * penAreaDepth uint8_t [cm] // 1
 * goalWidth uint8_t [cm] // 1
 * goalOffset int16_t [mm] // 2
 * velMaxXY uint8_t // 1
 * velMaxW uint8_t // 1
 * accMaxXY uint8_t // 1
 * accMaxW uint8_t // 1
 */
@SuppressWarnings("MismatchedReadAndWriteOfArray") // FP, fields are read by reflection
public class BotSkillKeeper extends ABotSkill
{
	/** Position of the ball or any artificial protection point [mm] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private final int[] ballPos = new int[2];

	/** Velocity of the ball [mm/s] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private final int[] ballVel = new int[2];

	/** Depth of the penalty area [cm] */
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int penAreaDepth = 0;
	/** Width of the goal [cm] */
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int goalWidth = 0;
	/** Distance from field center to goal line [mm] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int goalOffset = 0;

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int velMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int velMaxW = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMax = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMaxW = 0;


	protected BotSkillKeeper()
	{
		super(EBotSkill.KEEPER);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	public BotSkillKeeper(
			final IVector2 ballPos,
			final IVector2 ballVel,
			final double penAreaDepth,
			final double goalWidth,
			final double goalOffset,
			final double velMax,
			final double velMaxW,
			final double accMax,
			final double accMaxW)
	{
		this();

		this.ballPos[0] = (int) (ballPos.x());
		this.ballPos[1] = (int) (ballPos.y());

		this.ballVel[0] = (int) (ballVel.x() * 1000);
		this.ballVel[1] = (int) (ballVel.y() * 1000);

		this.penAreaDepth = (int) penAreaDepth / 10;
		this.goalWidth = (int) goalWidth / 10;
		this.goalOffset = (int) goalOffset;

		setVelMax(velMax);
		setVelMaxW(velMaxW);
		setAccMax(accMax);
		setAccMaxW(accMaxW);
	}


	public BotSkillKeeper(
			final IVector2 ballPos,
			final IVector2 ballVel,
			final double penAreaDepth,
			final double goalWidth,
			final double goalOffset,
			final IMoveConstraints mc)
	{
		this(ballPos, ballVel, penAreaDepth, goalWidth, goalOffset, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(),
				mc.getAccMaxW());
	}


	/**
	 * Max: 5m/s
	 *
	 * @param val [m/s]
	 */
	public final void setVelMax(final double val)
	{
		velMax = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL);
	}


	/**
	 * Max: 30rad/s
	 *
	 * @param val [rad/s]
	 */
	public final void setVelMaxW(final double val)
	{
		velMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
	}


	/**
	 * Max: 10m/s²
	 *
	 * @param val
	 */
	public final void setAccMax(final double val)
	{
		accMax = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC);
	}


	/**
	 * Max: 100rad/s²
	 *
	 * @param val
	 */
	public final void setAccMaxW(final double val)
	{
		accMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC_W);
	}


	public double getVelMax()
	{
		return DriveLimits.toDouble(velMax, DriveLimits.MAX_VEL);
	}


	public double getVelMaxW()
	{
		return DriveLimits.toDouble(velMaxW, DriveLimits.MAX_VEL_W);
	}


	public double getAccMax()
	{
		return DriveLimits.toDouble(accMax, DriveLimits.MAX_ACC);
	}


	public double getAccMaxW()
	{
		return DriveLimits.toDouble(accMaxW, DriveLimits.MAX_ACC_W);
	}


	@Override
	public MoveConstraints getMoveConstraints()
	{
		MoveConstraints moveCon = new MoveConstraints(new BotMovementLimits());
		moveCon.setVelMax(getVelMax());
		moveCon.setVelMaxW(getVelMaxW());
		moveCon.setAccMax(getAccMax());
		moveCon.setAccMaxW(getAccMaxW());

		return moveCon;
	}
}
