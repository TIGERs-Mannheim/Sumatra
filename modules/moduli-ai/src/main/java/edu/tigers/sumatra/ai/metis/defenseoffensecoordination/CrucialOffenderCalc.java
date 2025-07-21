/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defenseoffensecoordination;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Objects;
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
	@Configurable(defValue = "500.0")
	private static double distToBallForCrucialOffender = 500.0;
	@Configurable(defValue = "500.0")
	private static double closestToBallOffset = 500.0;
	private final Supplier<Optional<OngoingPass>> ongoingPass;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<ITrackedBot> opponentPassReceiver;
	@Getter
	private Set<BotID> crucialOffender;


	@Override
	public void doCalc()
	{
		if (Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius()).isPointInShape(getBall().getPos())
				&& getBall().getVel().getLength() > 0.3)
		{
			crucialOffender = Collections.emptySet();
			return;
		}

		crucialOffender = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.OFFENSIVE, Collections.emptySet())
				.stream()
				.filter(e -> !getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORTIVE_ATTACKER)
						.stream().map(ARole::getBotID)
						.toList().contains(e)) // do not add past supportive attackers as crucial
				.filter(e -> getWFrame().getBots().containsKey(e))
				.filter(e -> tigerIsCloserToBall(e) || isCloseToBall(e))
				.collect(Collectors.toSet());

		ongoingPass.get().ifPresent(e -> crucialOffender.add(e.getPass().getReceiver()));

		getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_OFFENDERS).addAll(
				crucialOffender.stream()
						.map(botID -> getWFrame().getBot(botID))
						.filter(Objects::nonNull) // onGoingPass Receiver might be a not existing BotID
						.map(ITrackedBot::getPos)
						.map(p -> Circle.createCircle(p, Geometry.getBotRadius() + 15))
						.map(c -> new DrawableCircle(c, new Color(60, 255, 60, 200)).setFill(true))
						.toList()
		);
	}


	private boolean tigerIsCloserToBall(BotID e)
	{
		var opponentDist = Optional.ofNullable(opponentPassReceiver.get())
				.map(ITrackedObject::getPos)
				.map(getBall().getPos()::distanceTo)
				.orElseGet(() -> opponentClosestToBall.get().getDist());
		return getWFrame().getBot(e).getPos().distanceTo(getWFrame().getBall().getPos())
				< opponentDist + closestToBallOffset;
	}


	private boolean isCloseToBall(BotID e)
	{
		return getWFrame().getBot(e).getPos().distanceTo(getBall().getPos()) < distToBallForCrucialOffender;
	}
}
