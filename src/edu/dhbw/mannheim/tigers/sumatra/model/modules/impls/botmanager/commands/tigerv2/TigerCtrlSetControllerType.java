/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Set the active controller on the bot.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlSetControllerType extends ACommand
{
	/** */
	public static enum ControllerType
	{
		/** */
		NONE(0x00),
		/** */
		FUSION_VEL(0x01),
		/** */
		FUSION(0x02);
		
		private int	id;
		
		
		private ControllerType(int i)
		{
			id = i;
		}
		
		
		/**
		 * 
		 * @return
		 */
		public int getId()
		{
			return id;
		}
		
		
		/**
		 * 
		 * @param type
		 * @return
		 */
		public static ControllerType getControllerTypeConstant(int type)
		{
			for (ControllerType t : values())
			{
				if (t.getId() == type)
				{
					return t;
				}
			}
			
			return NONE;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ControllerType	type	= ControllerType.NONE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerCtrlSetControllerType()
	{
	}
	
	
	/**
	 * 
	 * @param t
	 */
	public TigerCtrlSetControllerType(ControllerType t)
	{
		type = t;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		type = ControllerType.getControllerTypeConstant(byteArray2UByte(data, 0));
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, type.getId());
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_SET_CONTROLLER_TYPE;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 1;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public ControllerType getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(ControllerType type)
	{
		this.type = type;
	}
}