/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
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
	@Configurable(defValue = "0.3")
	private static double corridorWidthConsideredAsSafeCatch = 0.3;
	@Configurable(defValue = "-0.2")
	private static double corridorMinSlackTimeConsideredAsSafeCatch = -0.2;
	private final Supplier<Optional<OngoingPass>> ongoingPass;
	@Getter
	private Set<BotID> crucialOffender;


	@Override
	public void doCalc()
	{
		var ballInterceptions = getAiFrame().getPrevFrame().getTacticalField().getBallInterceptions();

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
				.filter(
						e -> tigerIsCloserToBall(e) || isCloseToBall(e) || (ballInterceptions.containsKey(e) && canCatchBall(
								ballInterceptions, e)))
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
