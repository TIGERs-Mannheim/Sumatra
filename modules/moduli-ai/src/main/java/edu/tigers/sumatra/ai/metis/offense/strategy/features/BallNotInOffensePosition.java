/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * MarkG
 */
public class BallNotInOffensePosition extends AOffensiveStrategyFeature
{
	/**
	 * Default
	 */
	public BallNotInOffensePosition()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		if (Geometry.getPenaltyAreaOur().isPointInShape(ballPos, Geometry.getBotRadius() + Geometry.getBallRadius())
				|| !Geometry.getField().isPointInShape(ballPos, 1.5 * Geometry.getBallRadius()))
		{
			strategy.setMaxNumberOfBots(0);
			strategy.setMinNumberOfBots(0);
			strategy.getDesiredBots().clear();
		}
	}
}
