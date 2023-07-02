/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;


import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Intercept an incoming ball near a desired target position.
 */
public class BotSkillReceiveBall extends ABotSkill
{

	/**
	 * Vel max is never really reached as this ball is designed to do only very small adjustments
	 * The 1.5 is the default value that is programmed into the Firmware
	 */
	private static final double VEL_MAX = 1.5;
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int[] receivePose = new int[3];

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int velMaxW;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMax;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMaxW;

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	private KickerDribblerCommands kickerDribbler;

	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int[] ballPos = new int[2];


	@SuppressWarnings("squid:S00107") // required for serialization and UI
	private BotSkillReceiveBall()
	{
		super(EBotSkill.RECEIVE_BALL);
		velMaxW = 0;
		accMax = 0;
		accMaxW = 0;
		kickerDribbler = new KickerDribblerCommands();
	}


	public BotSkillReceiveBall(
			IVector2 interceptPos,
			double interceptOrient,
			MoveConstraints mc,
			KickerDribblerCommands kd,
			IVector2 ballPos
	)
	{

		super(EBotSkill.RECEIVE_BALL);
		this.kickerDribbler = new KickerDribblerCommands();
		setReceivePos(interceptPos);
		setReceiveOrientation(interceptOrient);

		setVelMaxW(mc.getVelMaxW());
		setAccMax(mc.getAccMax());
		setAccMaxW(mc.getAccMaxW());

		setKickSpeed(kd.getKickSpeed());
		setKickerDevice(kd.getDevice());
		setKickerMode(kd.getMode());

		setBallPos(ballPos);
	}


	public IVector2 getReceivePos()
	{
		return Vector2.fromXY(receivePose[0], receivePose[1]);
	}


	public void setReceivePos(IVector2 receivePos)
	{
		this.receivePose[0] = (int) (receivePos.x());
		this.receivePose[1] = (int) (receivePos.y());
	}


	public double getReceiveOrientation()
	{
		return receivePose[2] / 1000.0;
	}


	public void setReceiveOrientation(double receiveOrientation)
	{
		this.receivePose[2] = (int) (receiveOrientation * 1000.0);
	}


	public double getVelMaxW()
	{
		return DriveLimits.toDouble(velMaxW, DriveLimits.MAX_VEL_W);
	}


	public void setVelMaxW(double velMaxW)
	{
		this.velMaxW = DriveLimits.toUInt8(velMaxW, DriveLimits.MAX_VEL_W);
	}


	public double getAccMax()
	{
		return DriveLimits.toDouble(accMax, DriveLimits.MAX_ACC);
	}


	void setAccMax(double accMax)
	{
		this.accMax = DriveLimits.toUInt8(accMax, DriveLimits.MAX_ACC);
	}


	public double getAccMaxW()
	{
		return DriveLimits.toDouble(accMaxW, DriveLimits.MAX_ACC_W);
	}


	void setAccMaxW(double accMaxW)
	{
		this.accMaxW = DriveLimits.toUInt8(accMaxW, DriveLimits.MAX_ACC_W);
	}


	public IVector2 getBallPos()
	{
		return Vector2.fromXY(ballPos[0], ballPos[1]);
	}


	void setBallPos(IVector2 ballPos)
	{
		this.ballPos[0] = (int) (ballPos.x());
		this.ballPos[1] = (int) (ballPos.y());
	}


	@Override
	public MoveConstraints getMoveConstraints()
	{
		MoveConstraints moveCon = new MoveConstraints();
		moveCon.setVelMax(VEL_MAX);
		moveCon.setVelMaxW(getVelMaxW());
		moveCon.setAccMax(getAccMax());
		moveCon.setAccMaxW(getAccMaxW());
		moveCon.setJerkMax(DriveLimits.MAX_JERK);
		moveCon.setJerkMaxW(DriveLimits.MAX_JERK_W);
		moveCon.setPrimaryDirection(Vector2.zero());

		return moveCon;
	}


	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return kickerDribbler;
	}


	void setKickSpeed(double kickSpeed)
	{
		this.kickerDribbler.setKickSpeed(kickSpeed);
	}


	void setKickerDevice(EKickerDevice kickerDevice)
	{
		this.kickerDribbler.setDevice(kickerDevice);
	}


	void setKickerMode(EKickerMode kickerMode)
	{
		this.kickerDribbler.setMode(kickerMode);
	}
}