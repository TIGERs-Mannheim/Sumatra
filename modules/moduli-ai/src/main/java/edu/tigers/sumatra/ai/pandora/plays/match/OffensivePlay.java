/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.match;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ids.BotID;


/**
 * The offensive play coordinates its roles
 */
public class OffensivePlay extends APlay
{
	private static final Logger log = Logger.getLogger(OffensivePlay.class.getName());
	
	private final Map<EOffensiveStrategy, ERole> strategyToRoleMap = new EnumMap<>(EOffensiveStrategy.class);
	
	
	/**
	 * offensive Play
	 */
	public OffensivePlay()
	{
		super(EPlay.OFFENSIVE);
		
		strategyToRoleMap.put(EOffensiveStrategy.KICK, ERole.ATTACKER);
		strategyToRoleMap.put(EOffensiveStrategy.SUPPORTIVE_ATTACKER, ERole.SUPPORTIVE_ATTACKER);
		strategyToRoleMap.put(EOffensiveStrategy.STOP, ERole.KEEP_DIST_TO_BALL);
		strategyToRoleMap.put(EOffensiveStrategy.DELAY, ERole.DELAYED_ATTACK);
		strategyToRoleMap.put(EOffensiveStrategy.FREE_SKIRMISH, ERole.FREE_SKIRMISH);
		strategyToRoleMap.put(EOffensiveStrategy.INTERCEPT, ERole.OPPONENT_INTERCEPTION);
		strategyToRoleMap.put(EOffensiveStrategy.RECEIVE_PASS, ERole.PASS_RECEIVER);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new AttackerRole();
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		super.updateBeforeRoles(frame);
		final Map<BotID, EOffensiveStrategy> playConfiguration = getAiFrame().getTacticalField()
				.getOffensiveStrategy().getCurrentOffensivePlayConfiguration();
		
		for (ARole role : new ArrayList<>(getRoles()))
		{
			final EOffensiveStrategy strategy = playConfiguration.get(role.getBotID());
			ERole requiredRole = strategyToRoleMap.get(strategy);
			if (requiredRole == null)
			{
				// not sure if I want to send a warning here default silently for now
				requiredRole = ERole.ATTACKER;
			}
			
			if (requiredRole != role.getType())
			{
				try
				{
					ARole newRole = (ARole) requiredRole.getInstanceableClass().newDefaultInstance();
					switchRoles(role, newRole);
				} catch (InstanceableClass.NotCreateableException e)
				{
					log.error("Could not create role " + requiredRole, e);
				}
			}
		}
	}
}
