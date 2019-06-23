/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;


/**
 * Packs a ACommand in an ACommand and prepends an ID.
 * This is a varying length command!
 * 
 * @author AndreR
 * 
 */
public class BaseStationACommand extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private ACommand		child	= null;
	private BotID			id		= new BotID();
	
	private final Logger	log	= Logger.getLogger(getClass());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationACommand()
	{
	}
	
	
	/**
	 * 
	 * @param id
	 * @param command
	 */
	public BaseStationACommand(BotID id, ACommand command)
	{
		this.id = id;
		child = command;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		if (data.length < (CommandConstants.HEADER_SIZE + 1))
		{
			log.warn("Child data to small (" + data.length + " Byte)");
			return;
		}
		
		id = new BotID(byteArray2UByte(data, 0));
		
		final byte header[] = new byte[CommandConstants.HEADER_SIZE];
		System.arraycopy(data, 1, header, 0, CommandConstants.HEADER_SIZE);
		child = CommandFactory.createEmptyPacket(header);
		if (child == null)
		{
			log.warn("Unknown command with header: " + header);
			return;
		}
		if ((data.length - CommandConstants.HEADER_SIZE - 1) < child.getDataLength())
		{
			log.warn("Not enough data, got " + (data.length - CommandConstants.HEADER_SIZE - 1) + ", expected: "
					+ child.getDataLength());
			// TODO AndreR: Report failures and discard commands which are invalid
			return;
		}
		
		final byte childData[] = new byte[child.getDataLength()];
		System.arraycopy(data, 1 + CommandConstants.HEADER_SIZE, childData, 0, child.getDataLength());
		child.setData(childData);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, id.getNumber());
		
		final byte transferData[] = child.getTransferData();
		System.arraycopy(transferData, 0, data, 1, transferData.length);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BASE_ACOMMAND;
	}
	
	
	@Override
	public int getDataLength()
	{
		if (child != null)
		{
			return child.getDataLength() + CommandConstants.HEADER_SIZE + 1;
		}
		
		return 0;
	}
	
	
	/**
	 * @return the child
	 */
	public ACommand getChild()
	{
		return child;
	}
	
	
	/**
	 * @param child the child to set
	 */
	public void setChild(ACommand child)
	{
		this.child = child;
	}
	
	
	/**
	 * @return the id
	 */
	public BotID getId()
	{
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(BotID id)
	{
		this.id = id;
	}
}
