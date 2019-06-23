/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

/**
 * Used to propagate new ai-configs
 * 
 * @author Gero
 */
public interface IAIConfigObserver
{
	/**
	 * This function notifies the actual field raster information to observers.
	 * It can be used to visualize the positioning field raster of AI-Module. Pay attention on
	 * the numbering of sub-fields / sub-rectangles. The raster numbering starts top left form left to right.
	 * See ({@link "http://tigers-mannheim.de/trac/wiki/Informatik#Spielfeld"}) for field orientation.
	 * Field raster will be loaded at start of AI Module.
	 * 
	 * @param newFieldRasterConfig the config
	 */
	void onNewFieldRaster(FieldRasterConfig newFieldRasterConfig);
}
