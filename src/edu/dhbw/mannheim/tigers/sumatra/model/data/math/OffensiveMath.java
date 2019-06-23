/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.04.2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.awt.Color;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * Math and methods for the offensiveStrategyCalculator
 * (There is just too much chaos in AiMath)
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveMath
{
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	public static BotIDMap<TrackedTigerBot> getPotentialOffensiveBotMap(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		BotIDMap<TrackedTigerBot> botMap = new BotIDMap<TrackedTigerBot>();
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			if (newTacticalField.getOffenseMovePositions().containsKey(key))
			{
				botMap.put(key, baseAiFrame.getWorldFrame().getTigerBotsVisible().get(key));
			}
		}
		if (baseAiFrame.getKeeperId() != null)
		{
			if (botMap.containsKey(baseAiFrame.getKeeperId()))
			{
				botMap.remove(baseAiFrame.getKeeperId());
			}
		}
		return botMap;
	}
	
	
	/**
	 * @param wFrame
	 * @param ourPenAreaMargin
	 * @param theirPenAreaMargin
	 * @return
	 */
	public static boolean isBallNearPenaltyAreaOrOutsideField(final WorldFrame wFrame, final float ourPenAreaMargin,
			final float theirPenAreaMargin)
	{
		PenaltyArea ourPenArea = AIConfig.getGeometry().getPenaltyAreaOur();
		PenaltyArea theirPenArea = AIConfig.getGeometry().getPenaltyAreaTheir();
		IVector2 ballPos = wFrame.getBall().getPos();
		
		if (ourPenArea.isPointInShape(ballPos, ourPenAreaMargin))
		{
			return true;
		}
		if (theirPenArea.isPointInShape(ballPos, theirPenAreaMargin))
		{
			return true;
		}
		if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param wFrame
	 * @param botPos
	 * @param target
	 * @return
	 */
	public static boolean isBallRedirectPossible(final WorldFrame wFrame,
			final IVector2 botPos, final IVector2 target)
	{
		IVector2 ballToBot = botPos.subtractNew(wFrame.getBall().getPos()).normalizeNew();
		IVector2 botToTarget = target.subtractNew(botPos).normalizeNew();
		
		float product = ballToBot.scalarProduct(botToTarget);
		if (product < 0)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<TrackedTigerBot> bots)
	{
		if (wFrame.getBall().getVel().getLength2() > 0.5f)
		{
			final float REDIRECT_TOLERANCE = 350f;
			IVector2 ballPos = wFrame.getBall().getPos();
			IVector2 ballVel = wFrame.getBall().getVel();
			
			IVector2 left = new Vector2(ballVel.getAngle() - 0.2f).normalizeNew();
			IVector2 right = new Vector2(ballVel.getAngle() + 0.2f).normalizeNew();
			
			IVector2 futureBall = wFrame.getBall().getPosByVel(0f);
			float dist = GeoMath.distancePP(ballPos, futureBall) - REDIRECT_TOLERANCE;
			
			IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
			IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
			
			IVector2 lp0 = ballPos.addNew(left.multiplyNew(dist + REDIRECT_TOLERANCE));
			IVector2 rp0 = ballPos.addNew(right.multiplyNew(dist + REDIRECT_TOLERANCE));
			
			DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			DrawableTriangle dtri2 = new DrawableTriangle(ballPos, lp0, rp0, new Color(0, 0, 255, 22));
			dtri2.setFill(true);
			DrawableCircle dc = new DrawableCircle(
					new Circle(ballPos.addNew(ballVel.normalizeNew().multiplyNew(150f)), 200f), new Color(255, 0, 0, 100));
			dc.setFill(true);
			
			BotID minID = null;
			float minDist = Float.MAX_VALUE;
			for (BotID key : bots.keySet())
			{
				IVector2 pos = bots.get(key).getPos();
				IVector2 kpos = AiMath.getBotKickerPos(bots.get(key));
				if (dtri.isPointInShape(pos) || dc.isPointInShape(pos)
						|| dtri.isPointInShape(kpos) || dc.isPointInShape(kpos))
				{
					if (GeoMath.distancePP(pos, ballPos) < minDist)
					{
						minDist = GeoMath.distancePP(pos, ballPos);
						minID = key;
					}
				}
			}
			if (minID != null)
			{
				return minID;
			}
		}
		return null;
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final TacticalField newTacticalField)
	{
		BotIDMap<TrackedTigerBot> potentialOffensiveBots = getPotentialOffensiveBotMap(newTacticalField, baseAiFrame);
		return getBestGetter(baseAiFrame, potentialOffensiveBots, newTacticalField);
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param bots
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final IBotIDMap<TrackedTigerBot> bots,
			final TacticalField newTacticalField)
	{
		IBotIDMap<TrackedTigerBot> potentialOffensiveBots = bots;
		float minDist = Float.MAX_VALUE;
		BotID bestBot = null;
		
		if (newTacticalField.getOffenseMovePositions() != null)
		{
			for (BotID key : newTacticalField.getOffenseMovePositions().keySet())
			{
				if (!potentialOffensiveBots.containsKey(key))
				{
					continue;
				}
				
				OffensiveMovePosition destination = newTacticalField.getOffenseMovePositions().get(key);
				float score = destination.getScoring();
				
				if (baseAiFrame.getPrevFrame() != null)
				{
					if (baseAiFrame.getPrevFrame().getPlayStrategy() != null)
					{
						if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE) != null)
						{
							for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE))
							{
								if (role.getBotID() == key)
								{
									// less role switching
									score = (score * 0.9f) - 450f;
									score = Math.max(score, 0f);
								}
							}
						}
					}
				}
				
				if ((newTacticalField.getGameState() == EGameState.STOPPED) ||
						(newTacticalField.getGameState() == EGameState.PREPARE_KICKOFF_WE))
				{
					// "disable" offensive swapping when game is stopped.
					score = score - 8000f;
				}
				
				if (score < minDist)
				{
					minDist = score;
					bestBot = key;
				}
			}
			return bestBot;
		}
		return null;
	}
}
