/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 26, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillSine extends ABotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private final int[]	vel			= new int[3];
	
	@SerialData(type = ESerialDataType.UINT16)
	private int				frequency	= 100;			// [mHz]
	
	
	/**
	 * Constructor.
	 */
	public BotSkillSine()
	{
		super(EBotSkill.BOT_SKILL_SINE);
	}
	
	
	/**
	 * @param velXY
	 * @param velW
	 * @param freq
	 */
	public BotSkillSine(final IVector2 velXY, final double velW, final double freq)
	{
		this();
		
		vel[0] = (int) (velXY.x() * 1000.0);
		vel[1] = (int) (velXY.y() * 1000.0);
		vel[2] = (int) (velW * 1000.0);
		frequency = (int) (freq * 1000.0);
	}
	
	
	public IVector3 getVel()
	{
		return Vector3.fromXYZ(vel[0] * 0.001, vel[1] * 0.001, vel[2] * 0.001);
	}
	
	
	public double getFrequency()
	{
		return frequency * 0.001;
	}
}
