/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.io.UnsupportedEncodingException;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Simple text messages from a tigerv2 bot.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemConsolePrint extends ACommand
{
	/** */
	public static enum ConsolePrintSource
	{
		/** */
		UNKNOWN(0),
		/** */
		MAIN(1),
		/** */
		MEDIA(2);
		
		private final int	id;
		
		
		private ConsolePrintSource(int id)
		{
			this.id = id;
		}
		
		
		/**
		 * Get id of source.
		 * 
		 * @return
		 */
		public int getId()
		{
			return id;
		}
		
		
		/**
		 * Convert an id to an enum.
		 * 
		 * @param id 1 (MAIN) or 2 (MEDIA)
		 * @return enum
		 */
		public static ConsolePrintSource getSourceConstant(int id)
		{
			for (ConsolePrintSource s : values())
			{
				if (s.getId() == id)
				{
					return s;
				}
			}
			
			return UNKNOWN;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private ConsolePrintSource	source;
	private String					text;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void setData(byte[] data)
	{
		int length = byteArray2UByte(data, 0);
		source = ConsolePrintSource.getSourceConstant(byteArray2UByte(data, 1));
		
		try
		{
			text = new String(data, 2, length, "US-ASCII");
		} catch (UnsupportedEncodingException err)
		{
			text = "Unsupported Encoding";
		}
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte[] textBytes;
		try
		{
			textBytes = text.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException err)
		{
			textBytes = new byte[1];
			textBytes[0] = 0;
		}
		
		int length = textBytes.length;
		if (length > 80)
		{
			length = 80;
		}
		
		byte2ByteArray(data, 0, length);
		byte2ByteArray(data, 1, source.getId());
		
		System.arraycopy(textBytes, 0, data, 2, length);
		
		return data;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_CONSOLE_PRINT;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 82;
	}
	
	
	/**
	 * @return the source
	 */
	public ConsolePrintSource getSource()
	{
		return source;
	}
	
	
	/**
	 * @param source the source to set
	 */
	public void setSource(ConsolePrintSource source)
	{
		this.source = source;
	}
	
	
	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}
	
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text)
	{
		this.text = text;
	}
	
}
