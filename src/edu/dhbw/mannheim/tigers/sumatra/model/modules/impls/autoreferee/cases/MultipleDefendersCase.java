/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): lukas
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg.EMsgType;


/**
 * @author lukas
 */
public class MultipleDefendersCase extends ARefereeCase
{
	private ETeamColor	teamColor	= null;
	private float			margin		= 0.0f;
	
	
	/**
	 * @param teamColor
	 */
	public MultipleDefendersCase(final ETeamColor teamColor)
	{
		this.teamColor = teamColor;
		margin = AIConfig.getGeometry().getBotRadius();
	}
	
	
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		WorldFrame wFrame = frame.getWorldFrame();
		List<PenaltyArea> penAreas = new ArrayList<>(2);
		penAreas.add(AIConfig.getGeometry().getPenaltyAreaOur());
		penAreas.add(AIConfig.getGeometry().getPenaltyAreaTheir());
		BotID keeper = determineKeeperID();
		
		// Check each bot individually
		for (TrackedTigerBot bot : wFrame.getBots().values())
		{
			if (bot.getId().getTeamColor() != teamColor)
			{
				// Robot's not part of the team we're watching
				continue;
			}
			if (bot.getId().equals(keeper))
			{
				// Keeper is always allowed inside his penalty area
				continue;
			}
			
			// boolean ballInPen = penArea.isPointInShape();
			boolean ballInPen = new Circle(bot.getPos(), 120).isPointInShape(wFrame.getBall().getPos());
			
			for (PenaltyArea penArea : penAreas)
			{
				// Check if fully inside the defense area
				if (penArea.isPointInShape(bot.getPos(), -margin) && ballInPen)
				{
					caseMsgs.add(new RefereeCaseMsg(teamColor, EMsgType.PENALTY_FULL));
				}
				
				if (penArea.isPointInShape(bot.getPos(), margin) && ballInPen)
				{
					caseMsgs.add(new RefereeCaseMsg(teamColor, EMsgType.PENALTY_PARTIAL));
				}
			}
		}
	}
	
	
	private BotID determineKeeperID()
	{
		if (teamColor == ETeamColor.BLUE)
		{
			return BotID.createBotId(TeamConfig.getKeeperIdBlue(), teamColor);
		}
		return BotID.createBotId(TeamConfig.getKeeperIdYellow(), teamColor);
	}
}
