/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryPlacementRole;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 *         Play that handles automated Ballplacement
 */
public class AutomatedThrowInPlay extends APlay
{
	/**
	 * constructor for AutomatedThrowInPlay
	 */
	public AutomatedThrowInPlay()
	{
		super(EPlay.AUTOMATED_THROW_IN);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		// First add a primary role, then a secondary role.
		if (getRoles().size() == 1)
		{
			if (getRoles().get(0).getType() == ERole.SECONDARY_AUTOMATED_THROW_IN)
			{
				return new PrimaryPlacementRole();
			} else if (getRoles().get(0).getType() == ERole.PRIMARY_AUTOMATED_THROW_IN)
			{
				return new SecondaryPlacementRole();
			}
		} else if (getRoles().isEmpty())
		{
			return new PrimaryPlacementRole();
		}
		throw new IllegalArgumentException("something went wrong");
	}
}
