/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author AndreR
 */
public class BotSkillLocalVelocity extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private int	vel[]	= new int[3];
	
	
	/** */
	public BotSkillLocalVelocity()
	{
		super(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy
	 * @param orientation
	 */
	public BotSkillLocalVelocity(final IVector2 xy, final float orientation)
	{
		super(EBotSkill.LOCAL_VELOCITY);
		
		vel[0] = (int) (xy.x() * 1000.0f);
		vel[1] = (int) (xy.y() * 1000.0f);
		vel[2] = (int) (orientation * 1000.0f);
	}
	
	
	/**
	 * @return
	 */
	public float getX()
	{
		return vel[0] / 1000;
	}
	
	
	/**
	 * @return
	 */
	public float getY()
	{
		return vel[1] / 1000;
	}
	
	
	/**
	 * @return
	 */
	public float getW()
	{
		return vel[2] / 1000;
	}
}
