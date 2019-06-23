/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario;

import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit.ASimStopCriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit.SimStopNoMoveCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit.SimStopTimeCrit;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TestSimScenario extends ASimulationScenario
{
	
	/**
	 * 
	 */
	public TestSimScenario()
	{
		setEnableBlue(true);
		setEnableYellow(true);
	}
	
	
	@Override
	protected void setupStopCriteria(final List<ASimStopCriterion> criteria)
	{
		criteria.add(new SimStopNoMoveCrit());
		criteria.add(new SimStopTimeCrit(5000));
	}
	
	
	@Override
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		setupBotsFormation1(bots);
	}
	
	
	@Override
	protected void setupInitialTacticalField(final TacticalField tacticalField)
	{
	}
	
	
	@Override
	protected Command getInitialRefereeCmd()
	{
		return Command.STOP;
	}
	
	
	@Override
	protected SimulationObject setupBall()
	{
		return new SimulationObject(new Vector3(100, 100, 0));
	}
	
	
	@Override
	public void onUpdate(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> caseMsgs)
	{
		if ((aiFrame.getRefereeMsg() == null) && (getSimTimeSinceStart(aiFrame.getWorldFrame().getTimestamp()) > 1))
		{
			newRefereeMsg(SSL_Referee.Command.DIRECT_FREE_YELLOW, 0, 0, 10, aiFrame.getWorldFrame().getTimestamp());
		}
	}
}
