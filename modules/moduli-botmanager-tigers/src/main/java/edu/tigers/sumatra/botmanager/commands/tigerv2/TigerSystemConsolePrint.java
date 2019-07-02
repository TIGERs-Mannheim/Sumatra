/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import java.nio.charset.StandardCharsets;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Simple text messages from a tigerv2 bot.
 * 
 * @author AndreR
 */
public class TigerSystemConsolePrint extends ACommand
{
	/** Console source. */
	public enum ConsolePrintSource
	{
		/** */
		UNKNOWN(0),
		/** */
		MAIN(1),
		/** */
		MEDIA(2),
		/** */
		LEFT(4),
		/** */
		RIGHT(8),
		/** */
		KD(0x10);
		
		private final int id;
		
		
		ConsolePrintSource(final int id)
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
		public static ConsolePrintSource getSourceConstant(final int id)
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
	@SerialData(type = ESerialDataType.UINT8)
	private int source;
	@SerialData(type = ESerialDataType.TAIL)
	private byte[] textData;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** Constructor. */
	public TigerSystemConsolePrint()
	{
		super(ECommand.CMD_SYSTEM_CONSOLE_PRINT);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the source
	 */
	public ConsolePrintSource getSource()
	{
		return ConsolePrintSource.getSourceConstant(source);
	}
	
	
	/**
	 * @param source the source to set
	 */
	public void setSource(final ConsolePrintSource source)
	{
		this.source = source.getId();
	}
	
	
	/**
	 * @return the text
	 */
	public String getText()
	{
		return new String(textData, 0, textData.length, StandardCharsets.US_ASCII);
	}
	
	
	/**
	 * @param text the text to set
	 */
	public void setText(final String text)
	{
		textData = text.getBytes(StandardCharsets.US_ASCII);
	}
}
