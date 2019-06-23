/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Keeper Skill Command
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TigerSkillPositioningCommand extends ACommand
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	destination[]	= new int[3];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	@SuppressWarnings("unused")
	private TigerSkillPositioningCommand()
	{
		this(Vector2.ZERO_VECTOR, 0);
	}
	
	
	/**
	 * @param destination [mm]
	 * @param orientation [rad]
	 */
	public TigerSkillPositioningCommand(IVector2 destination, float orientation)
	{
		super(ECommand.CMD_SKILL_POSITIONING);
		
		this.destination[0] = (int) destination.x();
		this.destination[1] = (int) destination.y();
		this.destination[2] = (int) (1000 * orientation);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the destination [mm]
	 */
	public final IVector2 getDestination()
	{
		return new Vector2(destination[0], destination[1]);
	}
	
	
	/**
	 * @return orientation [rad]
	 */
	public final float getOrientation()
	{
		return destination[2] / 1000.0f;
	}
	
}
