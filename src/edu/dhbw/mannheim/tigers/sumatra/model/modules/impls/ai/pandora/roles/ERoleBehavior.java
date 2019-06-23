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


/**
 * This enumeration classifies a {@link ARole}s behavior during a certain
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay}
 * 
 * @author Gero, Oliver
 * 
 */
@Deprecated
public enum ERoleBehavior
{
	/** Means that the role want to get somewhere near the goal */
	DEFENSIVE,
	
	/** Information about these roles target positions have to be provided by the certain play */
	CREATIVE,
	
	/** Roles with this tag want to get somewhere near the ball */
	AGGRESSIVE,
	
	/** not yet set */
	UNKNOWN;
}
