/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.07.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Acknowledgment for reliable commands.
 * ACKs are never send themselves as reliable!
 * 
 * @author AndreR
 */
public class TigerSystemAck extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT16)
	private int	seq;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerSystemAck()
	{
		super(ECommand.CMD_SYSTEM_ACK);
	}
	
	
	/**
	 * @param seq
	 */
	public TigerSystemAck(final int seq)
	{
		super(ECommand.CMD_SYSTEM_ACK);
		
		this.seq = seq;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the seq
	 */
	@Override
	public int getSeq()
	{
		return seq;
	}
	
	
	/**
	 * @param seq the seq to set
	 */
	@Override
	public void setSeq(final int seq)
	{
		this.seq = seq;
	}
}
