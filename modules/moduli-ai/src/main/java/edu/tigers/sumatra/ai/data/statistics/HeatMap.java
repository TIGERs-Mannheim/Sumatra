/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 1, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics;

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
