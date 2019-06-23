/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg.EMsgType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickOffCase extends ARefereeCase
{
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		EGameState gameState = frame.getTacticalField().getGameState();
		if ((gameState == EGameState.PREPARE_KICKOFF_WE))
		{
			for (TrackedTigerBot bot : frame.getWorldFrame().getFoeBots().values())
			{
				// opponents in their half?
				if (bot.getPos().x() < AIConfig.getGeometry().getBotRadius())
				{
					RefereeCaseMsg msg = new RefereeCaseMsg(bot.getTeamColor(), EMsgType.KICKOFF_PLACEMENT);
					msg.setBotAtFault(bot.getId());
				}
			}
			for (TrackedTigerBot bot : frame.getWorldFrame().getTigerBotsVisible().values())
			{
				// in our half?
				if (bot.getPos().x() > -AIConfig.getGeometry().getBotRadius())
				{
					RefereeCaseMsg msg = new RefereeCaseMsg(bot.getTeamColor(), EMsgType.KICKOFF_PLACEMENT);
					msg.setBotAtFault(bot.getId());
				}
			}
		}
	}
}
