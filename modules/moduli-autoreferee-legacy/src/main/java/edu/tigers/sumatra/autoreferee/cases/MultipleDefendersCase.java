/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): lukas
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee.cases;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg.EMsgType;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author lukas
 */
public class MultipleDefendersCase extends ARefereeCase
{
	private ETeamColor	teamColor	= null;
	private double			margin		= 0.0;
	
	
	/**
	 * @param teamColor
	 */
	public MultipleDefendersCase(final ETeamColor teamColor)
	{
		this.teamColor = teamColor;
		margin = Geometry.getBotRadius();
	}
	
	
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		WorldFrame wFrame = frame.getWorldFrame();
		List<PenaltyArea> penAreas = new ArrayList<>(2);
		penAreas.add(Geometry.getPenaltyAreaOur());
		penAreas.add(Geometry.getPenaltyAreaTheir());
		BotID keeper = frame.getKeeperId();
		
		// Check each bot individually
		for (ITrackedBot bot : wFrame.getBots().values())
		{
			if (bot.getBotId().getTeamColor() != teamColor)
			{
				// Robot's not part of the team we're watching
				continue;
			}
			if (bot.getBotId().equals(keeper))
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
}
