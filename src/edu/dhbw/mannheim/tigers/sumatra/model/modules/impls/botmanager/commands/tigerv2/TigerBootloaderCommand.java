/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * A bootloader command :)
 * 
 * @author AndreR
 * 
 */
public class TigerBootloaderCommand extends ACommand
{
	/** */
	public static enum ECommand
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
		ERASE_MEDIA(0x04);
		
		private int	id;
		
		
		private ECommand(int i)
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
		public static ECommand getCommandConstant(int type)
		{
			for (ECommand t : values())
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
	
	private ECommand	type	= ECommand.NONE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderCommand()
	{
	}
	
	
	/**
	 * 
	 * @param t
	 */
	public TigerBootloaderCommand(ECommand t)
	{
		type = t;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		type = ECommand.getCommandConstant(byteArray2UByte(data, 0));
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
		return CommandConstants.CMD_BOOTLOADER_COMMAND;
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
	public ECommand getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(ECommand type)
	{
		this.type = type;
	}
}