/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee.cases;

import java.util.List;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg.EMsgType;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSpeedStopCase extends ARefereeCase
{
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		for (ITrackedBot bot : frame.getWorldFrame().getBots().values())
		{
			if (bot.getVel().getLength2() > (Geometry.getStopSpeed() + 0.1))
			{
				RefereeCaseMsg msg = new RefereeCaseMsg(bot.getBotId().getTeamColor(), EMsgType.BOT_SPEED_STOP);
				msg.setBotAtFault(bot.getBotId());
				msg.setAdditionalInfo("Vel: " + bot.getVel().getLength2() + "m/s");
				caseMsgs.add(msg);
			}
		}
	}
}
