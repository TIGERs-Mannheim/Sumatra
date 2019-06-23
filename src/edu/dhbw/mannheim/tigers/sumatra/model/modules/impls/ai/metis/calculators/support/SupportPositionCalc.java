/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * Calculates ideal (offensive) positions for the SupportRole.
 * 
 * @author JulianT
 */
public class SupportPositionCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private static final Logger log = Logger.getLogger(SupportPositionCalc.class.getName());
	private static final int	rayCount	= 40;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		Map<BotID, IVector2> positions = new HashMap<BotID, IVector2>();
		Map<BotID, IVector2> targets = new HashMap<BotID, IVector2>();
		LinkedList<IVector2> intersections = getPositions(baseAiFrame.getPrevFrame(), wFrame);
		
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			positions.put(bot.getId(), getNext(bot.getPos(), intersections));
			targets.put(bot.getId(),
					baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShotTargetBots().get(bot.getId()));
		}
		
		newTacticalField.setSupportPositions(positions);
		newTacticalField.setSupportTargets(targets);
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setSupportPositions(baseAiFrame.getPrevFrame().getTacticalField().getSupportPositions());
		newTacticalField.setSupportTargets(baseAiFrame.getPrevFrame().getTacticalField().getSupportTargets());
	}
	
	
	/**
	 * Computes a list of points that are visible from ball and opponent goal.
	 * 
	 * @param preAiFrame
	 * @param wFrame
	 * @return A bunch of good offensive positions for the SupportRole
	 */
	private LinkedList<IVector2> getPositions(final AIInfoFrame preAiFrame, final WorldFrame wFrame)
	{
		LinkedList<Line> raysBall = new LinkedList<Line>(); // Ununterbrochene Strahlen des Balles auf die erweiterte
																				// gegnerische Torlinie
		LinkedList<Line> raysGoal = new LinkedList<Line>(); // Ununterbrochene Strahlen des gegnerischen Tormittelpunktes
																				// auf die Mittellinie
		LinkedList<IVector2> intersections = new LinkedList<IVector2>(); // Schnittpunkte der Ball- und Torstrahlen
		LinkedList<BotID> ignoreBots = new LinkedList<BotID>();
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 goalPos = AIConfig.getGeometry().getGoalTheir().getGoalPostRight();
		float fieldStepSize = AIConfig.getGeometry().getFieldWidth() / rayCount; // Abstand der Strahlenden zueinander
		float goalStepSize = AIConfig.getGeometry().getGoalSize() / rayCount;
		float yOffset = AIConfig.getGeometry().getFieldWidth() * -0.5f; // Rechteste y-Koordinate des Spielfeldes
		
		// Eigene Bots ignorieren
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			ignoreBots.add(bot.getId());
		}
		
		// Ununterbrochene Strahlen bestimmen
		for (int i = 0; i < rayCount; i++)
		{
			IVector2 endPos = new Vector2(goalPos.x(), yOffset + (i * fieldStepSize));
			// if (GeoMath.p2pVisibility(wFrame, ballPos, endPos, ignoreBots))
			if ((ballPos.x() != endPos.x())
					&& (ballPos.y() != endPos.y())
					&& GeoMath.p2pVisibility(wFrame, ballPos, endPos, AIConfig.getGeometry().getBallRadius() * 2.0f,
							ignoreBots))
			{
				raysBall.add(Line.newLine(ballPos, endPos));
			}
			
			endPos = new Vector2(0, yOffset + (i * fieldStepSize));
			IVector2 startPos = new Vector2(goalPos.x(), goalPos.y() + (i * goalStepSize));
			if ((startPos.x() != endPos.x())
					&& (startPos.y() != endPos.y())
					&& GeoMath.p2pVisibility(wFrame, startPos, endPos, AIConfig.getGeometry().getBallRadius() * 2.0f,
							ignoreBots))
			{
				raysGoal.add(Line.newLine(startPos, endPos));
			}
		}
		
		// S�mtliche Schnittpunkte berechnen
		for (Line ballRay : raysBall)
		{
			for (Line goalRay : raysGoal)
			{
				try
				{
					IVector2 intersectionPoint = GeoMath.intersectionPointOnLine(ballRay, goalRay);
					if (GeoMath.isInsideField(intersectionPoint)
							&& !AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(intersectionPoint))
					{
						intersections.add(intersectionPoint);
					}
				} catch (MathException me)
				{
					// Do nothing?
				}
			}
		}
		
		// Schnittpunkte zurueckgeben
		return intersections;
	}
	
	
	/**
	 * Picks the IVector2 from points that is next to pos
	 * 
	 * @param pos
	 * @param points
	 * @return The picked IVector2 next to pos
	 */
	private IVector2 getNext(final IVector2 pos, final LinkedList<IVector2> points)
	{
		IVector2 next;
		
		try
		{
			next = points.getFirst();
		} catch (NoSuchElementException nsee)
		{
			return pos;
		}
		
		for (IVector2 point : points)
		{
			if (GeoMath.distancePP(pos, point) < GeoMath.distancePP(pos, next))
			{
				next = point;
			}
		}
		
		return next;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
