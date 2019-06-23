/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - field raster generator
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.exceptions;



/**
 * 
 * This exception will be thrown in
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator} when an error occurs
 * loading the field raster
 * configuration.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class LoadConfigException extends Exception
{
	private static final long	serialVersionUID	= -5909125007954965181L;
	
	
	/**
	 * @param msg
	 */
	public LoadConfigException(String msg)
	{
		super(msg);
	}
}
