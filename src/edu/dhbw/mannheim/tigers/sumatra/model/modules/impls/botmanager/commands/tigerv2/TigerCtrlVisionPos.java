/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.03.2013
 * Author(s): AndreR
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
 * Position input for the bot, should be unfiltered vision data.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlVisionPos extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private int	pos[]	= new int[3];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlVisionPos()
	{
		super(ECommand.CMD_CTRL_VISION_POS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Global position in [m]
	 * 
	 * @param position X,Y in [m]
	 */
	public void setPosition(IVector2 position)
	{
		pos[0] = (int) (position.x() * 1000.0f);
		pos[1] = (int) (position.y() * 1000.0f);
	}
	
	
	/**
	 * Global orientation in [rad]
	 * 
	 * @param orientation w in [rad]
	 */
	public void setOrientation(float orientation)
	{
		pos[2] = (int) (orientation * 1000.0f);
	}
}
