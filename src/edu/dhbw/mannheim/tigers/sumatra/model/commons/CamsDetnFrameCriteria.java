/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.07.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.commons;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.ICriteria;

/**
 * Determines whether a {@link CamDetectionFrame} belongs to a camera ({@link Director}).
 * 
 * @author Gero
 * 
 */
public class CamsDetnFrameCriteria implements ICriteria<CamDetectionFrame>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public final int camId;

	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CamsDetnFrameCriteria(int camId) {
		this.camId = camId;
	}


	@Override
	public boolean matches(CamDetectionFrame object)
	{
		return object.cameraId == camId;
	}
}
