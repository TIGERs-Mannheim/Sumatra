/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class HeatMap
{
	PositionData	positionData;
	
	
	/**
	 * @param positionData
	 */
	public HeatMap(final PositionData positionData)
	{
		this.positionData = positionData;
	}
	
	
	/**
	 * @return
	 */
	public PositionData getPositionData()
	{
		return positionData;
	}
	
}
