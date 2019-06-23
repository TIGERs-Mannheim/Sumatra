/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Keeper Skill Command
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerSkillKeeperCommand extends ACommand
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	point2Block[]	= new int[2];
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	radius;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public TigerSkillKeeperCommand()
	{
		this(AVector2.ZERO_VECTOR, 0);
	}
	
	
	/**
	 * @param point2Block
	 * @param radius
	 */
	public TigerSkillKeeperCommand(final IVector2 point2Block, final float radius)
	{
		super(ECommand.CMD_SKILL_KEEPER);
		
		this.point2Block[0] = (int) point2Block.x();
		this.point2Block[1] = (int) point2Block.y();
		this.radius = (int) radius;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
