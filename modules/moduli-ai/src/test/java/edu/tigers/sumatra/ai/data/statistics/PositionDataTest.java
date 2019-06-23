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

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.Geometry;


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
		
		IVector2 topLeftCorner = Geometry.getCornerRightOur();
		
		PositionData positionData = new PositionData(usedResolution, usedResolution);
		positionData.signalPositionAt(topLeftCorner);
		
		int actualPositions = positionData.getEntryForPosition(topLeftCorner);
		
		Assert.assertEquals(expectedPositions, actualPositions);
	}
}
