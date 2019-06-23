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

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg.EMsgType;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TooNearToBallCase extends ARefereeCase
{
	
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		EGameStateTeam gs = frame.getTacticalField().getGameState();
		switch (gs)
		{
			case DIRECT_KICK_WE:
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case PREPARE_KICKOFF_WE:
			{
				// other bots should stay out of stop radius
				for (ITrackedBot bot : frame.getWorldFrame().getFoeBots().values())
				{
					checkBot(frame, caseMsgs, bot);
				}
			}
				break;
			case CORNER_KICK_THEY:
			case DIRECT_KICK_THEY:
			case GOAL_KICK_THEY:
			case THROW_IN_THEY:
			case PREPARE_KICKOFF_THEY:
			{
				// we should stay out of stop radius
				for (ITrackedBot bot : frame.getWorldFrame().getTigerBotsVisible().values())
				{
					checkBot(frame, caseMsgs, bot);
				}
			}
				break;
			case STOPPED:
			{
				for (ITrackedBot bot : frame.getWorldFrame().getBots().values())
				{
					checkBot(frame, caseMsgs, bot);
				}
			}
				break;
			default:
		}
	}
	
	
	private void checkBot(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs, final ITrackedBot bot)
	{
		double dist = GeoMath.distancePP(frame.getWorldFrame().getBall().getPos(), bot.getPos())
				- Geometry.getBotRadius();
		if (dist < Geometry
				.getBotToBallDistanceStop())
		{
			RefereeCaseMsg msg = new RefereeCaseMsg(bot.getTeamColor(), EMsgType.TOO_NEAR_TO_BALL);
			msg.setBotAtFault(bot.getBotId());
			msg.setAdditionalInfo("Dist: " + (int) (dist) + "mm");
			caseMsgs.add(msg);
		}
	}
}
