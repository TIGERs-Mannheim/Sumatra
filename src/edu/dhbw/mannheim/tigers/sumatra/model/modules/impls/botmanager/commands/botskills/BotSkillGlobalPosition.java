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
public class BotSkillGlobalPosition extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private int		pos[]	= new int[3];
	
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	t		= 0;
	
	
	/** */
	public BotSkillGlobalPosition()
	{
		super(EBotSkill.GLOBAL_POSITION);
	}
	
	
	/**
	 * Set velocity in bot local frame.
	 * 
	 * @param xy [mm]
	 * @param orientation
	 * @param t
	 */
	public BotSkillGlobalPosition(final IVector2 xy, final float orientation, final float t)
	{
		super(EBotSkill.GLOBAL_POSITION);
		
		pos[0] = (int) (xy.x());
		pos[1] = (int) (xy.y());
		pos[2] = (int) (orientation * 1000.0f);
		this.t = t;
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
	
	
	/**
	 * @return the t
	 */
	public final float getT()
	{
		return t;
	}
}
