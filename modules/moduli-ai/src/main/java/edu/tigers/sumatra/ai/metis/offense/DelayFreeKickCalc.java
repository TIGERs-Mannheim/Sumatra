/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DelayFreeKickCalc extends ACalculator
{
	@Configurable(comment = "This time is subtracted from the time that is still left for the freekick action", defValue = "1.5")
	private static double timeUntilKickOffset = 1.5;

	@Configurable(comment = "time in seconds", defValue = "3.5")
	private static double maxDelayWaitTime = 3.5;

	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTarget;

	private long startTime;

	@Getter
	private EFreeKickDelay freeKickDelay;


	@Override
	protected boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isStandardSituationForUs()
				|| getAiFrame().getGameState().isKickoffOrPrepareKickoffForUs();
	}


	@Override
	protected void reset()
	{
		freeKickDelay = EFreeKickDelay.OFF;
		startTime = 0;
	}


	@Override
	protected void doCalc()
	{
		if (startTime == 0 && getAiFrame().getGameState().isStandardSituationIncludingKickoffForUs())
		{
			startTime = getWFrame().getTimestamp();
		}
		double timeElapsed = startTime == 0 ? 0 : (getWFrame().getTimestamp() - startTime) * 1e-9;

		// maximize delay time by time until kick
		double timeRemaining = getAiFrame().getRefereeMsg().getCurrentActionTimeRemaining();
		double timeLeft = timeRemaining - timeUntilKickOffset;

		getShapes(EAiShapesLayer.OFFENSE_ATTACKER)
				.add(new DrawableAnnotation(getBall().getPos(), String.format("time left: %.2f", timeLeft),
						Vector2f.fromY(-200)));
		if (performDirectGoalKickNow() || timeLeft <= 0 || timeElapsed >= maxDelayWaitTime)
		{
			freeKickDelay = EFreeKickDelay.STOPPED;
		} else
		{
			freeKickDelay = EFreeKickDelay.IN_PROGRESS;
		}
	}


	private boolean performDirectGoalKickNow()
	{
		var score = bestGoalKickTarget.get().values().stream()
				.map(GoalKick::getRatedTarget)
				.mapToDouble(IRatedTarget::getScore)
				.min()
				.orElse(0.0);
		return (getAiFrame().getGameState().isFreeKickForUs() || getAiFrame().getGameState().isKickoff())
				&& tigerClosestToBall.get().getDist() < 400
				&& score > OffensiveConstants.getMinBotShouldDoGoalShotScore();
	}
}
