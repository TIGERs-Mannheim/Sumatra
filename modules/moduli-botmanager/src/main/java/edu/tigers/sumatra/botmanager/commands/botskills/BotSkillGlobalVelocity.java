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
public class BotSkillGlobalVelocity extends AMoveBotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private final int vel[] = new int[3];
	
	
	private BotSkillGlobalVelocity()
	{
		super(EBotSkill.GLOBAL_VELOCITY);
	}
	
	
	/**
	 * @param mc
	 */
	public BotSkillGlobalVelocity(final MoveConstraints mc)
	{
		super(EBotSkill.GLOBAL_VELOCITY);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy
	 * @param orientation
	 * @param mc
	 */
	public BotSkillGlobalVelocity(final IVector2 xy, final double orientation, final MoveConstraints mc)
	{
		this(mc);
		
		vel[0] = (int) (xy.x() * 1000.0);
		vel[1] = (int) (xy.y() * 1000.0);
		vel[2] = (int) (orientation * 1000.0);
	}
}
