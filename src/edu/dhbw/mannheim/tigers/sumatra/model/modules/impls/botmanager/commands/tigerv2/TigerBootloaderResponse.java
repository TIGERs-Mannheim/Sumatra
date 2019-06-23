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
	
	private EResponse	type		= EResponse.NONE;
	private long		offset	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderResponse()
	{
	}
	
	
	/**
	 * 
	 * @param t
	 */
	public TigerBootloaderResponse(EResponse t)
	{
		type = t;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		type = EResponse.getResponseConstant(byteArray2UByte(data, 0));
		offset = byteArray2UInt(data, 1);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, type.getId());
		int2ByteArray(data, 1, (int) offset);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BOOTLOADER_RESPONSE;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 5;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public EResponse getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(EResponse type)
	{
		this.type = type;
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