/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Packs a ACommand in an ACommand and prepends an ID.
 * This is a varying length command!
 * 
 * @author AndreR
 */
public class BaseStationACommand extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Cached variables
	private ACommand	child	= null;
	private BotID		id		= null;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int			idData;
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]		childData;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 */
	public BaseStationACommand()
	{
		super(ECommand.CMD_BASE_ACOMMAND);
	}
	
	
	/**
	 * @param id
	 * @param command
	 */
	public BaseStationACommand(final BotID id, final ACommand command)
	{
		super(ECommand.CMD_BASE_ACOMMAND);
		
		setId(id);
		setChild(command);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the child
	 */
	public ACommand getChild()
	{
		if (child == null)
		{
			child = CommandFactory.getInstance().decode(childData);
		}
		
		return child;
	}
	
	
	/**
	 * @param child the child to set
	 */
	public void setChild(final ACommand child)
	{
		childData = CommandFactory.getInstance().encode(child);
		this.child = child;
	}
	
	
	/**
	 * @return the id
	 */
	public BotID getId()
	{
		if (id == null)
		{
			id = getBotIdFromBaseStationId(idData);
		}
		
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(final BotID id)
	{
		idData = getBaseStationIdFromBotId(id);
		this.id = id;
	}
	
	
	/**
	 * Transform a botID in base station format to Sumatra BotID.
	 * Base station used id 0-11 for yellow and 12-23 for blue.
	 * ID 255 is an unused slot in base station.
	 * 
	 * @param id
	 * @return
	 */
	public static BotID getBotIdFromBaseStationId(final int id)
	{
		if (id == AObjectID.UNINITIALIZED_ID)
		{
			return BotID.noBot();
		}
		
		if (id > AObjectID.BOT_ID_MIDDLE_BS)
		{
			return BotID.createBotId(id - (AObjectID.BOT_ID_MIDDLE_BS + 1), ETeamColor.BLUE);
		}
		
		return BotID.createBotId(id, ETeamColor.YELLOW);
	}
	
	
	/**
	 * Transform a Sumatra BotID to base station format.
	 * 
	 * @param id
	 * @return
	 */
	public static int getBaseStationIdFromBotId(final BotID id)
	{
		if (id.getNumber() == AObjectID.UNINITIALIZED_ID)
		{
			return 255;
		}
		
		if (id.getTeamColor() == ETeamColor.BLUE)
		{
			return id.getNumber() + AObjectID.BOT_ID_MIDDLE_BS + 1;
		}
		
		return id.getNumber();
	}
}
