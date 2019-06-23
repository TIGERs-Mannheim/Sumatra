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

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions.LoadConfigException;


/**
 * Configuration object for the field raster.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class FieldRaster
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String	nodePath	= "fieldRaster.";
	
	private final int		numberOfRows;
	private final int		numberOfColumns;
	private final int		analysingFactor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public FieldRaster(XMLConfiguration configFile) throws LoadConfigException
	{
		numberOfRows = verifyInt(configFile.getInt(nodePath + "positioning.rows"));
		numberOfColumns = verifyInt(configFile.getInt(nodePath + "positioning.columns"));
		
		// check if xml-config entries (row / column) are valid
		if (numberOfColumns == -1 || numberOfRows == -1)
		{
			throw new LoadConfigException(
					"Invalid field positioning raster configuration. Please use a power of two for row and column size.");
		}
		
		analysingFactor = configFile.getInt(nodePath + "analysing.factor");
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
		if (value != 1 && java.lang.Integer.bitCount(value) == 1)
		{
			return value;
		}
		return -1;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int getNumberOfRows()
	{
		return numberOfRows;
	}
	

	public int getNumberOfColumns()
	{
		return numberOfColumns;
	}
	

	public int getAnalysingFactor()
	{
		return analysingFactor;
	}
	

}
