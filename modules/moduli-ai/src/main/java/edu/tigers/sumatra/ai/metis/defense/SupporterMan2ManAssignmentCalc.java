/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class SupporterMan2ManAssignmentCalc extends ACalculator
{
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<List<DefenseThreatAssignment>> defenseThreatAssignment;
	private final Supplier<List<DefenseBotThreat>> allDefenseMan2ManBotThreats;
	private final Supplier<DefenseBallThreat> defenseBallThreat;

	@Getter
	private Map<BotID, DefenseBotThreat> supporterToBotThreatMapping = Collections.emptyMap();


	@Override
	public void doCalc()
	{
		supporterToBotThreatMapping = calcSupporterToBotThreatMapping();
	}


	private Map<BotID, DefenseBotThreat> calcSupporterToBotThreatMapping()
	{
		var defenders = desiredBots.get().getOrDefault(EPlay.DEFENSIVE, Collections.emptySet());
		var protectedOpponentObjects = defenseThreatAssignment.get().stream()
				.filter(dta -> dta.getBotIds().stream().anyMatch(defenders::contains))
				.map(assignment -> assignment.getThreat().getObjectId())
				.collect(Collectors.toSet());

		var unprotectedBallToBotThreats = allDefenseMan2ManBotThreats.get().stream()
				.filter(threat -> threat.getDefendStrategy() == EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER)
				.filter(threat -> !protectedOpponentObjects.contains(threat.getObjectId()))
				.sorted(Comparator.comparingDouble(this::pressingScore))
				.toList();

		if (!desiredBots.get().containsKey(EPlay.SUPPORT))
		{
			return Map.of();
		}
		var supporters = desiredBots.get().get(EPlay.SUPPORT).stream().map(id -> getWFrame().getBot(id)).toList();
		Map<BotID, DefenseBotThreat> mapping = new HashMap<>();
		for (var threat : unprotectedBallToBotThreats)
		{
			var closestSupporter = supporters.stream()
					.filter(s -> !mapping.containsKey(s.getBotId()))
					.min(Comparator.comparingDouble(supporter -> threat.getPos().distanceToSqr(supporter.getPos())));
			if (closestSupporter.isEmpty())
			{
				break;
			}

			mapping.put(closestSupporter.get().getBotId(), threat);
		}
		return Collections.unmodifiableMap(mapping);
	}


	private double pressingScore(DefenseBotThreat threat)
	{
		var ball = defenseBallThreat.get().getPos();
		var bot = threat.getPos();
		var passScore = new DefenseThreatRater().calcPassDistanceScore(ball, bot);

		var factor = SumatraMath.cap(1 - (bot.x() - ball.x()) / 1000, 0, 1);
		return passScore * factor;
	}

}
