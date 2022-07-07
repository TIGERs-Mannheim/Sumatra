/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.offense.EFreeKickDelay;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


@RequiredArgsConstructor
public class BallHandlingRobotsStrategyCalc extends ACalculator
{
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<EFreeKickDelay> freeKickDelay;

	@Getter
	private EOffensiveStrategy ballHandlingRobotStrategy;


	@Override
	protected void doCalc()
	{
		ballHandlingRobotStrategy = getOffensiveStrategyForPrimary();
	}


	private EOffensiveStrategy getOffensiveStrategyForPrimary()
	{
		if (getAiFrame().getGameState().isStoppedGame())
		{
			return EOffensiveStrategy.STOP;
		}
		if (getAiFrame().getGameState().isPenaltyOrPreparePenaltyForUs())
		{
			return EOffensiveStrategy.PENALTY_KICK;
		}
		if (freeKickDelay.get() == EFreeKickDelay.IN_PROGRESS)
		{
			return EOffensiveStrategy.DELAY;
		}
		if (getAiFrame().getGameState().isStandardSituationForThem() ||
				getAiFrame().getGameState().isKickoffOrPrepareKickoffForThem())
		{
			return EOffensiveStrategy.INTERCEPT;
		}
		if (skirmishInformation.get().isStartCircleMove())
		{
			return EOffensiveStrategy.FREE_SKIRMISH;
		}
		return EOffensiveStrategy.KICK;
	}
}
