/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam.data;

import java.util.Map;


/**
 * Geometry information of SSL vision
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamGeometry
{
	/** camId -> calibration */
	private final Map<Integer, CamCalibration> calibrations;
	private CamFieldSize field;
	
	
	/**
	 * @param calibrations
	 * @param field
	 */
	public CamGeometry(final Map<Integer, CamCalibration> calibrations, final CamFieldSize field)
	{
		super();
		this.calibrations = calibrations;
		this.field = field;
	}
	
	
	/**
	 * @return the calibration
	 */
	public final Map<Integer, CamCalibration> getCalibrations()
	{
		return calibrations;
	}
	
	
	/**
	 * @return the field
	 */
	public final CamFieldSize getField()
	{
		return field;
	}
	
	
	/**
	 * Update geometry.
	 * 
	 * @param update
	 */
	public void update(final CamGeometry update)
	{
		calibrations.putAll(update.getCalibrations());
		field = update.getField();
	}
}
