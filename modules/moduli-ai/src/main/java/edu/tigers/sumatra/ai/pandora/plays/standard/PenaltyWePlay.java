/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooterRole;


/**
 * Handle some roles, if we have a penalty against the others
 */
public class PenaltyWePlay extends APlay
{
	/**
	 * Default
	 */
	public PenaltyWePlay()
	{
		super(EPlay.PENALTY_WE);
	}


	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		if (!getRoles().isEmpty())
		{
			throw new IllegalStateException("PenaltyWe Play can not handle more than 1 role!");
		}
		return new OneOnOneShooterRole();
	}
}
