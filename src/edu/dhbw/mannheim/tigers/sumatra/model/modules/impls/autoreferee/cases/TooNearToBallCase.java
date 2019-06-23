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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg.EMsgType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TooNearToBallCase extends ARefereeCase
{
	
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		EGameState gs = frame.getTacticalField().getGameState();
		switch (gs)
		{
			case DIRECT_KICK_WE:
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case PREPARE_KICKOFF_WE:
			{
				// other bots should stay out of stop radius
				for (TrackedTigerBot bot : frame.getWorldFrame().getFoeBots().values())
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
				for (TrackedTigerBot bot : frame.getWorldFrame().getTigerBotsVisible().values())
				{
					checkBot(frame, caseMsgs, bot);
				}
			}
				break;
			case STOPPED:
			{
				for (TrackedTigerBot bot : frame.getWorldFrame().getBots().values())
				{
					checkBot(frame, caseMsgs, bot);
				}
			}
				break;
			default:
		}
	}
	
	
	private void checkBot(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs, final TrackedTigerBot bot)
	{
		float dist = GeoMath.distancePP(frame.getWorldFrame().getBall().getPos(), bot.getPos())
				- AIConfig.getGeometry().getBotRadius();
		if (dist < AIConfig.getGeometry()
				.getBotToBallDistanceStop())
		{
			RefereeCaseMsg msg = new RefereeCaseMsg(bot.getTeamColor(), EMsgType.TOO_NEAR_TO_BALL);
			msg.setBotAtFault(bot.getId());
			msg.setAdditionalInfo("Dist: " + (int) (dist) + "mm");
			caseMsgs.add(msg);
		}
	}
}
