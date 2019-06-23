/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.SimulationParameters.SimulationObject;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefaultSimScenario extends ASimulationScenario
{
	
	@Override
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		float y = 1000;
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
