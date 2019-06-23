/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario;

import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit.ASimStopCriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit.SimStopGameStateReached;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeDirectKickSimScenario extends AUnitTestSimScenario
{
	@Override
	protected void setupStopCriteria(final List<ASimStopCriterion> criteria)
	{
		super.setupStopCriteria(criteria);
		criteria.add(new SimStopGameStateReached(EGameStateTeam.RUNNING));
	}
	
	
	@Override
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		setupBotsFormation1(bots);
	}
	
	
	@Override
	protected SimulationObject setupBall()
	{
		return new SimulationObject(
				new Vector3((Geometry.getFieldLength() / 2.0) - 200, (Geometry.getFieldWidth() / 2.0) - 200, 0));
	}
	
	
	@Override
	public void onUpdate(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> caseMsgs)
	{
		// give the bots some time
		if (getSimTimeSinceStart(aiFrame.getWorldFrame().getTimestamp()) < getPreparationTime())
		{
			return;
		}
		
		super.onUpdate(aiFrame, caseMsgs);
		
		if (aiFrame.getTeamColor() == ETeamColor.BLUE)
		{
			return;
		}
		EGameStateTeam gameState = aiFrame.getTacticalField().getGameState();
		if (!((gameState == EGameStateTeam.CORNER_KICK_WE) || (gameState == EGameStateTeam.GOAL_KICK_WE)
				|| (gameState == EGameStateTeam.DIRECT_KICK_WE)))
		{
			addErrorMessage("Unexpected gameState: " + gameState);
		}
	}
	
	
	@Override
	public void afterSimulation(final AIInfoFrame aiFrame)
	{
		if (aiFrame.getTacticalField().getGameState() != EGameStateTeam.RUNNING)
		{
			addErrorMessage("Not in expected gamestate RUNNING: " + aiFrame.getTacticalField().getGameState().name());
		}
	}
	
	
	@Override
	protected Command getInitialRefereeCmd()
	{
		return Command.DIRECT_FREE_YELLOW;
	}
}
