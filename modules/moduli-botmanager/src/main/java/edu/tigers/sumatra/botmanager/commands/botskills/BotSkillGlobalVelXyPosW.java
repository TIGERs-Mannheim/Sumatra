/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillGlobalVelXyPosW extends AMoveBotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private final int[]					vel						= new int[2];
	
	@SerialData(type = ESerialDataType.INT16)
	private final int						targetAngle;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								accMax					= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								jerkMax					= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								velMaxW					= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								accMaxW					= 0;
	
	@SerialData(type = ESerialDataType.EMBEDDED)
	private KickerDribblerCommands	kickerDribbler			= new KickerDribblerCommands();
	
	@SerialData(type = ESerialDataType.UINT8)
	private int								dataAcqusitionMode	= 0;
	
	
	/**
	 * Constructor.
	 */
	public BotSkillGlobalVelXyPosW()
	{
		this(AVector2.ZERO_VECTOR, 0);
	}
	
	
	/**
	 * @param xyVel
	 * @param targetAngle
	 */
	public BotSkillGlobalVelXyPosW(final IVector2 xyVel, final double targetAngle)
	{
		super(EBotSkill.GLOBAL_VEL_XY_POS_W);
		vel[0] = (int) (xyVel.x() * 1000.0);
		vel[1] = (int) (xyVel.y() * 1000.0);
		this.targetAngle = (int) (targetAngle * 1000);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy
	 * @param orientation
	 * @param accMax
	 * @param jerkMax
	 * @param velMaxW
	 * @param accMaxW
	 */
	public BotSkillGlobalVelXyPosW(final IVector2 xy, final double orientation,
			final double accMax, final double jerkMax, final double velMaxW, final double accMaxW)
	{
		this(xy, orientation);
		
		setAccMax(accMax);
		setJerkMax(jerkMax);
		
		setAccMaxW(accMaxW);
		setVelMaxW(velMaxW);
	}
	
	
	/**
	 * @return the vel
	 */
	public IVector2 getVel()
	{
		return Vector2.fromXY(vel[0], vel[1]).multiply(1e-3);
	}
	
	
	/**
	 * @return the targetAngle
	 */
	public double getTargetAngle()
	{
		return targetAngle / 1000.0;
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
	 * Max: 100rad/s²
	 * 
	 * @param val
	 */
	public final void setAccMaxW(final double val)
	{
		accMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC_W);
	}
	
	
	public double getVelMaxW()
	{
		return DriveLimits.toDouble(velMaxW, DriveLimits.MAX_VEL_W);
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
	public final void setAccMax(final double val)
	{
		accMax = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC);
	}
	
	
	public double getAccMax()
	{
		return DriveLimits.toDouble(accMax, DriveLimits.MAX_ACC);
	}
	
	
	@Override
	public MoveConstraints getMoveConstraints()
	{
		MoveConstraints moveCon = new MoveConstraints(new BotMovementLimits());
		moveCon.setVelMaxW(getVelMaxW());
		moveCon.setAccMax(getAccMax());
		moveCon.setAccMaxW(getAccMaxW());
		moveCon.setJerkMax(getJerkMax());
		
		return moveCon;
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
	 * @return
	 */
	public double getJerkMax()
	{
		return DriveLimits.toDouble(jerkMax, DriveLimits.MAX_JERK);
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
