/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EShootKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * This is a calculator for estimate dangerous points for defender positioning.
 * It implement the system for placing defenders of team odens.
 * (See their TDP 2010 p.10 f)
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class DefensePointsCalculator extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float									botRadius					= AIConfig.getGeometry().getBotRadius();
	private final Goal									goal							= AIConfig.getGeometry().getGoalOur();
	
	// --- technical specifications ---
	private static final float							SCALE_FACTOR				= 1000f;
	
	// --- analyzing specifications ---
	private final int										numberThreatPoints		= AIConfig.getMetisCalculators()
																										.getNumberOfThreatPoints();
	private final float									tolerancGoal				= AIConfig.getMetisCalculators()
																										.getToleranceGoal();
	
	/** the length between two points */
	private final float									pointStepSize				= (goal.getSize() + (2 * tolerancGoal))
																										/ numberThreatPoints;
	
	private float											directShotQuantifier		= AIConfig.getMetisCalculators()
																										.getDirectShootQuantifier();
	private float											indirectShotQuantifier	= AIConfig.getMetisCalculators()
																										.getIndirectShootQuantifier();
	private float											ballQuantifier				= AIConfig.getMetisCalculators()
																										.getBallQuantifier();
	
	private float											indirectShotDistance		= AIConfig.getMetisCalculators()
																										.getIndirectShootDistance();
	
	private TrackedBall									ball							= null;
	
	private float											tolerance					= AIConfig.getMetisCalculators()
																										.getScalingFactorDefense();
	
	private final Map<TrackedBot, List<Vector2>>	dangerousPointsIndirect	= new HashMap<TrackedBot, List<Vector2>>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefensePointsCalculator()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		directShotQuantifier = AIConfig.getMetisCalculators().getDirectShootQuantifier();
		indirectShotQuantifier = AIConfig.getMetisCalculators().getIndirectShootQuantifier();
		indirectShotDistance = AIConfig.getMetisCalculators().getIndirectShootDistance();
		ballQuantifier = AIConfig.getMetisCalculators().getBallQuantifier();
		
		final WorldFrame worldFrame = curFrame.worldFrame;
		dangerousPointsIndirect.clear();
		ball = worldFrame.ball;
		
		// this is necessary for testing play with fewer then 5 bots or mixedTeamChalange
		// IVector2 keeperPosition = new Vector2(keeper.pos);
		final Vector2 threatPoint = new Vector2(goal.getGoalCenter().x(), -(goal.getSize() / 2) - tolerancGoal);
		if (!worldFrame.foeBots.entrySet().isEmpty())
		{
			for (int i = 0; i < numberThreatPoints; i++)
			{
				threatPoint.y += pointStepSize;
				// curFrame.addDebugShape(new DrawablePoint(threatPoint, Color.BLACK));
				Set<Entry<BotID, TrackedBot>> foeBots = worldFrame.foeBots.entrySet();
				
				// Abfangen wenn kein Gegner auf dem Feld ist
				// shot
				for (final Entry<BotID, TrackedBot> entry : foeBots)
				{
					final TrackedBot bot = entry.getValue();
					
					// DefensePoints will not be calculated for bots which are inside the penalty area
					if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(bot.getPos())
							&& (bot.getPos().x() > AIConfig.getGeometry().getField().topLeft().x()))
					{
						List<Vector2> list = dangerousPointsIndirect.get(bot);
						if (list == null)
						{
							list = new ArrayList<Vector2>();
						}
						list.add(new Vector2(threatPoint));
						dangerousPointsIndirect.put(bot, list);
					}
				}
			}
			
			
			curFrame.tacticalInfo.setDefGoalPoints(calculatThreatValueOfDangerousPoints(curFrame, preFrame));
			
		}
		
		// Fallback l�sung
		// Liste mit pseudoDefPoints f�llen
		if (dangerousPointsIndirect.isEmpty())
		{
			// Fallback l�sung falls keine Punkt berechnet werden, trotz das gegner da sind
			List<DefensePoint> pseudoPoints = new ArrayList<DefensePoint>();
			// pseudoPoints.add(new DefensePoint(-2100, 0));
			// pseudoPoints.add(new DefensePoint(-2200, 400));
			// pseudoPoints.add(new DefensePoint(-2200, -400));
			curFrame.tacticalInfo.setDefGoalPoints(pseudoPoints);
			
		}
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This functions sorts the estimated dangerous points and calculat the threatvalue returns them in a
	 * descending order starting with the most dangerous point.
	 * 
	 * @return List of sorted dangerous points
	 */
	private List<DefensePoint> calculatThreatValueOfDangerousPoints(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final List<DefensePoint> sortedPoints = new ArrayList<DefensePoint>();
		
		// adds ball to the list
		final Vector2 threatPoint = new Vector2(goal.getGoalCenter().x(), -goal.getSize() / 2);
		for (int i = 0; i < numberThreatPoints; i++)
		{
			if ((ball.getPos().x() > -(AIConfig.getGeometry().getFieldLength() / 2))
					&& !AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPos()))
			{
				threatPoint.y += pointStepSize;
				// Verhinder das Punkt berechnet werden wenn der Gegner hinter unserm Tor im aus steht
				IVector2 nearestPoint = AIConfig.getGeometry().getPenaltyAreaOur()
						.nearestPointOutside(new Vector2(threatPoint), ball.getPos());
				
				nearestPoint = scaleVector(threatPoint, nearestPoint);
				
				DefensePoint defPoint = new DefensePoint(nearestPoint, 0, null);
				
				if (!isPointDirectProtected(threatPoint, ball.getPos(), curFrame, preFrame))
				{
					defPoint = calculateThreatValue(threatPoint, ball.getPos(), defPoint);
				} else
				{
					defPoint = calculateShootKind(threatPoint, ball.getPos(), defPoint);
				}
				sortedPoints.add(defPoint);
			}
		}
		
		
		for (final Entry<TrackedBot, List<Vector2>> entry : dangerousPointsIndirect.entrySet())
		{
			float threatValue = 0;
			final List<Vector2> list = entry.getValue();
			final TrackedBot passingBot = entry.getKey();
			
			for (final Vector2 goalPoint : list)
			{
				// Verhinder das Punkt berechnet werden wenn der Gegner hinter unserm Tor im aus steht
				IVector2 nearestPoint = AIConfig.getGeometry().getPenaltyAreaOur()
						.nearestPointOutside(new Vector2(goalPoint), passingBot.getPos());
				
				nearestPoint = scaleVector(goalPoint, nearestPoint);
				
				DefensePoint defPoint = new DefensePoint(nearestPoint, threatValue, passingBot);
				if (!isPointDirectProtected(goalPoint, passingBot.getPos(), curFrame, preFrame))
				{
					defPoint = calculateThreatValue(goalPoint, passingBot.getPos(), defPoint);
				} else
				{
					defPoint = calculateShootKind(goalPoint, passingBot.getPos(), defPoint);
				}
				sortedPoints.add(defPoint);
			}
		}
		Collections.sort(sortedPoints, ValuePoint.YCOMPARATOR);
		
		
		// Cummalted all points in the radius of the bot
		List<DefensePoint> cumulatedPoints = new ArrayList<DefensePoint>();
		
		for (int counter = 0; counter < sortedPoints.size(); counter++)
		{
			cumulatedPoints.add(getCumulatedPoint(sortedPoints, counter));
		}
		// sort the list after the Threatvalue
		Collections.sort(cumulatedPoints, ValuePoint.VALUEHIGHCOMPARATOR);
		// normalize list
		cumulatedPoints = normalizeList(cumulatedPoints);
		
		
		return cumulatedPoints;
	}
	
	
	private DefensePoint getCumulatedPoint(List<DefensePoint> sortedPoints, int cumulatedPointPosition)
	{
		final DefensePoint cumulatedPoint = new DefensePoint(sortedPoints.get(cumulatedPointPosition));
		
		final int listSize = sortedPoints.size();
		float cumulatedThreatValue = cumulatedPoint.getValue();
		
		
		// left side from point
		for (int counter = cumulatedPointPosition - 1; counter > -1; counter--)
		{
			if (GeoMath.distancePP(cumulatedPoint, sortedPoints.get(counter)) < botRadius)
			{
				cumulatedThreatValue += sortedPoints.get(counter).getValue();
			} else
			{
				break;
			}
		}
		
		// right side from point
		for (int counter = cumulatedPointPosition + 1; counter < listSize; counter++)
		{
			if (GeoMath.distancePP(cumulatedPoint, sortedPoints.get(counter)) < botRadius)
			{
				cumulatedThreatValue += sortedPoints.get(counter).getValue();
			} else
			{
				break;
			}
		}
		
		cumulatedPoint.setValue(cumulatedThreatValue);
		
		
		return cumulatedPoint;
	}
	
	
	/**
	 * Normalize all Values to the Maximum in the List. Choose the first Element as maximum. <br>
	 * <strong>Note:</strong> Only use for sorted Lists
	 * 
	 * @param cumulatedPoints - List to Normalize
	 * @return Normalized List
	 */
	private List<DefensePoint> normalizeList(List<DefensePoint> cumulatedPoints)
	{
		if (cumulatedPoints.size() > 0)
		{
			final float max = cumulatedPoints.get(0).getValue();
			for (final ValuePoint valuePoint : cumulatedPoints)
			{
				valuePoint.setValue(valuePoint.getValue() / max);
			}
		}
		return cumulatedPoints;
	}
	
	
	/**
	 * 
	 * Calculates the threat value for a goal point which can be attacked. If the distance is higher than a specified
	 * value, indirectShotQuantifier while be used.
	 * 
	 * @param goalPoint
	 * @param passingBot
	 * @param defPoint
	 * @return threat value
	 */
	private DefensePoint calculateThreatValue(IVector2 goalPoint, IVector2 passingBot, DefensePoint defPoint)
	{
		final float passerGoalDistance = GeoMath.distancePP(passingBot, goalPoint);
		final float ballPassingBotDistance = GeoMath.distancePP(passingBot, ball.getPos());
		float threatValue = 0;
		if ((ballPassingBotDistance > (indirectShotDistance)) && (passingBot.x() < 100))
		{
			threatValue = ((indirectShotQuantifier * 1) / (passerGoalDistance + ballPassingBotDistance)) * SCALE_FACTOR;
			defPoint.setKindOfshoot(EShootKind.INDIRECT);
			defPoint.setValue(threatValue);
			return defPoint;
		} else if (ballPassingBotDistance == 0)
		{
			threatValue = ((ballQuantifier * 1) / (passerGoalDistance + ballPassingBotDistance)) * SCALE_FACTOR;
			defPoint.setKindOfshoot(EShootKind.BALL);
			defPoint.setValue(threatValue);
			return defPoint;
		}
		threatValue = ((directShotQuantifier * 1) / (passerGoalDistance + ballPassingBotDistance)) * SCALE_FACTOR;
		defPoint.setKindOfshoot(EShootKind.DIRECT);
		defPoint.setValue(threatValue);
		return defPoint;
		
		
	}
	
	
	/**
	 * 
	 * Calculates the shootKind
	 * 
	 * @param goalPoint
	 * @param passingBot
	 * @param defPoint
	 * @return shootKind
	 */
	private DefensePoint calculateShootKind(IVector2 goalPoint, IVector2 passingBot, DefensePoint defPoint)
	{
		final float ballPassingBotDistance = GeoMath.distancePP(passingBot, ball.getPos());
		if (ballPassingBotDistance > (indirectShotDistance))
		{
			defPoint.setKindOfshoot(EShootKind.INDIRECT);
			return defPoint;
		} else if (ballPassingBotDistance == 0)
		{
			defPoint.setKindOfshoot(EShootKind.BALL);
		} else
		{
			defPoint.setKindOfshoot(EShootKind.DIRECT);
		}
		return defPoint;
	}
	
	
	/**
	 * 
	 * This function estimates if a goal point is directly protected by some other role.
	 * 
	 * @param goalPoint which should be proofed
	 * @return true when goalPoint is protected by keeper
	 */
	private boolean isPointDirectProtected(IVector2 goalPoint, IVector2 foeBotPosition, AIInfoFrame curFrame,
			AIInfoFrame prevousAiInfoFrame)
	{
		final IBotIDMap<TrackedTigerBot> ourBots = curFrame.worldFrame.tigerBotsAvailable;
		final BotIDMapConst<ARole> roles = prevousAiInfoFrame.getAssigendRoles();
		
		for (final Entry<BotID, TrackedTigerBot> idMapRole : ourBots)
		{
			// Pr�ft jeah nach der Bot Seite ob der Bot sich in Verteidigungspostions befindet
			// - Vor dem Roboter
			// - Rechts oder Links richtung tor (Abh�ngig von der Y-Position des gegnerischen RoboteR)
			// - Abstand zur Schusslinie
			ARole role = null;
			if (roles.containsKey(idMapRole.getKey()))
			{
				role = roles.get(idMapRole.getKey());
				// Add role keeper in if clause to ignore keeper by the defenspoint calculation
				if ((role.getType() != ERole.DEFENDER_KNDWDP) && (role.getType() != ERole.KEEPER_SOLO)
						&& (role.getType() != ERole.KEEPER_SOLO_V2))
				{
					final float newDist = GeoMath.distancePL(ourBots.get(idMapRole.getKey()).getPos(), goalPoint,
							foeBotPosition);
					// maybe ander vergleichs distanz
					if ((newDist < (2 * botRadius)) && (ourBots.get(idMapRole.getKey()).getPos().x() < foeBotPosition.x()))
					{
						if (foeBotPosition.y() <= 0)
						{
							if ((foeBotPosition.y() < ourBots.get(idMapRole.getKey()).getPos().y()))
							{
								return true;
							}
						} else
						{
							if (foeBotPosition.y() > ourBots.get(idMapRole.getKey()).getPos().y())
							{
								return true;
							}
						}
					}
				}
			} else
			{
				final float newDist = GeoMath.distancePL(ourBots.get(idMapRole.getKey()).getPos(), goalPoint,
						foeBotPosition);
				if ((newDist < (2 * botRadius)) && (ourBots.get(idMapRole.getKey()).getPos().x() < foeBotPosition.x()))
				{
					if (foeBotPosition.y() <= 0)
					{
						if ((foeBotPosition.y() < ourBots.get(idMapRole.getKey()).getPos().y()))
						{
							return true;
						}
					} else
					{
						if (foeBotPosition.y() > ourBots.get(idMapRole.getKey()).getPos().y())
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * Scales Vector from Point to intersection to a Vector from point to intersection + +tolerance.
	 * @return
	 */
	private Vector2 scaleVector(IVector2 threatPoint, IVector2 nearestPointOutside)
	{
		return GeoMath.stepAlongLine(nearestPointOutside, threatPoint, -tolerance);
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// create default list
		final Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
		final List<DefensePoint> defaultList = new ArrayList<DefensePoint>();
		defaultList.add(new DefensePoint(fieldCenter.x(), fieldCenter.y()));
		curFrame.tacticalInfo.setDefGoalPoints(defaultList);
	}
	
	
}
