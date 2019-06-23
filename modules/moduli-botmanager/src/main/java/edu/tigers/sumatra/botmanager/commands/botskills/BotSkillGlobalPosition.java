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
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author AndreR
 */
public class BotSkillGlobalPosition extends AMoveBotSkill
{
	private static final int UNUSED_PRIMARY_DIRECTION = -128;
	
	@SerialData(type = ESerialDataType.INT16)
	private final int[] pos = new int[3];
	
	@SerialData(type = ESerialDataType.UINT8)
	private int velMax = 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int velMaxW = 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int accMax = 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int accMaxW = 0;
	
	@SerialData(type = ESerialDataType.EMBEDDED)
	private KickerDribblerCommands kickerDribbler = new KickerDribblerCommands();
	
	/** 360°/255 */
	@SerialData(type = ESerialDataType.INT8)
	private int primaryDirection = UNUSED_PRIMARY_DIRECTION;
	
	
	/**
	 * 
	 */
	private BotSkillGlobalPosition()
	{
		super(EBotSkill.GLOBAL_POSITION);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy [mm]
	 * @param orientation
	 * @param mc
	 */
	public BotSkillGlobalPosition(final IVector2 xy, final double orientation, final MoveConstraints mc)
	{
		this();
		
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0);
		
		
		setVelMax(mc.getVelMax());
		setVelMaxW(mc.getVelMaxW());
		setAccMax(mc.getAccMax());
		setAccMaxW(mc.getAccMaxW());
		setPrimaryDirection(mc.getPrimaryDirection());
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy [mm]
	 * @param orientation
	 * @param velMax
	 * @param velMaxW
	 * @param accMax
	 * @param accMaxW
	 * @param primaryDirection
	 */
	public BotSkillGlobalPosition(final IVector2 xy, final double orientation,
			final double velMax, final double velMaxW, final double accMax, final double accMaxW,
			final IVector2 primaryDirection)
	{
		this();
		
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0);
		
		
		setVelMax(velMax);
		setVelMaxW(velMaxW);
		setAccMax(accMax);
		setAccMaxW(accMaxW);
		setPrimaryDirection(primaryDirection);
	}
	
	
	/**
	 * @return the pos
	 */
	public final IVector2 getPos()
	{
		return Vector2.fromXY(pos[0], pos[1]);
	}
	
	
	/**
	 * @return
	 */
	public final double getOrientation()
	{
		return pos[2] / 1000.0;
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
	
	
	@Override
	public double getKickSpeed()
	{
		return kickerDribbler.getKickSpeed();
	}
	
	
	@Override
	public EKickerMode getMode()
	{
		return kickerDribbler.getMode();
	}
	
	
	@Override
	public EKickerDevice getDevice()
	{
		return kickerDribbler.getDevice();
	}
	
	
	@Override
	public double getDribbleSpeed()
	{
		return kickerDribbler.getDribblerSpeed();
	}
	
	
	/**
	 * @param direction
	 */
	public void setPrimaryDirection(final IVector2 direction)
	{
		if (direction.isZeroVector())
		{
			primaryDirection = UNUSED_PRIMARY_DIRECTION;
		} else
		{
			setPrimaryDirection(direction.getAngle());
		}
	}
	
	
	/**
	 * @param angle
	 */
	public void setPrimaryDirection(final double angle)
	{
		primaryDirection = (int) ((AngleMath.normalizeAngle(angle) * 127.0) / Math.PI);
	}
	
	
	/**
	 * @return primary move direction
	 */
	public IVector2 getPrimaryDirection()
	{
		if (primaryDirection == UNUSED_PRIMARY_DIRECTION)
		{
			return Vector2f.ZERO_VECTOR;
		}
		
		double angle = (primaryDirection / 127.0) * Math.PI;
		return Vector2.fromAngle(angle);
	}
}
