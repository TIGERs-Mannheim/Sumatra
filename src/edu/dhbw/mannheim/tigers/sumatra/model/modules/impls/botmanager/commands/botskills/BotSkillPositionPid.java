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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author AndreR
 */
public class BotSkillPositionPid extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private int	pos[]	= new int[3];
	
	
	/** */
	public BotSkillPositionPid()
	{
		super(EBotSkill.POSITION_PID);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy [mm]
	 * @param orientation
	 */
	public BotSkillPositionPid(final IVector2 xy, final float orientation)
	{
		super(EBotSkill.POSITION_PID);
		
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0f);
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
	public final float getOrientation()
	{
		return pos[2] / 1000f;
	}
}
