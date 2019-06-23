/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Calibrate motor control on bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TigerSkillCalibrateMotor extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** learn rate */
	@SerialData(type = ESerialDataType.INT16)
	private int		alpha;
	
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int[]	speed	= new int[3];
	
	/** [s] */
	@SerialData(type = ESerialDataType.INT16)
	private int		length;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private TigerSkillCalibrateMotor()
	{
		super(ECommand.CMD_SKILL_CALIBRATE_MOTOR);
		alpha = 10;
		speed[1] = 500;
		length = 2000;
	}
	
	
	/**
	 * @param alpha
	 * @param speed
	 * @param length
	 */
	public TigerSkillCalibrateMotor(float alpha, IVector3 speed, float length)
	{
		super(ECommand.CMD_SKILL_CALIBRATE_MOTOR);
		this.alpha = (int) (alpha * 1000.0f);
		this.speed[0] = (int) (speed.y() * 1000.0f);
		this.speed[1] = (int) (speed.x() * 1000.0f);
		this.speed[2] = (int) (speed.z() * 1000.0f);
		this.length = (int) (length * 1000.0f);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
