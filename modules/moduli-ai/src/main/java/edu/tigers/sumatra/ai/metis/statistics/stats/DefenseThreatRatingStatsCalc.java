/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatReductionRater;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class DefenseThreatRatingStatsCalc extends AStatsCalc
{
	private final DefenseThreatReductionRater defenseThreatReductionRater = new DefenseThreatReductionRater();

	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<List<DefenseBotThreatDefData>> defenseBotThreats;
	private final Supplier<Map<Integer, Double>> defenseThreatRatingForNumDefender;
	private final Supplier<List<DefenseThreatAssignment>> defenseThreatAssignments;


	private double botRatingRaw;
	private MovingAverage botRatingRawAverage = new MovingAverage();
	private double botRatingWanted;
	private MovingAverage botRatingWantedAverage = new MovingAverage();
	private double botRatingActual;
	private MovingAverage botRatingActualAverage = new MovingAverage();


	@Override
	public void saveStatsToMatchStatistics(MatchStats matchStatistics)
	{
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_RAW,
				new StatisticData(botRatingRaw));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_RAW_AVG,
				new StatisticData(botRatingRawAverage.getCombinedValue()));

		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_WANTED,
				new StatisticData(botRatingWanted));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_WANTED_AVG,
				new StatisticData(botRatingWantedAverage.getCombinedValue()));

		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_ACTUAL,
				new StatisticData(botRatingActual));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_BOT_THREAT_RATING_ACTUAL_AVG,
				new StatisticData(botRatingActualAverage.getCombinedValue()));
	}


	@Override
	public void onStatisticUpdate(BaseAiFrame baseAiFrame)
	{
		var defenderIDs = defenseThreatAssignments.get().stream()
				.filter(ta -> ta.getThreat().getType() != EDefenseThreatType.BALL)
				.flatMap(ta -> ta.getBotIds().stream().filter(
						botID -> desiredBots.get().getOrDefault(EPlay.DEFENSIVE, Collections.emptySet()).contains(botID)))
				.collect(Collectors.toSet());
		var defenders = defenderIDs.stream()
				.map(botID -> baseAiFrame.getWorldFrame().getBot(botID))
				.filter(Objects::nonNull)
				.toList();

		botRatingRaw = defenseThreatRatingForNumDefender.get().getOrDefault(0, 0.0);
		botRatingWanted = defenseThreatRatingForNumDefender.get().getOrDefault(defenderIDs.size(), 0.0);
		botRatingActual = defenseThreatReductionRater.calcThreatRatingActual(defenseBotThreats.get(), defenders,
				defenseThreatAssignments.get(), baseAiFrame.getWorldFrame().getBall().getPos());
		botRatingRawAverage.add(botRatingRaw);
		botRatingWantedAverage.add(botRatingWanted);
		botRatingActualAverage.add(botRatingActual);

	}
}
