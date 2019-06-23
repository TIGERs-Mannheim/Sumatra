/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster;

import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;


/**
 * Layer for the positioning raster.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class PositioningRaster extends ARasterLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public PositioningRaster()
	{
		super(EFieldLayer.POSITIONING_RASTER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayer(Graphics2D g2)
	{
		drawGrid(g2, Color.red, rasterConfig.getNumberOfColumns(), rasterConfig.getNumberOfRows());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
