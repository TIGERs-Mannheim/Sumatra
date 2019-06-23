/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Set the active controller on the bot.
 * 
 * @author AndreR
 */
public class TigerCtrlSetControllerType extends ACommand
{
	/** */
	public static enum EControllerType
	{
		/** */
		NONE(0x00),
		/** */
		FUSION_VEL(0x01),
		/** */
		FUSION(0x02),
		/** */
		MOTOR(0x03),
		/** */
		CALIBRATE(0x04),
		/** */
		TIGGA(0x05);
		
		private int	id;
		
		
		private EControllerType(final int i)
		{
			id = i;
		}
		
		
		/**
		 * @return
		 */
		public int getId()
		{
			return id;
		}
		
		
		/**
		 * @param type
		 * @return
		 */
		public static EControllerType getControllerTypeConstant(final int type)
		{
			for (EControllerType t : values())
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
	@SerialData(type = ESerialDataType.UINT8)
	private int	type	= EControllerType.NONE.getId();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerCtrlSetControllerType()
	{
		super(ECommand.CMD_CTRL_SET_CONTROLLER_TYPE, true);
	}
	
	
	/**
	 * @param t
	 */
	public TigerCtrlSetControllerType(final EControllerType t)
	{
		super(ECommand.CMD_CTRL_SET_CONTROLLER_TYPE, true);
		
		type = t.getId();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public EControllerType getControllerType()
	{
		return EControllerType.getControllerTypeConstant(type);
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setControllerType(final EControllerType type)
	{
		this.type = type.getId();
	}
}