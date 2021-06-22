/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
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

	@Getter
	private Set<BotID> crucialOffender;


	@Override
	public void doCalc()
	{
		crucialOffender = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.OFFENSIVE, Collections.emptySet()).stream()
				.filter(e -> getWFrame().getBot(e).getPos().distanceTo(getBall().getPos()) < distToBallForCrucialOffender)
				.collect(Collectors.toSet());
		ongoingPass.get().ifPresent(e -> crucialOffender.add(e.getPass().getReceiver()));
	}
}
