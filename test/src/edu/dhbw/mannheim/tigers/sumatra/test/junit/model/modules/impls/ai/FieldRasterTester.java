/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.10.2010
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai;


import static org.junit.Assert.fail;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRaster;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions.LoadConfigException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;


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
	
	private FieldRaster	rasterConfig	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public FieldRasterTester()
	{
		// Load configuration from xml-file
		try
		{
			AIConfig.getInstance().loadAIConfig(AAgent.AI_CONFIG_PATH + AAgent.AI_DEFAULT_CONFIG);
		} catch (LoadConfigException err)
		{
			System.out.println("Unable to load ai configuration: " + err);
			throw new RuntimeException("Unable to load ai Config:" + AAgent.AI_CONFIG_PATH + AAgent.AI_DEFAULT_CONFIG);
		}
		
		rasterConfig = AIConfig.getFieldRaster();
		
	}
	

	// --------------------------------------------------------------------------
	// --- tests ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Test
	public void testGetPositioningRectangle()
	{
		try
		{
			// check if function can handle negative parameters
			FieldRasterGenerator.getInstance().getPositioningRectangle(-1);
			fail("IllegalArgumentException was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			// check if function can handle larger parameters
			int fieldNumber = rasterConfig.getNumberOfColumns() * rasterConfig.getNumberOfRows() + 1;
			FieldRasterGenerator.getInstance().getPositioningRectangle(fieldNumber);
			fail("IllegalArgumentException was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
	}
	

	@Test
	public void testGetPositionRectFromPosition()
	{
		Rectanglef field = AIConfig.getGeometry().getField();
		
		// test corners
		FieldRasterGenerator.getInstance().getPositionRectFromPosition(new Vector2(field.topLeft()));
		FieldRasterGenerator.getInstance().getPositionRectFromPosition(new Vector2(field.topRight()));
		FieldRasterGenerator.getInstance().getPositionRectFromPosition(new Vector2(field.bottomLeft()));
		FieldRasterGenerator.getInstance().getPositionRectFromPosition(new Vector2(field.bottomRight()));
		
		// test position out of field bounds
		float fieldXhalf = AIConfig.getGeometry().getFieldLength() / 2;
		float fieldYhalf = AIConfig.getGeometry().getFieldWidth() / 2;
		
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
			FieldRasterGenerator.getInstance().getPositionRectFromPosition(position);
			fail("IllegalArgumentException was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
	}
	
}
