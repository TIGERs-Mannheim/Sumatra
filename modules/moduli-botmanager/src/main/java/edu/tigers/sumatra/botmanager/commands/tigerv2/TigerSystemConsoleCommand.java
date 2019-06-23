/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import java.io.UnsupportedEncodingException;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Simple command message to a tigerv2 bot.
 * 
 * @author AndreR
 */
public class TigerSystemConsoleCommand extends ACommand
{
	/** Console target. */
	public enum ConsoleCommandTarget
	{
		/** */
		UNKNOWN(0),
		/** */
		MAIN(1),
		/** */
		MEDIA(2);
		
		private final int id;
		
		
		private ConsoleCommandTarget(final int id)
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
		public static ConsoleCommandTarget getTargetConstant(final int id)
		{
			for (ConsoleCommandTarget s : values())
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
	private int		target;
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	textData;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** Constructor. */
	public TigerSystemConsoleCommand()
	{
		super(ECommand.CMD_SYSTEM_CONSOLE_COMMAND, true);
	}
	
	
	/**
	 * @param target
	 * @param command
	 */
	public TigerSystemConsoleCommand(final ConsoleCommandTarget target, final String command)
	{
		this();
		setTarget(target);
		setText(command);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the text
	 */
	@SuppressWarnings("squid:S1166")
	public String getText()
	{
		String text;
		
		try
		{
			text = new String(textData, 0, textData.length, "US-ASCII");
		} catch (UnsupportedEncodingException err)
		{
			text = "Unsupported Encoding";
		}
		
		return text;
	}
	
	
	/**
	 * @param text the text to set
	 */
	@SuppressWarnings("squid:S1166")
	public void setText(final String text)
	{
		try
		{
			textData = text.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException err)
		{
			textData = new byte[1];
			textData[0] = 0;
		}
	}
	
	
	/**
	 * @return the target
	 */
	public ConsoleCommandTarget getTarget()
	{
		return ConsoleCommandTarget.getTargetConstant(target);
	}
	
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(final ConsoleCommandTarget target)
	{
		this.target = target.getId();
	}
	
}
