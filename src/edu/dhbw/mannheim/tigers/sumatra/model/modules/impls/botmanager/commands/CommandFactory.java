/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialDescription;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialException;


/**
 */
public final class CommandFactory
{
	// Logger
	private static final Logger					log						= Logger.getLogger(CommandFactory.class.getName());
	
	private static CommandFactory					instance					= null;
	
	private Map<Integer, SerialDescription>	commands					= new HashMap<Integer, SerialDescription>();
	
	private static final int						HEADER_LENGTH			= 2;
	private static final int						LEGACY_HEADER_LENGTH	= 4;
	
	
	private CommandFactory()
	{
		
	}
	
	
	/**
	 * Get singleton instance.
	 * 
	 * @return
	 */
	public static synchronized CommandFactory getInstance()
	{
		if (instance == null)
		{
			instance = new CommandFactory();
		}
		return instance;
	}
	
	
	/**
	 * Call once per application lifetime to parse all commands.
	 */
	public void loadCommands()
	{
		commands.clear();
		
		for (ECommand ecmd : ECommand.values())
		{
			SerialDescription desc;
			try
			{
				desc = new SerialDescription(ecmd.getClazz());
				
				// do sanity checks for encode and decode - should not throw any exception
				desc.decode(desc.encode(desc.newInstance()));
			} catch (SerialException err)
			{
				log.error("Could not load command: " + ecmd, err);
				continue;
			}
			
			ACommand acmd;
			try
			{
				acmd = (ACommand) desc.newInstance();
			} catch (SerialException err)
			{
				log.error("Could not create instance of: " + ecmd);
				continue;
			} catch (ClassCastException err)
			{
				log.error(ecmd + " is not based on ACommand!", err);
				continue;
			}
			
			if (acmd.getType().getId() != ecmd.getId())
			{
				log.error("ECommand id mismatch in command: " + ecmd + ". The command does not use the correct enum.");
				continue;
			}
			
			if (commands.get(ecmd.getId()) != null)
			{
				log.error(ecmd + "'s command code is already defined by: "
						+ commands.get(ecmd.getId()).getClass().getName());
				continue;
			}
			
			commands.put(ecmd.getId(), desc);
		}
	}
	
	
	/**
	 * Parse byte data and create command.
	 * 
	 * @param data
	 * @param legacy true if this is a legacy command which's header includes data length
	 * @return
	 */
	public ACommand decode(final byte[] data, final boolean legacy)
	{
		int cmdId = ACommand.byteArray2UShort(data, 0);
		
		if (!commands.containsKey(cmdId))
		{
			log.error("Unknown command: " + cmdId + ", length: " + data.length);
			return null;
		}
		
		byte[] cmdData;
		
		if (legacy)
		{
			cmdData = new byte[data.length - LEGACY_HEADER_LENGTH];
			System.arraycopy(data, LEGACY_HEADER_LENGTH, cmdData, 0, data.length - LEGACY_HEADER_LENGTH);
		} else
		{
			cmdData = new byte[data.length - HEADER_LENGTH];
			System.arraycopy(data, HEADER_LENGTH, cmdData, 0, data.length - HEADER_LENGTH);
		}
		
		SerialDescription cmdDesc = commands.get(cmdId);
		
		ACommand acmd;
		
		try
		{
			acmd = (ACommand) cmdDesc.decode(cmdData);
			int cmdDataLen = cmdDesc.getLength(acmd);
			if (cmdDataLen != cmdData.length)
			{
				String cmdStr = String.format("0x%x", cmdId);
				log.warn("Command " + cmdStr + " did not parse all data (" + cmdDataLen + " used of " + cmdData.length
						+ " available)");
			}
		} catch (SerialException err)
		{
			log.error("Could not parse cmd: " + cmdId, err);
			return null;
		}
		
		return acmd;
	}
	
	
	/**
	 * Encode command in byte stream.
	 * 
	 * @param cmd
	 * @param legacy true if this is a legacy command which's header includes data length
	 * @return
	 */
	public byte[] encode(final ACommand cmd, final boolean legacy)
	{
		int cmdId = cmd.getType().getId();
		
		if (!commands.containsKey(cmdId))
		{
			log.error("No description for command: " + cmdId);
			return null;
		}
		
		SerialDescription cmdDesc = commands.get(cmdId);
		
		byte[] cmdData;
		try
		{
			cmdData = cmdDesc.encode(cmd);
		} catch (SerialException err)
		{
			log.error("Could not encode command: " + cmdId, err);
			return null;
		}
		
		byte[] data;
		
		if (legacy)
		{
			data = new byte[cmdData.length + LEGACY_HEADER_LENGTH];
			
			ACommand.short2ByteArray(data, 0, cmdId);
			ACommand.short2ByteArray(data, 2, cmdData.length);
			
			System.arraycopy(cmdData, 0, data, LEGACY_HEADER_LENGTH, cmdData.length);
		} else
		{
			data = new byte[cmdData.length + HEADER_LENGTH];
			
			ACommand.short2ByteArray(data, 0, cmdId);
			
			System.arraycopy(cmdData, 0, data, HEADER_LENGTH, cmdData.length);
		}
		
		return data;
	}
	
	
	/**
	 * @param cmd
	 * @param legacy
	 * @return
	 */
	public int getLength(final ACommand cmd, final boolean legacy)
	{
		int length;
		int cmdId = cmd.getType().getId();
		
		if (!commands.containsKey(cmdId))
		{
			log.error("No description for command: " + cmdId);
			return 0;
		}
		
		SerialDescription cmdDesc = commands.get(cmdId);
		
		try
		{
			length = cmdDesc.getLength(cmd);
		} catch (SerialException err)
		{
			log.error("Could not get length of: " + cmd.getType(), err);
			return 0;
		}
		
		if (legacy)
		{
			length += LEGACY_HEADER_LENGTH;
		} else
		{
			length += HEADER_LENGTH;
		}
		
		return length;
	}
}
