/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Crucial offenders may not be taken by defense. But there is no guaranty, that a crucial offender is actually
 * becoming an offender!
 */
@RequiredArgsConstructor
public class CrucialOffenderCalc extends ACalculator
{

	private final Supplier<Optional<OngoingPass>> ongoingPass;

	@Configurable(defValue = "500.0")
	private static double distToBallForCrucialOffender = 500.0;

	@Configurable(defValue = "500.0")
	private static double closestToBallOffset = 500.0;

	@Configurable(defValue = "0.3")
	private static double corridorWidthConsideredAsSafeCatch = 0.3;

	@Configurable(defValue = "-0.2")
	private static double corridorMinSlackTimeConsideredAsSafeCatch = -0.2;

	@Getter
	private Set<BotID> crucialOffender;


	@Override
	public void doCalc()
	{
		var ballInterceptions = getAiFrame().getPrevFrame().getTacticalField().getBallInterceptions();
		crucialOffender = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.OFFENSIVE, Collections.emptySet()).stream()
				.filter(e -> getWFrame().getBots().containsKey(e))
				.filter(
						e -> tigerIsCloserToBall(e) || isCloseToBall(e) || (ballInterceptions.containsKey(e) && canCatchBall(
								ballInterceptions, e)))
				.collect(Collectors.toSet());
		ongoingPass.get().ifPresent(e -> crucialOffender.add(e.getPass().getReceiver()));
	}


	private boolean tigerIsCloserToBall(BotID e)
	{
		return getWFrame().getBot(e).getPos().distanceTo(getWFrame().getBall().getPos()) <
				getAiFrame().getPrevFrame().getTacticalField().getOpponentClosestToBall().getDist() + closestToBallOffset;
	}


	private boolean canCatchBall(Map<BotID, RatedBallInterception> ballInterceptions, BotID e)
	{
		return ballInterceptions.get(e).getCorridorLength() > corridorWidthConsideredAsSafeCatch
				|| ballInterceptions.get(e).getMinCorridorSlackTime() < corridorMinSlackTimeConsideredAsSafeCatch;
	}


	private boolean isCloseToBall(BotID e)
	{
		return getWFrame().getBot(e).getPos().distanceTo(getBall().getPos()) < distToBallForCrucialOffender;
	}
}
