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
 * Simple command message to a tigerv2 bot.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemConsoleCommand extends ACommand
{
	/** */
	public static enum ConsoleCommandTarget
	{
		/** */
		UNKNOWN(0),
		/** */
		MAIN(1),
		/** */
		MEDIA(2);
		
		private final int	id;
		
		
		private ConsoleCommandTarget(int id)
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
		public static ConsoleCommandTarget getTargetConstant(int id)
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
	private ConsoleCommandTarget	target;
	private String						text;
	
	
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
		target = ConsoleCommandTarget.getTargetConstant(byteArray2UByte(data, 1));
		
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
		byte2ByteArray(data, 1, target.getId());
		
		System.arraycopy(textBytes, 0, data, 2, length);
		
		return data;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_CONSOLE_COMMAND;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 82;
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
	
	
	/**
	 * @return the target
	 */
	public ConsoleCommandTarget getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(ConsoleCommandTarget target)
	{
		this.target = target;
	}
	
}
