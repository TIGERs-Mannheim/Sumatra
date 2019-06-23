/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.10.2010
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.modules.ai;


import static org.junit.Assert.fail;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * This is a test class for the fieldRasterGenerator
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class FieldRasterTester
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private FieldRasterGenerator	raster;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public FieldRasterTester()
	{
		SumatraSetupHelper.setupSumatra();
		
		raster = new FieldRasterGenerator();
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- tests ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPositioningRectangleNegative()
	{
		// check if function can handle negative parameters
		raster.getPosFieldRectangle(-1);
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPositioningRectangleLarge()
	{
		// check if function can handle larger parameters
		final int fieldNumber = (FieldRasterGenerator.getNumberOfColumns() * FieldRasterGenerator.getNumberOfRows()) + 1;
		raster.getPosFieldRectangle(fieldNumber);
	}
	
	
	/**
	 */
	@Test
	public void testGetPositionRectFromPosition()
	{
		final Rectangle field = AIConfig.getGeometry().getField();
		
		// test corners
		raster.getPositionRectFromPosition(new Vector2(field.topLeft()));
		raster.getPositionRectFromPosition(new Vector2(field.topRight()));
		raster.getPositionRectFromPosition(new Vector2(field.bottomLeft()));
		raster.getPositionRectFromPosition(new Vector2(field.bottomRight()));
		
		// test position out of field bounds
		final float fieldXhalf = AIConfig.getGeometry().getFieldLength() / 2;
		final float fieldYhalf = AIConfig.getGeometry().getFieldWidth() / 2;
		
		Vector2 testVector = new Vector2(fieldXhalf + 1, fieldYhalf);
		testGetPositionRectFromPosition(testVector);
		testVector = new Vector2(fieldXhalf, fieldYhalf + 1);
		testGetPositionRectFromPosition(testVector);
		testVector = new Vector2(-fieldXhalf - 1, -fieldYhalf);
		testGetPositionRectFromPosition(testVector);
		testVector = new Vector2(-fieldXhalf, -fieldYhalf - 1);
		testGetPositionRectFromPosition(testVector);
		
	}
	
	
	/**
	 * 
	 * Test for handling position out of field bounds
	 * 
	 * @param position
	 */
	private void testGetPositionRectFromPosition(Vector2 position)
	{
		try
		{
			raster.getPositionRectFromPosition(position);
			fail("IllegalArgumentException was not catched!");
			
		} catch (final IllegalArgumentException err)
		{
		}
	}
	
}
