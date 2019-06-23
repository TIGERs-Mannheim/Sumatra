/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * This class is a file filter for the JFileChooser (see SaveConfigAction, LoadConfigAction).
 * Filter for *.rcc files.
 * 
 * @author Lukas
 * 
 */

public class ConfFileFilter extends FileFilter
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	private static final String	FILESUFFIX	= ".rcc";
	private static final String	DESCRIPTION	= "Robot Control Configuration (*" + FILESUFFIX + ")";
	
	
	// -----------------------------------------------------------------
	// ----- Constructor -----------------------------------------------
	// -----------------------------------------------------------------
	
	// -----------------------------------------------------------------
	// ----- Methods ---------------------------------------------------
	// -----------------------------------------------------------------
	@Override
	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}
		return f.getName().endsWith(FILESUFFIX);
	}
	
	
	@Override
	public String getDescription()
	{
		return DESCRIPTION;
	}
	
	
	/**
	 * @return
	 */
	public String getFileSuffix()
	{
		return FILESUFFIX;
	}
}
