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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	private int	pos[]	= new int[3];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		pos[0] = byteArray2Short(data, 0);
		pos[1] = byteArray2Short(data, 2);
		pos[2] = byteArray2Short(data, 4);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, pos[0]);
		short2ByteArray(data, 2, pos[1]);
		short2ByteArray(data, 4, pos[2]);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_VISION_POS;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 6;
	}
	
	
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
