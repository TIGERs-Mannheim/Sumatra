/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.action.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;


/**
 * @author: MarkG
 */
public class KickInsBlaueActionMove extends AOffensiveActionMove
{
	
	private double viability = 0.0;
	
	
	/**
	 * Kick ins Blaue
	 */
	public KickInsBlaueActionMove()
	{
		super(EOffensiveActionMove.KICK_INS_BLAUE);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		boolean kickInsBlaue = calcKickInsBlaueParams(newTacticalField, baseAiFrame, action);
		if (baseAiFrame.getGamestate().isStandardSituationForUs())
		{
			return EActionViability.FALSE;
		}
		if (kickInsBlaue)
		{
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	@Override
	public void activateAction(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final OffensiveAction action)
	{
		action.setType(OffensiveAction.EOffensiveAction.KICK_INS_BLAUE);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return viability * ActionMoveConstants.getViabilityMultiplierKickInsBlaue();
	}
	
	
	private boolean calcKickInsBlaueParams(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		IVector2 baseTarget = Geometry.getPenaltyMarkTheir().addNew(Vector2.fromXY(-1000, 0));
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		IVector2 ballToTarget = baseTarget.subtractNew(ballPos);
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		
		IVector2 h1 = ballPos.addNew(ballToTarget.scaleToNew(1600).addNew(normal.multiplyNew(250)));
		IVector2 h2 = ballPos.addNew(ballToTarget.scaleToNew(1600).addNew(normal.multiplyNew(-250)));
		
		DrawableTriangle dt = new DrawableTriangle(ballPos, h1, h2, new Color(125, 30, 255, 10));
		dt.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(dt);
		
		IVector2 helperPos = ballPos.addNew(ballToTarget.scaleToNew(1600 + 350.0));
		IVector2 a1 = ballPos.addNew(ballToTarget.scaleToNew(500).addNew(normal.multiplyNew(300)));
		IVector2 a2 = ballPos.addNew(ballToTarget.scaleToNew(500).addNew(normal.multiplyNew(-300)));
		DrawableTriangle dt2 = new DrawableTriangle(ballPos, a1, a2, new Color(0, 230, 255, 30));
		dt2.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(dt2);
		
		boolean freeShootingPos = true;
		
		for (BotID id : baseAiFrame.getWorldFrame().getFoeBots().keySet())
		{
			ITrackedBot bot = baseAiFrame.getWorldFrame().getFoeBot(id);
			IVector2 botPos = bot.getPos();
			if (dt2.getTriangle().isPointInShape(botPos))
			{
				freeShootingPos = false;
			}
			if (!freeShootingPos)
			{
				break;
			}
		}
		
		boolean isFree = true;
		double radius = 500;
		ICircle whatSoEverCircleMark = Circle.createCircle(helperPos, radius);
		boolean firstRun = true;
		boolean freeOnFirst = false;
		
		while (isFree && (radius < 1500))
		{
			for (BotID id : baseAiFrame.getWorldFrame().getFoeBots().keySet())
			{
				ITrackedBot bot = baseAiFrame.getWorldFrame().getFoeBot(id);
				IVector2 botPos = bot.getPos();
				if (whatSoEverCircleMark.isPointInShape(botPos) || dt.getTriangle().isPointInShape(botPos))
				{
					isFree = false;
				}
				if (!isFree)
				{
					radius = radius - 50;
					break;
				}
			}
			if (firstRun && isFree)
			{
				freeOnFirst = true;
				firstRun = false;
			}
			radius = radius + 50;
			whatSoEverCircleMark = Circle.createCircle(helperPos, radius);
		}
		if (freeOnFirst)
		{
			isFree = true;
		}
		
		// yeah.... to save some calc time.
		viability = (radius - 500) / 1500.0;
		
		DrawableCircle dc = new DrawableCircle(whatSoEverCircleMark, new Color(125, 30, 255, 10));
		dc.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(dc);
		
		DrawableAnnotation dtext = new DrawableAnnotation(helperPos, "free of bots: " + isFree, Color.black);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(dtext);
		DrawableAnnotation dtext2 = new DrawableAnnotation(helperPos.addNew(Vector2.fromXY(100, 0)),
				"can kick: " + freeShootingPos,
				Color.black);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(dtext2);
		
		action.setKickInsBlauePossible(isFree && freeShootingPos);
		action.setKickInsBlaueTarget(helperPos);
		return isFree && freeShootingPos;
	}
}
