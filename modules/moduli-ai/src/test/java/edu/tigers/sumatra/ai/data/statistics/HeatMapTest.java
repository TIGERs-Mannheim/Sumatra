/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.statistics;

import org.junit.Test;

import edu.tigers.sumatra.ai.metis.statistics.HeatMap;
import edu.tigers.sumatra.ai.metis.statistics.PositionData;
import junit.framework.Assert;


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
