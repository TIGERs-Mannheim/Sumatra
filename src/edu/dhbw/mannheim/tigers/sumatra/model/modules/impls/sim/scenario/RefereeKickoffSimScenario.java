/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario;

import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.ASimStopCriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.SimStopGameStateReached;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.SimStopNoMoveCrit;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeKickoffSimScenario extends AUnitTestSimScenario
{
	@Override
	protected void setupStopCriteria(final List<ASimStopCriterion> criteria)
	{
		super.setupStopCriteria(criteria);
		criteria.add(new SimStopGameStateReached(EGameState.RUNNING));
	}
	
	
	@Override
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		setupBotsFormation1(bots);
	}
	
	
	@Override
	protected SimulationObject setupBall()
	{
		return new SimulationObject(new Vector3(0, 0, 0));
	}
	
	
	@Override
	public void onUpdate(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> caseMsgs)
	{
		// give the bots some time
		if (getSimTimeSinceStart() < getPreparationTime())
		{
			return;
		}
		if (aiFrame.getLatestRefereeMsg().getCommand() != Command.NORMAL_START)
		{
			newRefereeMsg(Command.NORMAL_START, 0, 0, 100);
			getStopCriteria().add(new SimStopNoMoveCrit());
		}
		
		super.onUpdate(aiFrame, caseMsgs);
		
		if (aiFrame.getTeamColor() == ETeamColor.BLUE)
		{
			return;
		}
		EGameState gameState = aiFrame.getTacticalField().getGameState();
		if ((gameState != EGameState.PREPARE_KICKOFF_WE))
		{
			addErrorMessage("Unexpected gameState: " + gameState);
		}
	}
	
	
	@Override
	public void afterSimulation(final AIInfoFrame aiFrame)
	{
		if (aiFrame.getTacticalField().getGameState() != EGameState.RUNNING)
		{
			addErrorMessage("Not in expected gamestate RUNNING: " + aiFrame.getTacticalField().getGameState().name());
		}
	}
	
	
	@Override
	protected Command getInitialRefereeCmd()
	{
		return Command.PREPARE_KICKOFF_YELLOW;
	}
}
