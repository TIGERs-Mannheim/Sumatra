/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         Simon Sander <Simon.Sander@dlr.de>
 *         ChrisC
 */
public class SupportPlay extends APlay
{
	
	/**
	 * Default constructor
	 */
	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		super.updateBeforeRoles(frame);
		
		// Viability of all Supporter have to be shared before role update
		Map<SupportRole, EnumMap<ESupportBehavior, Double>> viabilityMap = new HashMap<>();
		getRoles()
				.forEach(r -> ((SupportRole) r).exchangeViability(viabilityMap));
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new SupportRole();
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
}
