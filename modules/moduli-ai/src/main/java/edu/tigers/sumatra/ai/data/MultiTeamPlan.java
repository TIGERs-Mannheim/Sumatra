/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import static edu.tigers.sumatra.ai.data.MultiTeamRobotPlan.EMultiTeamRobotRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MultiTeamPlan
{
	private final Map<BotID, MultiTeamRobotPlan> robotPlans = new HashMap<>();
	
	
	public Map<BotID, MultiTeamRobotPlan> getRobotPlans()
	{
		return robotPlans;
	}
	
	
	/**
	 * Get robot plans for certain roles
	 * 
	 * @param role
	 * @return
	 */
	public List<MultiTeamRobotPlan> getRobotPlans(EMultiTeamRobotRole role)
	{
		return robotPlans.values().stream()
				.filter(p -> p.getRole() == role)
				.collect(Collectors.toList());
	}
}
