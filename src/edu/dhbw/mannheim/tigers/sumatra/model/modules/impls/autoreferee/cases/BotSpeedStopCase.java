/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg.EMsgType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSpeedStopCase extends ARefereeCase
{
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		for (TrackedTigerBot bot : frame.getWorldFrame().getBots().values())
		{
			if (bot.getVel().getLength2() > (AIConfig.getGeometry().getStopSpeed() + 0.1))
			{
				RefereeCaseMsg msg = new RefereeCaseMsg(bot.getTeamColor(), EMsgType.BOT_SPEED_STOP);
				msg.setBotAtFault(bot.getId());
				msg.setAdditionalInfo("Vel: " + bot.getVel().getLength2() + "m/s");
				caseMsgs.add(msg);
			}
		}
	}
}
