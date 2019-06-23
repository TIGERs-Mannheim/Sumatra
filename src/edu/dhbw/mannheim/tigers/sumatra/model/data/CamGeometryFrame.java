/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Data holder for all meta-information concerning the camera image respectively the data extracted from it.
 * 
 * @author Gero
 * 
 */
public class CamGeometryFrame implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long				serialVersionUID	= -3628553597156386921L;
	

	public final CamFieldGeometry			fieldGeometry;
	public final List<CamCalibration>	cameraCalibration;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param fieldGeometry
	 * @param cameraCalibration
	 */
	public CamGeometryFrame(CamFieldGeometry fieldGeometry, List<CamCalibration> cameraCalibration)
	{
		this.fieldGeometry = fieldGeometry;
		this.cameraCalibration = Collections.unmodifiableList(cameraCalibration);
	}
	

	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public CamGeometryFrame(CamGeometryFrame original)
	{
		this.fieldGeometry = original.fieldGeometry;
		this.cameraCalibration = Collections.unmodifiableList(new ArrayList<CamCalibration>(original.cameraCalibration));
	}
}
