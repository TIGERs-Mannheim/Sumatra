/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EBallResponsibility;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates OffenseStrategy for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStrategyCalc extends ACalculator
{
	protected static final Logger log = Logger
			.getLogger(OffensiveStrategyCalc.class.getName());
	
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
				log.error("Could not create offensive calc features", e);
			}
		}
	}
	
	
	private boolean isNoOffensiveGameState(ITacticalField newTacticalField)
	{
		return newTacticalField.getGameState().isKickoffOrPrepareKickoff()
				|| newTacticalField.getGameState().isPenaltyOrPreparePenalty()
				|| newTacticalField.getGameState().isBallPlacementForUs()
				|| newTacticalField.getGameState().isPenaltyShootout();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		OffensiveStrategy offensiveStrategy = new OffensiveStrategy();
		IBotIDMap<ITrackedBot> potentialOffensiveBots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField,
				baseAiFrame);
		boolean validPotentialBots = potentialOffensiveBots == null || potentialOffensiveBots.isEmpty();
		if (validPotentialBots
				|| isNoOffensiveGameState(newTacticalField)
				|| (newTacticalField.getBallResponsibility() == EBallResponsibility.DEFENSE
						&& baseAiFrame.getPrevFrame().getAICom().getPassTarget() == null))
		{
			offensiveStrategy.setMaxNumberOfBots(0);
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.getDesiredBots().clear();
			newTacticalField.setOffensiveStrategy(offensiveStrategy);
			return;
		}
		TemporaryOffensiveInformation tempInfo = new TemporaryOffensiveInformation();
		for (EOffensiveStrategyFeature key : EOffensiveStrategyFeature.values())
		{
			features.get(key).initFeature();
			features.get(key).doCalc(newTacticalField, baseAiFrame, tempInfo, offensiveStrategy);
		}
		
		// store new offensive Strategy in tactical field
		newTacticalField.setOffensiveStrategy(offensiveStrategy);
	}
}
