/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooter;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class OneOnOneShootoutPlay extends APlay
{
	
	/**
	 * Creates a new OneOnOneShootoutPlay
	 */
	public OneOnOneShootoutPlay()
	{
		
		super(EPlay.ATTACKER_SHOOTOUT);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		
		return new OneOnOneShooter();
	}
}
