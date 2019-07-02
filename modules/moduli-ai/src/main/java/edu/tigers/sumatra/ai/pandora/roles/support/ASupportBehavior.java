/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.statemachine.AState;


/**
 * This is the base class for all supportive behaviors.
 */
public abstract class ASupportBehavior extends AState
{
	private final ARole role;
	
	
	public ASupportBehavior(final ARole role)
	{
		super();
		this.role = role;
	}
	
	
	/**
	 * Calculates the viability [0,1], whether this behavior could be appropriate for the given bot in this situation.
	 */
	public abstract double calculateViability();

	/**
	 * Normally you want to return the static isActive configurable of the class
	 * @return true if the behaviour is currently enabled
	 */
	public abstract boolean getIsActive();
	
	
	protected ARole getRole()
	{
		return role;
	}
	
}

