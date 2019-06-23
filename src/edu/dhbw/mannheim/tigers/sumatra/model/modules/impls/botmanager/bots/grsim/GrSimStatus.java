/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GrSimStatus
{
	private final int			id;
	private final boolean	barrierInterrupted;
	private final boolean	kicked;
	
	
	/**
	  * 
	  */
	public GrSimStatus()
	{
		id = -1;
		barrierInterrupted = false;
		kicked = false;
	}
	
	
	/**
	 * @param data
	 */
	public GrSimStatus(final byte[] data)
	{
		char status = (char) data[0];
		id = status & 7;
		barrierInterrupted = (status & 8) == 1;
		kicked = (status & 16) == 1;
	}
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	
	/**
	 * @return the barrierInterrupted
	 */
	public final boolean isBarrierInterrupted()
	{
		return barrierInterrupted;
	}
	
	
	/**
	 * @return the kicked
	 */
	public final boolean isKicked()
	{
		return kicked;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("GrSimStatus [id=");
		builder.append(id);
		builder.append(", barrierInterrupted=");
		builder.append(barrierInterrupted);
		builder.append(", kicked=");
		builder.append(kicked);
		builder.append("]");
		return builder.toString();
	}
	
}
