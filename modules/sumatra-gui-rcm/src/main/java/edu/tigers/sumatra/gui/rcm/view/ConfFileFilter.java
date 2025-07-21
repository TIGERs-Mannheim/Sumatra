/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.rcm.view;

import javax.swing.filechooser.FileFilter;
import java.io.File;


/**
 * This class is a file filter for the JFileChooser (see SaveConfigAction, LoadConfigAction).
 * Filter for *.rcc files.
 * 
 * @author Lukas
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
	public boolean accept(final File f)
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
