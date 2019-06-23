/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * A bootloader command :)
 * 
 * @author AndreR
 */
public class TigerBootloaderCommand extends ACommand
{
	/** */
	public static enum EBootCommand
	{
		/** */
		NONE(0xFF),
		/** */
		MODE_QUERY(0x00),
		/** */
		ENTER(0x01),
		/** */
		EXIT(0x02),
		/** */
		ERASE_MAIN(0x03),
		/** */
		ERASE_MEDIA(0x04),
		/** */
		ERASE_KD(0x05),
		/** */
		ERASE_LEFT(0x06),
		/** */
		ERASE_RIGHT(0x07);
		
		private int	id;
		
		
		private EBootCommand(final int i)
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
		public static EBootCommand getCommandConstant(final int type)
		{
			for (EBootCommand t : values())
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
	private int	type	= EBootCommand.NONE.getId();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderCommand()
	{
		super(ECommand.CMD_BOOTLOADER_COMMAND);
	}
	
	
	/**
	 * @param t
	 */
	public TigerBootloaderCommand(final EBootCommand t)
	{
		super(ECommand.CMD_BOOTLOADER_COMMAND);
		
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
	public EBootCommand getCommand()
	{
		return EBootCommand.getCommandConstant(type);
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setCommand(final EBootCommand type)
	{
		this.type = type.getId();
	}
}