/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import static edu.tigers.sumatra.ai.metis.offense.OffensiveMath.getPotentialOffensiveBotMap;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.IAiInfoFromPrevFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates the {@link OffensiveStrategy} for the overall attacking plan
 */
public class OffensiveStrategyCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(OffensiveStrategyCalc.class.getName());
	
	private static final Color COLOR = new Color(30, 100, 184);
	
	
	@Configurable(defValue = "1300.0")
	private static double minMarginToOurPenAreaForIntercept = 1300;
	
	private EnumMap<EOffensiveStrategyFeature, AOffensiveStrategyFeature> features = new EnumMap<>(
			EOffensiveStrategyFeature.class);
	
	
	/**
	 * Calculates and fills the offensiveStrategy
	 */
	public OffensiveStrategyCalc()
	{
		for (EOffensiveStrategyFeature key : EOffensiveStrategyFeature.values())
		{
			try
			{
				features.put(key, (AOffensiveStrategyFeature) key.getInstanceableClass().getConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				log.error("Could not create offensive calc feature: " + key, e);
			}
		}
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IPassTarget activePassTarget = getActivePassTarget(newTacticalField.getAiInfoFromPrevFrame());
		
		if (noOffensiveBotRequired())
		{
			return;
		}
		
		features.values().forEach(f -> f.update(baseAiFrame));
		
		final OffensiveStrategy offensiveStrategy = new OffensiveStrategy();
		offensiveStrategy.setActivePassTarget(activePassTarget);
		features.values().forEach(f -> f.doCalc(newTacticalField, offensiveStrategy));
		newTacticalField.setOffensiveStrategy(offensiveStrategy);
		
		drawStrategy(offensiveStrategy, newTacticalField.getDrawableShapes());
	}
	
	
	private boolean noOffensiveBotRequired()
	{
		IBotIDMap<ITrackedBot> potentialOffensiveBots = getPotentialOffensiveBotMap(getNewTacticalField(), getAiFrame());
		return potentialOffensiveBots.isEmpty()
				|| noOffensiveGameState()
				|| getNewTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE
				|| noInterceptorRequired();
	}
	
	
	private boolean noOffensiveGameState()
	{
		return getNewTacticalField().getGameState().isKickoffOrPrepareKickoff()
				|| getNewTacticalField().getGameState().isPenaltyOrPreparePenalty()
				|| getNewTacticalField().getGameState().isBallPlacementForUs()
				|| getNewTacticalField().getGameState().isPenaltyShootout();
	}
	
	
	private boolean noInterceptorRequired()
	{
		return getAiFrame().getGamestate().isStandardSituationForThem()
				&& tooNearToOurPenAreaForIntercept();
	}
	
	
	private boolean tooNearToOurPenAreaForIntercept()
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), minMarginToOurPenAreaForIntercept);
	}
	
	
	private IPassTarget getActivePassTarget(final IAiInfoFromPrevFrame aiInfoFromPrevFrame)
	{
		List<IPassTarget> handledPassTargets = aiInfoFromPrevFrame.getActivePassTargets();
		if (handledPassTargets.size() > 1)
		{
			log.warn("Got more than one pass target from previous frame: " + handledPassTargets);
		}
		IPassTarget activePassTarget = null;
		if (!handledPassTargets.isEmpty())
		{
			activePassTarget = handledPassTargets.get(0);
		}
		return activePassTarget;
	}
	
	
	private void drawStrategy(final OffensiveStrategy offensiveStrategy, final ShapeMap shapesMap)
	{
		if (offensiveStrategy.getActivePassTarget().isPresent())
		{
			drawPassTarget(offensiveStrategy.getActivePassTarget().get(), shapesMap);
		}
		
		if (offensiveStrategy.getAttackerBot().isPresent())
		{
			ITrackedBot attacker = getWFrame().getBot(offensiveStrategy.getAttackerBot().get());
			shapesMap.get(EAiShapesLayer.OFFENSIVE_STRATEGY_DEBUG).add(new DrawableAnnotation(attacker.getPos(),
					offensiveStrategy.isAttackerIsAllowedToKick() ? "Ready" : "Waiting", COLOR)
							.withCenterHorizontally(true)
							.withOffset(Vector2.fromY(170)));
		}
		
		for (Map.Entry<BotID, EOffensiveStrategy> entry : offensiveStrategy.getCurrentOffensivePlayConfiguration()
				.entrySet())
		{
			ITrackedBot tBot = getWFrame().getBot(entry.getKey());
			shapesMap.get(EAiShapesLayer.OFFENSIVE_STRATEGY_DEBUG)
					.add(new DrawableAnnotation(tBot.getPos(), entry.getValue().name(), COLOR)
							.withCenterHorizontally(true)
							.withOffset(Vector2f.fromY(-170)));
		}
	}
	
	
	private void drawPassTarget(final IPassTarget passTarget, final ShapeMap shapesMap)
	{
		shapesMap.get(EAiShapesLayer.OFFENSIVE_STRATEGY).add(new DrawableCircle(
				Circle.createCircle(passTarget.getKickerPos(), 100), COLOR)
						.withFill(true));
		
		shapesMap.get(EAiShapesLayer.OFFENSIVE_STRATEGY)
				.add(new DrawableLine(Line.fromPoints(getBall().getPos(), passTarget.getKickerPos()), COLOR));
		
		shapesMap.get(EAiShapesLayer.OFFENSIVE_STRATEGY_DEBUG).add(new DrawableAnnotation(passTarget.getKickerPos(),
				String.format("age: %.2f\nreached in: %.2f",
						passTarget.getAge(getWFrame().getTimestamp()),
						passTarget.getTimeUntilReachedInS(getWFrame().getTimestamp())),
				COLOR)
						.withCenterHorizontally(true)
						.withOffset(Vector2.fromY(210)));
	}
}
