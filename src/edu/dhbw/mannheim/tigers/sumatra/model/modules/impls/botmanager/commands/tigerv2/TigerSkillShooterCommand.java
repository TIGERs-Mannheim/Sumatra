/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Command for Shooter Skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TigerSkillShooterCommand extends ACommand
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	shootTarget[]	= new int[2];
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	duration;
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT8)
	private int	device;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public TigerSkillShooterCommand()
	{
		super(ECommand.CMD_SKILL_SHOOTER);
		shootTarget[0] = 0;
		shootTarget[1] = 0;
		duration = 0;
		device = 0;
	}
	
	
	/**
	 * @param shootTarget
	 * @param duration
	 * @param device
	 */
	public TigerSkillShooterCommand(IVector2 shootTarget, int duration, int device)
	{
		super(ECommand.CMD_SKILL_SHOOTER);
		this.shootTarget[0] = (int) shootTarget.x();
		this.shootTarget[1] = (int) shootTarget.y();
		this.duration = duration;
		this.device = device;
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
