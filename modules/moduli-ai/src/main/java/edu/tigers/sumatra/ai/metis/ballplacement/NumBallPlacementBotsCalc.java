/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballplacement;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;


public class NumBallPlacementBotsCalc extends ACalculator
{
	@Configurable(defValue = "true")
	private static boolean useSecondBallPlacer = true;

	@Configurable(comment = "max dist where ball can be pushed. if dist > this, then shoot", defValue = "3000.0")
	private static double pushBallVsPassDistance = 3000;

	private final Hysteresis insidePushRadiusHysteresis = new Hysteresis(
			pushBallVsPassDistance - 500,
			pushBallVsPassDistance + 500);

	@Getter
	private int numBallPlacementBots;


	@Override
	protected void doCalc()
	{
		if (useSecondBallPlacer && !isBallInsidePushRadius())
		{
			numBallPlacementBots = 2;
		} else
		{
			numBallPlacementBots = 1;
		}
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isBallPlacementForUs();
	}


	@Override
	protected void reset()
	{
		numBallPlacementBots = 0;
		insidePushRadiusHysteresis.setUpper(false);
	}


	private boolean isBallInsidePushRadius()
	{
		IVector2 placementPos = getAiFrame().getGameState().getBallPlacementPositionForUs();
		if (placementPos != null)
		{
			insidePushRadiusHysteresis.update(placementPos.distanceTo(getBall().getPos()));
			return insidePushRadiusHysteresis.isLower();
		}
		return true;
	}
}
