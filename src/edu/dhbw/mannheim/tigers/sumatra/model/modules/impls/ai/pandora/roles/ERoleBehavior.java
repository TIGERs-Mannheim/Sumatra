/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.11.2010
 * Author(s): Gero
 * Oliver
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;

/**
 * This enumeration classifies a {@link ARole}s behavior during a certain {@link APlay}
 * 
 * @author Gero, Oliver
 * 
 */
public enum ERoleBehavior
{
	/** Means that the role want to get somewhere near the goal */
	DEFENSIVE,
	
	/** Information about these roles target positions have to be provided by the certain play */
	CREATIVE,
	
	/** Roles with this tag want to get somewhere near the ball */
	AGGRESSIVE;
}
