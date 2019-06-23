/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillGlobalPosVel extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private int	vel[]	= new int[3];
	@SerialData(type = ESerialDataType.INT16)
	private int	pos[]	= new int[3];
	
	
	/** */
	public BotSkillGlobalPosVel()
	{
		super(EBotSkill.GLOBAL_POS_VEL);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy [mm]
	 * @param orientation [rad]
	 * @param globVel [m/s]
	 */
	public BotSkillGlobalPosVel(final IVector2 xy, final float orientation, final IVector3 globVel)
	{
		super(EBotSkill.GLOBAL_POS_VEL);
		
		vel[0] = (int) (globVel.x() * 1000.0f);
		vel[1] = (int) (globVel.y() * 1000.0f);
		vel[2] = (int) (globVel.z() * 1000.0f);
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0f);
	}
}
