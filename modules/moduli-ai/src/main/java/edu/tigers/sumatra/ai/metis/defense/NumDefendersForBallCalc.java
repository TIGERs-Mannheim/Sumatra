/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


/**
 * Calculate the desired number of defenders for the ball threat.
 */
@RequiredArgsConstructor
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

	@Configurable(comment = "Max number of defenders for covering the ball", defValue = "2")
	private static int maxDefendersForBallThreat = 2;

	@Configurable(comment = "Minimum number of defenders if the ball responsibility is at the defense", defValue = "1")
	private static int minDefendersForBallThreatWhenBallResponsibilityDefense = 1;

	private final Hysteresis angleOneCrucialDefenderHysteresis = new Hysteresis(
			angleThresholdOneCrucialDefenderLower,
			angleThresholdOneCrucialDefenderUpper);

	private final Hysteresis angleZeroCrucialDefenderHysteresis = new Hysteresis(
			angleThresholdZeroCrucialDefenderLower,
			angleThresholdZeroCrucialDefenderUpper);

	private final Hysteresis ballXValueHysteresis = new Hysteresis(
			minXValueForBallLower,
			minXValueForBallUpper);


	private final Supplier<EBallResponsibility> ballResponsibility;

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

		if (needToPrepareForDirectFreeThem())
		{
			numBotsForBall = Math.min(getBallDefenderForFreeKick(), 2 * maxDefendersForBallThreat);
		}

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

		if (ballResponsibility.get() == EBallResponsibility.DEFENSE)
		{
			numBotsForBall = Math.max(minDefendersForBallThreatWhenBallResponsibilityDefense, numBotsForBall);
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


	private boolean needToPrepareForDirectFreeThem()
	{
		var gameState = getAiFrame().getGameState();
		if (gameState.isFreeKickForThem())
		{
			return true;
		}
		if (gameState.isNextGameStateForUs() || gameState.isGameRunning())
		{
			return false;
		}

		return gameState.getNextState() == EGameState.DIRECT_FREE;
	}


	private int getBallDefenderForFreeKick()
	{
		var ballPos = getAiFrame().getGameState().isBallPlacement() ?
				getAiFrame().getGameState().getBallPlacementPosition() :
				getBall().getPos();

		var ballLeftPost = Lines.segmentFromPoints(ballPos, Geometry.getGoalOur().getLeftPost());
		var ballRightPost = Lines.segmentFromPoints(ballPos, Geometry.getGoalOur().getRightPost());

		var penArea = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2);
		var defPosLeft = penArea.intersectPerimeterPath(ballLeftPost).stream().findAny();
		var defPosRight = penArea.intersectPerimeterPath(ballRightPost).stream().findAny();

		if (defPosLeft.isEmpty() || defPosRight.isEmpty())
		{
			return maxDefendersForBallThreat;
		}
		var distance = defPosRight.get().distanceTo(defPosLeft.get());
		return (int) Math.ceil(distance / (2 * Geometry.getBotRadius()));
	}
}
