/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.SimulationParameters.SimulationObject;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefaultSimScenario extends ASimulationScenario
{
	
	@Override
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		double y = 1000;
		for (int i = 0; i < 6; i++)
		{
			SimulationObject sob = new SimulationObject();
			sob.setPos(new Vector3(-1000 + (300 * i), y, 0));
			bots.put(BotID.createBotId(i, ETeamColor.BLUE), sob);
			SimulationObject soy = new SimulationObject();
			soy.setPos(new Vector3(-1000 + (300 * i), -y, 0));
			bots.put(BotID.createBotId(i, ETeamColor.YELLOW), soy);
		}
	}
	
}
