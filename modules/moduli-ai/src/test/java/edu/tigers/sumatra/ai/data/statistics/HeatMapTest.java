/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 1, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics;

import junit.framework.Assert;

import org.junit.Test;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class HeatMapTest
{
	
	/**
	 * This checks for a proper initialization with position data
	 */
	@Test
	public void shouldBeInitializedWithProperPositionData()
	{
		int resolutions = 100;
		PositionData expectedPositionData = new PositionData(resolutions, resolutions);
		
		HeatMap heatMap = new HeatMap(expectedPositionData);
		
		PositionData actualPositionData = heatMap.getPositionData();
		
		Assert.assertEquals(expectedPositionData, actualPositionData);
	}
}
