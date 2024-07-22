/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
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
	@Configurable(comment = "Max number of defenders for covering the ball", defValue = "2")
	private static int maxDefendersForBallThreat = 2;

	@Configurable(comment = "Minimum number of defenders if the ball responsibility is at the defense", defValue = "1")
	private static int minDefendersForBallThreatWhenBallResponsibilityDefense = 1;


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
		int numBotsForBall = maxDefendersForBallThreat;

		if (needToPrepareForDirectFreeThem())
		{
			numBotsForBall = Math.min(getBallDefenderForFreeKick(), 2 * maxDefendersForBallThreat);
		}

		if (ballResponsibility.get() == EBallResponsibility.DEFENSE)
		{
			numBotsForBall = Math.max(minDefendersForBallThreatWhenBallResponsibilityDefense, numBotsForBall);
		}

		return numBotsForBall;
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
