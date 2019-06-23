/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * @author AndreR
 */
public class BotSkillCircleBall extends AMoveBotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private int								speed						= 0;
	
	@SerialData(type = ESerialDataType.INT16)
	private int								circleRadius			= 0;
	
	@SerialData(type = ESerialDataType.INT16)
	private int								targetAngle				= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								frictionCoeff			= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								accMax					= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int								accMaxW					= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int								jerkMax					= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int								jerkMaxW					= 0;
	
	@SerialData(type = ESerialDataType.EMBEDDED)
	private KickerDribblerCommands	kickerDribbler			= new KickerDribblerCommands();
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								dataAcqusitionMode	= 0;
	
	
	/**
	 * 
	 */
	private BotSkillCircleBall()
	{
		super(EBotSkill.CIRCLE_BALL);
	}
	
	
	/**
	 * @param mc
	 */
	public BotSkillCircleBall(final MoveConstraints mc)
	{
		this();
		setAccMax(mc.getAccMax());
		setAccMaxW(mc.getAccMaxW());
		setJerkMaxW(mc.getJerkMaxW());
		setJerkMax(mc.getJerkMax());
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param speed [m/s]
	 * @param radius [mm]
	 * @param targetAngle
	 * @param friction
	 * @param mc
	 */
	public BotSkillCircleBall(final double speed, final double radius, final double targetAngle,
			final double friction, final MoveConstraints mc)
	{
		this(mc);
		
		this.speed = (int) (speed * 1000.0);
		circleRadius = (int) radius;
		this.targetAngle = (int) (targetAngle * 1e4);
		frictionCoeff = (int) (friction * 256.0);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * Used by bot skills panel.
	 * 
	 * @param speed
	 * @param radius
	 * @param targetAngle
	 * @param friction
	 * @param accMax
	 * @param accMaxW
	 * @param jerkMax
	 * @param jerkMaxW
	 * @param dribbleSpeed
	 * @param kickSpeed
	 * @param kickDevice
	 * @param kickMode
	 */
	@SuppressWarnings("squid:S00107")
	public BotSkillCircleBall(final double speed, final double radius, final double targetAngle,
			final double friction,
			final double accMax, final double accMaxW, final double jerkMax, final double jerkMaxW,
			final double dribbleSpeed, final double kickSpeed, final EKickerDevice kickDevice, final EKickerMode kickMode)
	{
		this();
		
		this.speed = (int) (speed * 1000.0);
		circleRadius = (int) radius;
		this.targetAngle = (int) (targetAngle * 1e4);
		frictionCoeff = (int) (friction * 256.0);
		
		setAccMax(accMax);
		setAccMaxW(accMaxW);
		setJerkMaxW(jerkMaxW);
		setJerkMax(jerkMax);
		
		kickerDribbler.setDribblerSpeed(dribbleSpeed);
		kickerDribbler.setKick(kickSpeed, kickDevice, kickMode);
	}
	
	
	public double getSpeed()
	{
		return speed * 0.001;
	}
	
	
	public double getRadius()
	{
		return circleRadius;
	}
	
	
	public double getTargetAngle()
	{
		return targetAngle * 1e-4;
	}
	
	
	public double getFriction()
	{
		return (frictionCoeff * 1.0) / 256.0;
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
	
	
	public double getAccMax()
	{
		return DriveLimits.toDouble(accMax, DriveLimits.MAX_ACC);
	}
	
	
	public double getAccMaxW()
	{
		return DriveLimits.toDouble(accMaxW, DriveLimits.MAX_ACC_W);
	}
	
	
	/**
	 * Max: 10m/s²
	 * 
	 * @param val
	 */
	public final void setJerkMax(final double val)
	{
		jerkMax = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK);
	}
	
	
	/**
	 * Max: 100rad/s²
	 * 
	 * @param val
	 */
	public final void setJerkMaxW(final double val)
	{
		jerkMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK_W);
	}
	
	
	/**
	 * @return
	 */
	public double getJerkMax()
	{
		return DriveLimits.toDouble(jerkMax, DriveLimits.MAX_JERK);
	}
	
	
	/**
	 * @return
	 */
	public double getJerkMaxW()
	{
		return DriveLimits.toDouble(jerkMaxW, DriveLimits.MAX_JERK_W);
	}
	
	
	@Override
	public MoveConstraints getMoveConstraints()
	{
		MoveConstraints moveCon = new MoveConstraints(new BotMovementLimits());
		moveCon.setAccMax(getAccMax());
		moveCon.setAccMaxW(getAccMaxW());
		moveCon.setJerkMax(getJerkMax());
		moveCon.setJerkMaxW(getJerkMaxW());
		
		return moveCon;
	}
	
	
	/**
	 * @return the kickerDribbler
	 */
	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return kickerDribbler;
	}
	
	
	/**
	 * @param kickerDribbler the kickerDribbler to set
	 */
	@Override
	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
		this.kickerDribbler = kickerDribbler;
	}
	
	
	/**
	 * @return the dataAcqusitionMode
	 */
	@Override
	public EDataAcquisitionMode getDataAcquisitionMode()
	{
		return EDataAcquisitionMode.getModeConstant(dataAcqusitionMode);
	}
	
	
	/**
	 * @param dataAcqusitionMode the dataAcqusitionMode to set
	 */
	@Override
	public void setDataAcquisitionMode(final EDataAcquisitionMode dataAcqusitionMode)
	{
		this.dataAcqusitionMode = dataAcqusitionMode.getId();
	}
}
