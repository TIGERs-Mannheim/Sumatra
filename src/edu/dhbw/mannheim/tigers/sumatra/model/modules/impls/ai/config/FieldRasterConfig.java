/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.exceptions.LoadConfigException;


/**
 * Configuration object for the field raster.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class FieldRasterConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String	NODE_PATH	= "calculators.fieldRaster.";
	
	private final int					numberOfRows;
	private final int					numberOfColumns;
	private final int					numberOfPositioningFields;
	private final int					numberOfAnalysingFields;
	private final int					analysingFactor;
	private final int					analysingMode;
	private final int					analysingKind;
	private final int					playfinderNumberOfColumns;
	private final int					playfinderNumberOfRows;
	private final int					playfinderAnalysingFactor;
	private final int					playfinderNumberOfAnalysingFields;
	private final int					playfnderAnalysingMode;
	
	
	private final int					playfinderAnalysingKind;
	private final int					playfinderNumberOfPositioningFields;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 * @throws LoadConfigException
	 */
	public FieldRasterConfig(Configuration configFile) throws LoadConfigException
	{
		// Main
		numberOfRows = verifyInt(configFile.getInt(NODE_PATH + "main.positioning.rows"));
		numberOfColumns = verifyInt(configFile.getInt(NODE_PATH + "main.positioning.columns"));
		
		// check if xml-config entries (row / column) are valid
		if ((numberOfColumns == -1) || (numberOfRows == -1))
		{
			throw new LoadConfigException(
					"Invalid field positioning raster configuration. Please use a power of two for row and column size.");
		}
		numberOfPositioningFields = numberOfColumns * numberOfRows;
		analysingFactor = configFile.getInt(NODE_PATH + "main.analysing.factor");
		analysingMode = configFile.getInt(NODE_PATH + "main.analysing.mode");
		analysingKind = configFile.getInt(NODE_PATH + "main.analysing.kind");
		numberOfAnalysingFields = numberOfPositioningFields * analysingFactor * analysingFactor;
		
		// PLayfinder
		playfinderNumberOfRows = verifyInt(configFile.getInt(NODE_PATH + "playfinder.positioning.rows"));
		playfinderNumberOfColumns = verifyInt(configFile.getInt(NODE_PATH + "playfinder.positioning.columns"));
		if ((playfinderNumberOfRows == -1) || (playfinderNumberOfColumns == -1))
		{
			throw new LoadConfigException(
					"Invalid field positioning raster configuration. Please use a power of two for row and column size.");
		}
		playfinderNumberOfPositioningFields = playfinderNumberOfRows * playfinderNumberOfColumns;
		playfinderAnalysingFactor = configFile.getInt(NODE_PATH + "playfinder.analysing.factor");
		playfnderAnalysingMode = configFile.getInt(NODE_PATH + "playfinder.analysing.mode");
		playfinderAnalysingKind = configFile.getInt(NODE_PATH + "playfinder.analysing.kind");
		
		playfinderNumberOfAnalysingFields = playfinderNumberOfPositioningFields * playfinderAnalysingFactor
				* playfinderAnalysingFactor;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This function is used to check the input of the configuration file. Value must be a power of two.
	 * 
	 * @param value to check
	 * @return -1 when false
	 */
	private int verifyInt(int value)
	{
		// --- proofs if value is a power of two. When return is negative then verify has failed. ---
		if ((value != 1) && (java.lang.Integer.bitCount(value) == 1))
		{
			return value;
		}
		return -1;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public int getNumberOfRows()
	{
		return numberOfRows;
	}
	
	
	/**
	 * @return
	 */
	public int getNumberOfColumns()
	{
		return numberOfColumns;
	}
	
	
	/**
	 * @return
	 */
	public int getNumberOfPositioningFields()
	{
		return numberOfPositioningFields;
	}
	
	
	/**
	 * @return
	 */
	public int getAnalysingFactor()
	{
		return analysingFactor;
	}
	
	
	/**
	 * @return
	 */
	public int getAnalysingMode()
	{
		return analysingMode;
	}
	
	
	/**
	 * @return
	 */
	public int getAnalysingKind()
	{
		return analysingKind;
	}
	
	
	/**
	 * @return
	 */
	public int getNumberOfAnalysingFields()
	{
		return numberOfAnalysingFields;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getPlayfinderNumberOfColumns()
	{
		return playfinderNumberOfColumns;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getPlayfinderNumberOfRows()
	{
		return playfinderNumberOfRows;
	}
	
	
	/**
	 * @return the numberOfSmallAnalysingFields
	 */
	public int getPlayfinderNumberOfAnalysingFields()
	{
		return playfinderNumberOfAnalysingFields;
	}
	
	
	/**
	 * @return the playfnderAnalysingMode
	 */
	public int getPlayfnderAnalysingMode()
	{
		return playfnderAnalysingMode;
	}
	
	
	/**
	 * @return the playfinderAnalysingKind
	 */
	public int getPlayfinderAnalysingKind()
	{
		return playfinderAnalysingKind;
	}
	
	
	/**
	 * @return the playfinderNumberOfPositioningFields
	 */
	public int getPlayfinderNumberOfPositioningFields()
	{
		return playfinderNumberOfPositioningFields;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getPlayfinderAnalysingFactor()
	{
		return playfinderAnalysingFactor;
	}
}
