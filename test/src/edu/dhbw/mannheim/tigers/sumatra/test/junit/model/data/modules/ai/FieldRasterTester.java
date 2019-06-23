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

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator.EGeneratorTyp;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;


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
	private FieldRasterConfig		rasterConfig	= null;
	private FieldRasterGenerator	raster;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public FieldRasterTester()
	{
		
		// Load configuration
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, "ai_default.xml");
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, "RoboCup_2012.xml");
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
		
		
		rasterConfig = AIConfig.getFieldRaster();
		raster = new FieldRasterGenerator(EGeneratorTyp.MAIN);
		
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
		final int fieldNumber = (rasterConfig.getNumberOfColumns() * rasterConfig.getNumberOfRows()) + 1;
		raster.getPosFieldRectangle(fieldNumber);
	}
	
	
	/**
	 */
	@Test
	public void testGetPositionRectFromPosition()
	{
		final Rectanglef field = AIConfig.getGeometry().getField();
		
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
