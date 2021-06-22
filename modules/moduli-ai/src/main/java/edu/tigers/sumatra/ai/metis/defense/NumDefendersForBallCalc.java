/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;


/**
 * Calculate the desired number of defenders for the ball threat.
 */
public class NumDefendersForBallCalc extends ACalculator
{
	@Configurable(comment = "Lower x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "-250")
	private static double minXValueForBallLower = -250;
	@Configurable(comment = "Upper x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "250")
	private static double minXValueForBallUpper = 250;
	@Configurable(comment = "Lookahead for ball position", defValue = "0.3")
	private static double ballLookahead = 0.3;

	@Configurable(comment = "Boundary for reducing to one crucial defenders", defValue = "1.2")
	private static double angleThresholdOneCrucialDefenderLower = 1.2;
	@Configurable(comment = "Boundary for reducing to one crucial defenders", defValue = "1.35")
	private static double angleThresholdOneCrucialDefenderUpper = 1.35;
	@Configurable(comment = "Boundary for reducing to zero crucial defenders", defValue = "1.45")
	private static double angleThresholdZeroCrucialDefenderLower = 1.45;
	@Configurable(comment = "Boundary for reducing to zero crucial defenders", defValue = "1.5")
	private static double angleThresholdZeroCrucialDefenderUpper = 1.5;

	@Configurable(comment = "Degrade one crucial defender to a standard defender during indirect for them", defValue = "true")
	private static boolean onlyOneCrucialDuringIndirect = true;

	@Configurable(comment = "Max number of defenders for covering the ball", defValue = "2")
	private static int maxDefendersForBallThreat = 2;


	private final Hysteresis angleOneCrucialDefenderHysteresis = new Hysteresis(
			angleThresholdOneCrucialDefenderLower,
			angleThresholdOneCrucialDefenderUpper);

	private final Hysteresis angleZeroCrucialDefenderHysteresis = new Hysteresis(
			angleThresholdZeroCrucialDefenderLower,
			angleThresholdZeroCrucialDefenderUpper);

	private final Hysteresis ballXValueHysteresis = new Hysteresis(
			minXValueForBallLower,
			minXValueForBallUpper);


	/**
	 * The number of defenders that should be assigned to the ball.
	 * This number may be higher than the total number of defenders.
	 * It is an input for determining the total number of defenders.
	 */
	@Getter
	private int numDefenderForBall = 0;


	@Override
	public void doCalc()
	{
		numDefenderForBall = numDefendersBasedOnBallPos();
	}


	private int numDefendersBasedOnBallPos()
	{
		final IVector2 ballPos = getBall().getTrajectory().getPosByTime(0.1).getXYVector();
		int numBotsForBall = maxDefendersForBallThreat;

		if (!isBallInOurHalf())
		{
			numBotsForBall = Math.min(1, numBotsForBall);
		}

		angleOneCrucialDefenderHysteresis.setLowerThreshold(angleThresholdOneCrucialDefenderLower);
		angleOneCrucialDefenderHysteresis.setUpperThreshold(angleThresholdOneCrucialDefenderUpper);
		angleOneCrucialDefenderHysteresis
				.update(Math.abs(ballPos.subtractNew(Geometry.getGoalOur().bisection(ballPos)).getAngle()));

		angleZeroCrucialDefenderHysteresis.setLowerThreshold(angleThresholdZeroCrucialDefenderLower);
		angleZeroCrucialDefenderHysteresis.setUpperThreshold(angleThresholdZeroCrucialDefenderUpper);
		angleZeroCrucialDefenderHysteresis
				.update(Math.abs(ballPos.subtractNew(Geometry.getGoalOur().bisection(ballPos)).getAngle()));

		if (angleZeroCrucialDefenderHysteresis.isUpper())
		{
			// explicitly use zero defenders here (overwriting minDefendersForBallThreat here):
			// ball can not reasonably be marked
			numBotsForBall = 0;
		} else if (angleOneCrucialDefenderHysteresis.isUpper())
		{
			numBotsForBall = Math.min(1, numBotsForBall);
		}

		if (onlyOneCrucialDuringIndirect && getAiFrame().getGameState().isIndirectFreeForThem())
		{
			numBotsForBall = Math.min(1, numBotsForBall);
		}

		return numBotsForBall;
	}


	private boolean isBallInOurHalf()
	{
		final double ballXPos = getBall().getTrajectory().getPosByTime(ballLookahead).x();
		ballXValueHysteresis.setUpperThreshold(minXValueForBallUpper);
		ballXValueHysteresis.setLowerThreshold(minXValueForBallLower);
		ballXValueHysteresis.update(ballXPos);
		return ballXValueHysteresis.isLower();
	}
}
