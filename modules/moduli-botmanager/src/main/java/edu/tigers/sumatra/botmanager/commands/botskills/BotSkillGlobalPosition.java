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
import edu.tigers.sumatra.math.Vector2;


/**
 * @author AndreR
 */
public class BotSkillGlobalPosition extends AMoveBotSkill
{
	private static final int	MAX_VEL		= 5;
	private static final int	MAX_VEL_W	= 30;
	private static final int	MAX_ACC		= 10;
	private static final int	MAX_ACC_W	= 100;
	
	@SerialData(type = ESerialDataType.INT16)
	private final int				pos[]			= new int[3];
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						velMax		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						velMaxW		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						accMax		= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int						accMaxW		= 0;
	
	
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
	 */
	public BotSkillGlobalPosition(final IVector2 xy, final double orientation,
			final double velMax, final double velMaxW, final double accMax, final double accMaxW)
	{
		this();
		
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0);
		
		
		setVelMax(velMax);
		setVelMaxW(velMaxW);
		setAccMax(accMax);
		setAccMaxW(accMaxW);
	}
	
	
	/**
	 * @return the pos
	 */
	public final IVector2 getPos()
	{
		return new Vector2(pos[0], pos[1]);
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
		velMax = (int) ((val / MAX_VEL) * 255);
	}
	
	
	/**
	 * Max: 30rad/s
	 * 
	 * @param val [rad/s]
	 */
	public final void setVelMaxW(final double val)
	{
		velMaxW = (int) ((val / MAX_VEL_W) * 255);
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
	public double getVelMax()
	{
		return (velMax / 255.0) * MAX_VEL;
	}
	
	
	/**
	 * @return
	 */
	public double getVelMaxW()
	{
		return (velMaxW / 255.0) * MAX_VEL_W;
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
}
