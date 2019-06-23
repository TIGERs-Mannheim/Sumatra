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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * A bootloader response :)
 * 
 * @author AndreR
 * 
 */
public class TigerBootloaderResponse extends ACommand
{
	/** */
	public static enum EResponse
	{
		/** */
		NONE(0xFF),
		/** */
		ACK(0x00),
		/** */
		NACK(0x01),
		/** */
		MODE_NORMAL(0x02),
		/** */
		MODE_BOOTLOADER(0x03);
		
		private int	id;
		
		
		private EResponse(int i)
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
		public static EResponse getResponseConstant(int type)
		{
			for (EResponse t : values())
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
	private int		type		= EResponse.NONE.getId();
	@SerialData(type = ESerialDataType.UINT32)
	private long	offset	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderResponse()
	{
		super(ECommand.CMD_BOOTLOADER_RESPONSE);
	}
	
	
	/**
	 * 
	 * @param t
	 */
	public TigerBootloaderResponse(EResponse t)
	{
		super(ECommand.CMD_BOOTLOADER_RESPONSE);
		
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
	public EResponse getResponse()
	{
		return EResponse.getResponseConstant(type);
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setResponse(EResponse type)
	{
		this.type = type.getId();
	}
	
	
	/**
	 * @return the offset
	 */
	public int getOffset()
	{
		return (int) offset;
	}
	
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(long offset)
	{
		this.offset = offset;
	}
}