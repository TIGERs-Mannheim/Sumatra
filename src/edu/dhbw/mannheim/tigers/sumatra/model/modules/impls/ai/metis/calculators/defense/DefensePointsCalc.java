/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EThreatKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.AEvaluationAlgorithm;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.CricitalAngleAlgorhitm;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.DirectShootEvualationAlgorithm;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.IndirectShootEvualationAlgorithm;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This is a calculator for estimate dangerous points for defender positioning.
 * It implement the system for placing defenders of team odens.
 * (See their TDP 2010 p.10 f)
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class DefensePointsCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --- technical specifications ---
	@Configurable(comment = "")
	private static float								scaleFactor					= 1000f;
	
	// --- analyzing specifications ---
	@Configurable(comment = "Quantifer for the indirectShots in DefensPoints Calculator")
	private static float								indirectShotQuantifier	= 1.0f;
	@Configurable(comment = "Quantifer for the directShots in DefensPoints Calculator")
	private static float								directShotQuantifier		= 3.0f;
	
	@Configurable(comment = "Quantifer for the indirectShots in DefensPoints Calculator")
	private static float								cricitalAngleQuantifier	= 2.0f;
	@Configurable(comment = "")
	private static int								numberThreatPoints		= 12;
	@Configurable(comment = "")
	private static float								tolerancGoal				= 50;
	@Configurable(comment = "Distance in mm to determine if its a indirect Shot. Ff ball is more than this distance away, it will classified as indirect shooter")
	private static float								indirectShotDistance		= 1000.0f;
	@Configurable(comment = "Distance in mm to determine if its a direct Shot. Ff ball is more than this distance away, it will classified as direct shooter")
	private static float								directShotDistance		= 1000.0f;
	
	@Configurable(comment = "Describes the angle witch should be considerd as critical.")
	private static float								criticalAngle				= AngleMath.PI_HALF + (AngleMath.PI_HALF / 2);
	
	@Configurable(comment = "Tolerance to set the bot a little bit in the front of the penalty area in mm")
	private static float								scalingFactorDefense		= 1;
	private final Goal								goal							= AIConfig.getGeometry().getGoalOur();
	private final List<AEvaluationAlgorithm>	evualtionAlgorithm		= new ArrayList<AEvaluationAlgorithm>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefensePointsCalc()
	{
		evualtionAlgorithm.add(new CricitalAngleAlgorhitm(
				criticalAngle));
		evualtionAlgorithm.add(new DirectShootEvualationAlgorithm(directShotDistance));
		evualtionAlgorithm.add(new IndirectShootEvualationAlgorithm(indirectShotDistance));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<DefensePoint> defPointList = new ArrayList<DefensePoint>();
		final WorldFrame wFrame = baseAiFrame.getWorldFrame();
		final Vector2 threatPoint = new Vector2(goal.getGoalCenter().x(), -(goal.getSize() / 2) - tolerancGoal);
		// prevents for go in if no foe bots exists
		if (!wFrame.foeBots.entrySet().isEmpty())
		{
			// Steps trought numberThreatPoints count points on the goal line for every foebot
			for (int i = 0; i < numberThreatPoints; i++)
			{
				threatPoint.y += getPointStepSize();
				Set<Entry<BotID, TrackedTigerBot>> foeBots = wFrame.foeBots.entrySet();
				
				for (final Entry<BotID, TrackedTigerBot> entry : foeBots)
				{
					final TrackedTigerBot bot = entry.getValue();
					
					// DefensePoints will not be calculated for bots which are inside the penalty area or behind the goal
					// line
					if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(bot.getPos())
							&& (bot.getPos().x() > AIConfig.getGeometry().getField().topLeft().x())
							&& AIConfig.getGeometry().getField()
									.isPointInShape(bot.getPos(), AIConfig.getGeometry().getBotRadius()))
					{
						DefensePoint defPoint = new DefensePoint(threatPoint, 0, bot);
						defPointList.add(defPoint);
					}
				}
			}
		}
		
		// evualate the threat value for each algorithm
		for (AEvaluationAlgorithm evaluationAlgorithm : evualtionAlgorithm)
		{
			evaluationAlgorithm.evaluateSituation(baseAiFrame, defPointList);
		}
		
		// Calculate the final threat value
		finalizePoints(defPointList);
		
		
		moveDefensePointsToOutsideOfPenaltyArea(defPointList);
		// sort the lsit after y value
		Collections.sort(defPointList, ValuePoint.Y_COMPARATOR);
		List<DefensePoint> cumulatedPoints = new ArrayList<DefensePoint>();
		for (int counter = 0; counter < defPointList.size(); counter++)
		{
			cumulatedPoints.add(getCumulatedPoint(defPointList, counter));
		}
		normalizeListDefPoints(cumulatedPoints);
		
		newTacticalField.setDefGoalPoints(cumulatedPoints);
	}
	
	
	/**
	 * Create the final value for each point.
	 * 
	 * @param defPointList
	 */
	private void finalizePoints(final List<DefensePoint> defPointList)
	{
		for (EThreatKind threat : EThreatKind.values())
		{
			List<Float> values = new ArrayList<Float>();
			// Get all values from defPoints with this threat
			for (DefensePoint defPoint : defPointList)
			{
				if (defPoint.containsThreat(threat))
				{
					values.add(defPoint.getValueOfThreat(threat));
				}
			}
			// Calculate the maximum
			float max = Float.MIN_VALUE;
			for (final float value : values)
			{
				if (value > max)
				{
					max = value;
				}
			}
			
			for (DefensePoint defPoint : defPointList)
			{
				if (defPoint.containsThreat(threat))
				{
					defPoint.addThreatKindQuantifyd(threat,
							(defPoint.getValueOfThreat(threat) / max) * getQuantifier(threat));
				}
			}
		}
		
		for (DefensePoint defPoint : defPointList)
		{
			defPoint.calcualteFinalThreat();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the StepSize to iterate over the goalline
	 */
	private float getPointStepSize()
	{
		return (AIConfig.getGeometry().getGoalOur().getSize() + (2 * tolerancGoal)) / numberThreatPoints;
	}
	
	
	/**
	 * Set all {@link DefensePoint} to the outside of the {@link PenaltyArea}
	 * 
	 * @param defPointList
	 */
	private void moveDefensePointsToOutsideOfPenaltyArea(final List<DefensePoint> defPointList)
	{
		for (DefensePoint defensePoint : defPointList)
		{
			IVector2 nearestPointOutside = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(new Vector2(defensePoint), defensePoint.getProtectAgainst().getPos());
			defensePoint.set(scaleVector(defensePoint, nearestPointOutside));
		}
	}
	
	
	/**
	 * Cumulate all threat levels in the bot radius. So one point representate a bot cover this areas.
	 * 
	 * @param sortedPoints - the sortet points after Y.
	 * @param cumulatedPointPosition - the index of the point to cumulate the arrounding
	 * @return
	 */
	private DefensePoint getCumulatedPoint(final List<DefensePoint> sortedPoints, final int cumulatedPointPosition)
	{
		final DefensePoint cumulatedPoint = new DefensePoint(sortedPoints.get(cumulatedPointPosition));
		
		final int listSize = sortedPoints.size();
		float cumulatedThreatValue = cumulatedPoint.getValue();
		
		
		// left side from point
		for (int counter = cumulatedPointPosition - 1; counter > -1; counter--)
		{
			if (GeoMath.distancePP(cumulatedPoint, sortedPoints.get(counter)) < AIConfig.getGeometry().getBotRadius())
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
			if (GeoMath.distancePP(cumulatedPoint, sortedPoints.get(counter)) < AIConfig.getGeometry().getBotRadius())
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
	 * First sort the list after the higth of the threat value and than
	 * Normalize all Values to the Maximum in the List. Choose the first Element as maximum. <br>
	 * 
	 * @param cumulatedPoints - List to Normalize
	 */
	private void normalizeListDefPoints(final List<DefensePoint> cumulatedPoints)
	{
		Collections.sort(cumulatedPoints, ValuePoint.VALUE_HIGH_COMPARATOR);
		if (cumulatedPoints.size() > 0)
		{
			final float max = cumulatedPoints.get(0).getValue();
			for (final ValuePoint valuePoint : cumulatedPoints)
			{
				valuePoint.setValue(valuePoint.getValue() / max);
			}
		}
	}
	
	
	/**
	 * Scales Vector from Point to intersection to a Vector from point to intersection +/-tolerance.
	 * 
	 * @return
	 */
	private Vector2 scaleVector(final IVector2 threatPoint, final IVector2 nearestPointOutside)
	{
		return GeoMath.stepAlongLine(nearestPointOutside, threatPoint, -scalingFactorDefense);
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// create default list
		final Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
		final List<DefensePoint> defaultList = new ArrayList<DefensePoint>();
		defaultList.add(new DefensePoint(fieldCenter.x(), fieldCenter.y()));
		newTacticalField.setDefGoalPoints(defaultList);
	}
	
	
	/**
	 * Return the quantifier for each {@link EThreatKind}
	 * 
	 * @param threat - the quantifiert search
	 * @return
	 */
	public float getQuantifier(final EThreatKind threat)
	{
		switch (threat)
		{
			case DIRECT:
				return directShotQuantifier;
			case INDIRECT:
				return indirectShotQuantifier;
			case CRITICAL_ANGLE:
				return cricitalAngleQuantifier;
			case DEFAULT:
				return 1.0f;
			default:
				return 1.0f;
		}
		
	}
}
