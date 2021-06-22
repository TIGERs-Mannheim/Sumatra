/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.kickoff;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Calculate the parameters for the kickoff.
 */
@RequiredArgsConstructor
public class KickoffActionsCalc extends ACalculator
{
	@Configurable(comment = "The minimum score of a direct shot to do it", defValue = "0.5")
	private static double minimumScoreForDirectKick = 0.5;

	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<GoalKick> goalKick;

	@Getter
	private KickoffStrategy kickoffStrategy;


	@Override
	public boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isKickoffOrPrepareKickoffForUs();
	}


	@Override
	protected void reset()
	{
		kickoffStrategy = new KickoffStrategy();
	}


	@Override
	public void doCalc()
	{
		kickoffStrategy = findStrategy();
	}


	private KickoffStrategy findStrategy()
	{
		var bestGoalKick = goalKick.get();
		if (bestGoalKick != null && bestGoalKick.getRatedTarget().getScore() >= minimumScoreForDirectKick)
		{
			return new KickoffStrategy(null, bestGoalKick.getKick(), bestMovingPosition());
		}

		var bestPass = selectedPasses.get().values().stream().findFirst().map(RatedPass::getPass).orElse(null);
		if (bestPass != null)
		{
			return new KickoffStrategy(bestPass, bestPass.getKick(), bestMovingPosition());
		}

		if (bestGoalKick != null)
		{
			return new KickoffStrategy(null, bestGoalKick.getKick(), bestMovingPosition());
		}
		return new KickoffStrategy(null, null, bestMovingPosition());
	}


	private List<IVector2> bestMovingPosition()
	{
		List<IVector2> bestMovementPositions = new ArrayList<>(2);
		bestMovementPositions.add(Vector2.fromXY(-300, Math.min(1500, Geometry.getFieldWidth() * 0.4)));
		bestMovementPositions.add(Vector2.fromXY(-500, -Math.min(1000, Geometry.getFieldWidth() * 0.3)));
		return bestMovementPositions;
	}
}
