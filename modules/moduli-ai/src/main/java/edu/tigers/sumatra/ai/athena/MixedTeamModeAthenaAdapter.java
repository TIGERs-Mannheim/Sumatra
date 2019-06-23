/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena;

import static edu.tigers.sumatra.ai.data.MultiTeamRobotPlan.EMultiTeamRobotRole;

import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;


/**
 * Adapter for mixed team mode
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MixedTeamModeAthenaAdapter extends MatchModeAthenaAdapter
{
	private final Map<ERole, EMultiTeamRobotRole> roleMap = new EnumMap<>(ERole.class);
	
	
	/**
	 * Default
	 */
	public MixedTeamModeAthenaAdapter()
	{
		roleMap.put(ERole.KEEPER, EMultiTeamRobotRole.GOALIE);
		roleMap.put(ERole.ONE_ON_ONE_KEEPER, EMultiTeamRobotRole.GOALIE);
		roleMap.put(ERole.DEFENDER_PLACEHOLDER, EMultiTeamRobotRole.DEFENSE);
		roleMap.put(ERole.CENTER_BACK, EMultiTeamRobotRole.DEFENSE);
		roleMap.put(ERole.MAN_TO_MAN_MARKER, EMultiTeamRobotRole.DEFENSE);
		roleMap.put(ERole.OFFENSIVE, EMultiTeamRobotRole.OFFENSE);
		roleMap.put(ERole.EPIC_PENALTY_SHOOTER, EMultiTeamRobotRole.OFFENSE);
		roleMap.put(ERole.KICKOFF_SHOOTER, EMultiTeamRobotRole.OFFENSE);
		roleMap.put(ERole.PENALTY_ATTACKER, EMultiTeamRobotRole.OFFENSE);
		roleMap.put(ERole.SUPPORT, EMultiTeamRobotRole.OFFENSE);
	}
	
	
	@Override
	public void doProcess(final MetisAiFrame metisAiFrame, final PlayStrategy.Builder playStrategyBuilder,
			final AIControl aiControl)
	{
		super.doProcess(metisAiFrame, playStrategyBuilder, aiControl);
		
		for (APlay play : playStrategyBuilder.getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				EMultiTeamRobotRole robotRole = roleMap.getOrDefault(role.getType(), EMultiTeamRobotRole.DEFAULT);
				metisAiFrame.getTacticalField().getMultiTeamPlan().getRobotPlans()
						.put(role.getBotID(), createRobotPlan(role.getBotID(), robotRole));
			}
		}
	}
	
	
	private MultiTeamRobotPlan createRobotPlan(BotID botID, EMultiTeamRobotRole role)
	{
		MultiTeamRobotPlan robotPlan = new MultiTeamRobotPlan(botID);
		robotPlan.setRole(role);
		return robotPlan;
	}
	
}
