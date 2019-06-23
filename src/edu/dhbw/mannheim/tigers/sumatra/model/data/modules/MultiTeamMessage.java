/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * @author JulianT
 */
public class MultiTeamMessage
{
	private TeamPlan	teamPlan;
	
	
	/**
	 * @param teamPlan
	 */
	public MultiTeamMessage(final TeamPlan teamPlan)
	{
		this.teamPlan = teamPlan;
	}
	
	
	/**
	 * @return
	 */
	public TeamPlan getTeamPlan()
	{
		return teamPlan;
	}
	
	
	/**
	 * @param teamColor
	 * @return
	 */
	public Map<BotID, RobotPlan> getRobotPlans(final ETeamColor teamColor)
	{
		Map<BotID, RobotPlan> robotPlanMap = new HashMap<>();
		
		for (RobotPlan robotPlan : teamPlan.getPlansList())
		{
			robotPlanMap.put(BotID.createBotId(robotPlan.getRobotId(), teamColor), robotPlan);
		}
		
		return robotPlanMap;
	}
}
