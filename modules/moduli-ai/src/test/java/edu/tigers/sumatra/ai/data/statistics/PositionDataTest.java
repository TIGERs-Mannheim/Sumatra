/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.statistics;

import org.junit.Test;

import edu.tigers.sumatra.ai.metis.statistics.PositionData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import junit.framework.Assert;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class PositionDataTest
{
	
	/**
	 * This will check if the initialization with specific boundaries will work properly
	 */
	@Test
	public void testInitializationWithResolution()
	{
		int expectedResolutionX = 100;
		int expectedResolutionY = 100;
		PositionData positionData = new PositionData(expectedResolutionX, expectedResolutionY);
		
		int actualResolutionX = positionData.getResolutionX();
		Assert.assertEquals(expectedResolutionX, actualResolutionX);
		
		int actualResolutionY = positionData.getResolutionY();
		Assert.assertEquals(expectedResolutionY, actualResolutionY);
	}
	
	
	/**
	 * This checks if the position map is returning the right indices
	 */
	@Test
	public void shouldGetEntryFor1DetectionAt99_99ForOurRightCorner()
	{
		int expectedPositions = 1;
		
		int usedResolution = 100;
		
		IVector2 somePoint = Vector2.fromXY(1000, 400);
		
		PositionData positionData = new PositionData(usedResolution, usedResolution);
		positionData.signalPositionAt(somePoint);
		
		int actualPositions = positionData.getEntryForPosition(somePoint);
		
		Assert.assertEquals(expectedPositions, actualPositions);
	}
}
