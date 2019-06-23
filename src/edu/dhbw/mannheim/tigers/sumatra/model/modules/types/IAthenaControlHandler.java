/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;


/**
 * Encapsulates the capability of handling {@link AthenaControl}-objects.
 * 
 * @author Gero
 */
public interface IAthenaControlHandler
{
	/**
	 * Passes the control-object with new instructions
	 * 
	 * @param newControl
	 */
	void onNewAthenaControl(AthenaControl newControl);
}