/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.EnhancedFieldAnalyser;


/**
 * Calculates ideal (offensive) positions for the SupportRole.
 * 
 * @author JulianT SimonS
 *         TODO
 *         umbenennen
 *         Kreis um Ball
 *         Werte des Fieldanalysers verweden
 *         �berpr�fen von Zeit und visbilkity �berdenken
 */
/**
 * TODO Simon, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Simon
 */
public class SupportPositionSimonCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private int rayCount = 40;
	
	private float						ballCircleRadius	= 1000;
	private float						pointCircleRadius	= 500;								// cirle
	// without
	// enemy
	
	private float						minAngle				= 10;
	private float						maxAngle				= 80;
	private float						bestAngle			= 50;
	
	
	private HashMap<BotID, Long>	lastBotUpdateTime	= new HashMap<BotID, Long>();
	
	
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
		
		LinkedList<IVector2> intersections = getPositions(baseAiFrame.getPrevFrame(), baseAiFrame);
		
		// System.err.println("intersections: " + intersections.size());
		
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			IVector2 botPos = bot.getPos();
			
			LinkedList<ValuePoint> sortedValueIntersetction = sortIntersectionPoints(baseAiFrame, botPos, intersections);
			
			positions.put(bot.getId(), getBestIntersectionPoints(baseAiFrame, bot.getId(), sortedValueIntersetction));
			
			targets.put(bot.getId(),
					baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShotTargetBots().get(bot.getId()));
		}
		
		newTacticalField.setSupportPositions(positions);
		newTacticalField.setSupportTargets(targets);
		
		newTacticalField.setSupportIntersections(intersections);
		
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setSupportPositions(baseAiFrame.getPrevFrame().getTacticalField().getSupportPositions());
		newTacticalField.setSupportTargets(baseAiFrame.getPrevFrame().getTacticalField().getSupportTargets());
		newTacticalField.setSupportIntersections(baseAiFrame.getPrevFrame().getTacticalField().getSupportIntersections());
	}
	
	
	/**
	 * Computes a list of points that are visible from ball and opponent goal.
	 * 
	 * @param preAiFrame
	 * @param baseAiFrame
	 * @return A bunch of good offensive positions for the SupportRole
	 */
	private LinkedList<IVector2> getPositions(final AIInfoFrame preAiFrame, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		// LinkedList<Line> linesBallGoalline = new LinkedList<Line>();
		// LinkedList<Line> lineGoalMiddleline = new LinkedList<Line>();
		
		LinkedList<IVector2> intersections = new LinkedList<IVector2>();
		
		IVector2 bestGoalPos = baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShootTarget();
		
		IVector2 ballPos = wFrame.getBall().getPos();
		// IVector2 theirGoalRight = AIConfig.getGeometry().getGoalTheir().getGoalPostRight();
		IVector2 theirGoalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		// float middlelineStepSize = AIConfig.getGeometry().getFieldWidth() / rayCount; // Abstand der Strahlenden //
		// zueinander
		// float goalStepSize = AIConfig.getGeometry().getGoalSize() / rayCount;
		// float rightFieldLineY = AIConfig.getGeometry().getFieldWidth() * -0.5f; // Rechteste y-Koordinate des
		// Spielfeldes
		
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		
		float lineLength = fieldWidth;
		
		LinkedList<Line> linesBall = new LinkedList<Line>();
		LinkedList<Line> linesGoal = new LinkedList<Line>();
		
		for (int i = 0; i < 360; i += 10)
		{
			float angle = AngleMath.deg2rad(i);
			
			float endX = (float) (ballPos.x() + (lineLength * Math.sin(angle)));
			float endY = (float) (ballPos.y() + (lineLength * Math.cos(angle)));
			Vector2 point = new Vector2(endX, endY);
			
			if (checkVisibilityIgnoreTigers(baseAiFrame, ballPos, point))
			{
				linesBall.add(Line.newLine(ballPos, new Vector2(endX, endY)));
			}
		}
		
		
		for (int i = 180; i < 360; i += 10)
		{
			float angle = AngleMath.deg2rad(i);
			
			float endX = (float) (theirGoalCenter.x() + (lineLength * Math.sin(angle)));
			float endY = (float) (theirGoalCenter.y() + (lineLength * Math.cos(angle)));
			
			Vector2 point = new Vector2(endX, endY);
			
			linesGoal.add(Line.newLine(bestGoalPos, point));
			
		}
		// // Ununterbrochene Strahlen bestimmen
		// for (int i = 0; i < rayCount; i++)
		// {
		// IVector2 middlelinePos = new Vector2(0, rightFieldLineY + (i * middlelineStepSize));
		// IVector2 goalFieldlinePos = new Vector2(theirGoalRight.x(), rightFieldLineY + (i * middlelineStepSize));
		//
		// if ((!(ballPos.equals(middlelinePos))) && checkVisibilityIgnoreTigers(wFrame, ballPos, middlelinePos))
		// {
		// linesBallGoalline.add(Line.newLine(ballPos, goalFieldlinePos));
		// }
		//
		// middlelinePos = new Vector2(0, rightFieldLineY + (i * middlelineStepSize));
		// IVector2 goallinePos = new Vector2(theirGoalRight.x(), theirGoalRight.y() + (i * goalStepSize));
		//
		// if ((!(middlelinePos.equals(goallinePos))) && checkVisibilityIgnoreTigers(wFrame, goallinePos, middlelinePos))
		// {
		// lineGoalMiddleline.add(Line.newLine(goallinePos, middlelinePos));
		// }
		//
		// }
		
		Circle ballCircle = new Circle(ballPos, ballCircleRadius);
		PenaltyArea theirPenalty = AIConfig.getGeometry().getPenaltyAreaTheir();
		
		Rectangle field = AIConfig.getGeometry().getField();
		
		Rectangle ourField = new Rectangle(new Vector2(0, ((-0.5) * fieldWidth)), new Vector2(((-0.5) * fieldLength),
				((0.5) * fieldWidth)));
		
		
		boolean foebotInPointCirce = false;
		
		for (Line ballRay : linesBall)
		{
			for (Line goalRay : linesGoal)
			{
				try
				{
					IVector2 intersectionPoint = GeoMath.intersectionPointOnLine(ballRay, goalRay);
					Circle pointCircle = new Circle(intersectionPoint, pointCircleRadius);
					
					if (ourField.isPointInShape(intersectionPoint))
					{
						// System.out.println("ourField.isPointInShape: " + intersectionPoint);
						continue;
					}
					
					
					if (!field.isPointInShape(intersectionPoint))
					{
						// System.out.println("!field.isPointInShape: " + intersectionPoint);
						continue;
					}
					
					if (ballCircle.isPointInShape(intersectionPoint))
					{
						// System.out.println("ballCircle.isPointInShape: " + intersectionPoint);
						continue;
					}
					
					if (theirPenalty.isPointInShape(intersectionPoint))
					{
						// System.out.println("theirPenalty.isPointInShape: " + intersectionPoint);
						continue;
					}
					
					if (!checkVisibilityIgnoreFoeKeeper(baseAiFrame, intersectionPoint, bestGoalPos))
					{
						// System.out.println("theirPenalty.isPointInShape: " + intersectionPoint);
						continue;
					}
					
					
					foebotInPointCirce = false;
					for (TrackedTigerBot bot : wFrame.getFoeBots().values())
					{
						if (pointCircle.isPointInShape(bot.getPos()))
						{
							// System.out.println("Foebot in pointCircle: " + intersectionPoint);
							foebotInPointCirce = true;
						}
						
						if (foebotInPointCirce)
						{
							break;
						}
					}
					
					if (foebotInPointCirce)
					{
						continue;
					}
					
					intersections.add(intersectionPoint);
					// preAiFrame.addDebugShape(new DrawablePoint(intersectionPoint, Color.yellow));
					
				} catch (MathException me)
				{
				}
			}
		}
		
		return intersections;
	}
	
	
	/**
	 * Get best Intersection
	 * 
	 * @param baseAiFrame
	 * @param botID
	 * @param valuePoints
	 * @return A list of sorted ValuePoints
	 */
	private IVector2 getBestIntersectionPoints(final BaseAiFrame baseAiFrame, final BotID botID,
			final LinkedList<ValuePoint> valuePoints)
	{
		// TODO ball direction auf supportBot abfangen
		
		// IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		// IVector2 goalPos = baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShootTarget();
		
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		IVector2 botPos = wFrame.tigerBotsVisible.get(botID).getPos();
		
		ValuePoint bestPos;
		
		if (valuePoints.isEmpty())
		{
			bestPos = new ValuePoint(0, 0, 0);
		} else
		{
			bestPos = valuePoints.getFirst();
		}
		
		
		long newTime = System.currentTimeMillis();
		
		
		for (ValuePoint point : valuePoints)
		{
			
			if (GeoMath.distancePP(botPos, point) < GeoMath.distancePP(botPos, bestPos))
			{
				bestPos = point;
				lastBotUpdateTime.put(botID, newTime);
			}
			
		}
		
		valuePoints.remove(bestPos);
		return bestPos;
	}
	
	
	/**
	 * Determin the value of an Intersetcion and sorted the list
	 * 
	 * @param baseAiFrame
	 * @param botPos
	 * @param points
	 * @return A list of sorted ValuePoints
	 */
	private LinkedList<ValuePoint> sortIntersectionPoints(final BaseAiFrame baseAiFrame, final IVector2 botPos,
			final LinkedList<IVector2> points)
	{
		LinkedList<ValuePoint> sortedIntersectionPoints = new LinkedList<ValuePoint>();
		IVector2 goalPos = baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShootTarget();
		
		// 1 = good -- 0 = bad
		
		for (IVector2 point : points)
		{
			
			float value = 0;
			
			value += determinAngleValue(baseAiFrame, point);
			value += map(AiMath.getScoreForStraightShot(baseAiFrame.getWorldFrame(), point, goalPos), 0, 1, 1, 0);
			// value += AiMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), point);
			value += getFieldValue(baseAiFrame, point);
			
			sortedIntersectionPoints.add(new ValuePoint(point, value));
		}
		
		Collections.sort(sortedIntersectionPoints, ValuePoint.VALUE_HIGH_COMPARATOR);
		
		return sortedIntersectionPoints;
	}
	
	
	private float getFieldValue(final BaseAiFrame baseAiFrame, final IVector2 point)
	{
		float recValue = 0;
		float foeBotRecValue = 0;
		
		EnhancedFieldAnalyser fieldAnalyser = baseAiFrame.getPrevFrame().getTacticalField().getEnhancedFieldAnalyser();
		if (fieldAnalyser == null)
		{
			return 0;
		}
		List<AIRectangle> rectangles = fieldAnalyser.getAnalysingRectangleVector().getRectangles();
		
		final float FACTOR = fieldAnalyser.getTotalMaximum();
		
		for (AIRectangle rect : rectangles)
		{
			if (rect.isPointInShape(point))
			{
				recValue = rect.getValue();
				break;
			}
			
		}
		
		float corRecValue = recValue / FACTOR;
		
		// rectangle near foevot
		if (corRecValue < 0)
		{
			foeBotRecValue = -corRecValue;
		}
		
		if (foeBotRecValue > 1)
		{
			foeBotRecValue = 1;
		}
		
		
		return foeBotRecValue;
		
		
	}
	
	
	/**
	 * Determin the value of the angle between point-ball and point-goal
	 * good 1
	 * badd 0
	 * 
	 * @param baseAiFrame
	 * @param point
	 * @return value
	 */
	private float determinAngleValue(final BaseAiFrame baseAiFrame, final IVector2 point)
	{
		
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getVel();
		IVector2 goalPos = baseAiFrame.getPrevFrame().getTacticalField().getBestDirectShootTarget();
		
		IVector2 pointBallVec = ballPos.subtractNew(point);
		IVector2 pointgoalVec = goalPos.subtractNew(point);
		
		float angle = AngleMath.rad2deg(GeoMath.angleBetweenVectorAndVector(pointBallVec, pointgoalVec));
		float value = 0;
		
		if ((angle < minAngle) || (angle > maxAngle))
		{
			value = 0;
		} else
		{
			if (angle < bestAngle)
			{
				value = map(angle, minAngle, bestAngle, 0, 1);
			} else
			{
				value = map(angle, bestAngle, maxAngle, 1, 0);
			}
		}
		
		return value;
		
	}
	
	
	private boolean checkVisibilityIgnoreTigers(final BaseAiFrame baseFrame, final IVector2 start, final IVector2 end)
	{
		float ballRadiusTrashhold = AIConfig.getGeometry().getBallRadius() * 4.0f;
		
		// Eigene Bots ignorieren
		LinkedList<BotID> ignoreBots = new LinkedList<BotID>();
		for (TrackedTigerBot bot : baseFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			ignoreBots.add(bot.getId());
		}
		
		return GeoMath.p2pVisibility(baseFrame.getWorldFrame(), start, end, ballRadiusTrashhold, ignoreBots);
	}
	
	
	private boolean checkVisibilityIgnoreFoeKeeper(final BaseAiFrame baseFrame, final IVector2 start, final IVector2 end)
	{
		float ballRadiusTrashhold = AIConfig.getGeometry().getBallRadius() * 4.0f;
		WorldFrame wFrame = baseFrame.getWorldFrame();
		
		LinkedList<BotID> ignoreBots = new LinkedList<BotID>();
		
		ignoreBots.add(baseFrame.getKeeperFoeId());
		
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			ignoreBots.add(bot.getId());
		}
		
		return GeoMath.p2pVisibility(wFrame, start, end, ballRadiusTrashhold, ignoreBots);
	}
	
	
	private float map(final float x, final float in_min, final float in_max, final float out_min, final float out_max)
	{
		return (((x - in_min) * (out_max - out_min)) / (in_max - in_min)) + out_min;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
