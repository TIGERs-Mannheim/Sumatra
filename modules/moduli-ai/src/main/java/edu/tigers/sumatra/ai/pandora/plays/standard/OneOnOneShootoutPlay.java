/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
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
	protected ARole onAddRole()
	{
		
		return new OneOnOneShooter();
	}
}
