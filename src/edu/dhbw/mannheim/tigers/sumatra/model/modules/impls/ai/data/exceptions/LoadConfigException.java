/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - field raster generator
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;


/**
 * 
 * This exception will be thrown in {@link FieldRasterGenerator} when an error occurs loading the field raster
 * configuration.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class LoadConfigException extends Exception
{
	private static final long	serialVersionUID	= -5909125007954965181L;
	
	
	public LoadConfigException(String msg)
	{
		super(msg);
	}
}
