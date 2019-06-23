/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author AndreR
 */
public class BotSkillLocalVelocity extends AMoveBotSkill
{
	private static final int	MAX_ACC		= 10;
	private static final int	MAX_ACC_W	= 100;
	private static final int	MAX_JERK		= 100;
	private static final int	MAX_JERK_W	= 1000;
	
	
	@SerialData(type = ESerialDataType.INT16)
	private final int				vel[]			= new int[3];
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						accMax		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						accMaxW		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						jerkMax		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						jerkMaxW		= 0;
	
	
	/**
	 * 
	 */
	private BotSkillLocalVelocity()
	{
		super(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	/**
	 * @param mc
	 */
	public BotSkillLocalVelocity(final MoveConstraints mc)
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
	 * @param xy
	 * @param orientation
	 * @param mc
	 */
	public BotSkillLocalVelocity(final IVector2 xy, final double orientation, final MoveConstraints mc)
	{
		this(mc);
		
		vel[0] = (int) (xy.x() * 1000.0);
		vel[1] = (int) (xy.y() * 1000.0);
		vel[2] = (int) (orientation * 1000.0);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy
	 * @param orientation
	 * @param accMax
	 * @param accMaxW
	 * @param jerkMax
	 * @param jerkMaxW
	 */
	public BotSkillLocalVelocity(final IVector2 xy, final double orientation,
			final double accMax, final double accMaxW, final double jerkMax, final double jerkMaxW)
	{
		this();
		
		vel[0] = (int) (xy.x() * 1000.0);
		vel[1] = (int) (xy.y() * 1000.0);
		vel[2] = (int) (orientation * 1000.0);
		
		setAccMax(accMax);
		setAccMaxW(accMaxW);
		setJerkMaxW(jerkMaxW);
		setJerkMax(jerkMax);
	}
	
	
	/**
	 * @return
	 */
	public double getX()
	{
		return vel[0] / 1000.0;
	}
	
	
	/**
	 * @return
	 */
	public double getY()
	{
		return vel[1] / 1000.0;
	}
	
	
	/**
	 * @return
	 */
	public double getW()
	{
		return vel[2] / 1000.0;
	}
	
	
	/**
	 * Max: 10m/s²
	 * 
	 * @param val
	 */
	public final void setAccMax(final double val)
	{
		accMax = (int) ((val / MAX_ACC) * 255);
	}
	
	
	/**
	 * Max: 100rad/s²
	 * 
	 * @param val
	 */
	public final void setAccMaxW(final double val)
	{
		accMaxW = (int) ((val / MAX_ACC_W) * 255);
	}
	
	
	/**
	 * @return
	 */
	public double getAccMax()
	{
		return (accMax / 255.0) * MAX_ACC;
	}
	
	
	/**
	 * @return
	 */
	public double getAccMaxW()
	{
		return (accMaxW / 255.0) * MAX_ACC_W;
	}
	
	
	/**
	 * Max: 10m/s²
	 * 
	 * @param val
	 */
	public final void setJerkMax(final double val)
	{
		jerkMax = (int) ((val / MAX_JERK) * 255);
	}
	
	
	/**
	 * Max: 100rad/s²
	 * 
	 * @param val
	 */
	public final void setJerkMaxW(final double val)
	{
		jerkMaxW = (int) ((val / MAX_JERK_W) * 255);
	}
	
	
	/**
	 * @return
	 */
	public double getJerkMax()
	{
		return (jerkMax / 255.0) * MAX_JERK;
	}
	
	
	/**
	 * @return
	 */
	public double getJerkMaxW()
	{
		return (jerkMaxW / 255.0) * MAX_JERK_W;
	}
}
