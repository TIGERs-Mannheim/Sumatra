/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.07.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.I2DShape;

/**
 * Interface for all JUnit-Test classes, testing a {@link I2DShape}.
 * 
 * @author Malte
 * @TODO Is it possible, to add the @Test Tag in the interface so 
 * 	there is no need to add it to the implementation of the methods?
 */
public interface I2DShapeTest
{
	public void testConstructor();
	public void testGetArea();
	public void testIsPointInShape();
	public void testIsLineIntersectingShape();
	public void testNearestPointOutside();	
}
