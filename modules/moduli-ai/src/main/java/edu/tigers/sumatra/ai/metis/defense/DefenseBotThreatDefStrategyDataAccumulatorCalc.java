/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DefenseBotThreatDefStrategyDataAccumulatorCalc extends ADefenseThreatCalc
{
	private final Supplier<List<DefenseBotThreatDefStrategyData>> centerBackDefData;
	private final Supplier<List<DefenseBotThreatDefStrategyData>> man2ManDefData;
	private final Supplier<DefenseBallThreat> defenseBallThreat;

	private final DefenseThreatRater defenseThreatRater = new DefenseThreatRater();

	@Getter
	private List<DefenseBotThreatDefData> allDefenseBotThreatDefData;
	@Getter
	private List<DefenseBotThreatDefData> inDangerZoneBotThreatDefData;

	private final Map<BotID, Hysteresis> opponentDangerZoneHysteresis = new HashMap<>();


	@Override
	protected void doCalc()
	{
		updateOpponentDangerZoneHystereses();
		allDefenseBotThreatDefData = centerBackDefData.get().stream()
				.map(this::buildThreatFromCenterBackData)
				.sorted(Comparator.comparingDouble(DefenseBotThreatDefData::threatRating).reversed())
				.toList();

		inDangerZoneBotThreatDefData = allDefenseBotThreatDefData.stream()
				.filter(data -> movingInDangerZone(data.bot().getBotId()))
				.toList();

		for (int i = 0; i < allDefenseBotThreatDefData.size(); i++)
		{
			getShapes(EAiShapesLayer.DEFENSE_THREAT_RATING_REDUCTION).addAll(
					allDefenseBotThreatDefData.get(i).drawShapes(i)
			);
		}
	}


	private void updateOpponentDangerZoneHystereses()
	{
		for (ITrackedBot bot : getWFrame().getOpponentBots().values())
		{
			final Hysteresis hysteresis = opponentDangerZoneHysteresis.computeIfAbsent(
					bot.getBotId(),
					botID -> new Hysteresis(0, 1)
			);
			hysteresis.setLowerThreshold(DefenseThreatRater.getDangerDropOffX() - 100);
			hysteresis.setUpperThreshold(DefenseThreatRater.getDangerDropOffX() + 100);
			hysteresis.update(predictedOpponentPos(bot).x());
		}
	}


	private boolean movingInDangerZone(final BotID botID)
	{
		return opponentDangerZoneHysteresis.get(botID).isLower();
	}


	private DefenseBotThreatDefData buildThreatFromCenterBackData(DefenseBotThreatDefStrategyData centerBackData)
	{
		var bot = getWFrame().getBot(centerBackData.threatID());
		var m2mData = man2ManDefData.get().stream()
				.filter(m2m -> m2m.threatID() == centerBackData.threatID())
				.findAny();
		var threatRating = defenseThreatRater.getThreatRatingOfRobot(defenseBallThreat.get().getPos(), bot);
		getShapes(EAiShapesLayer.DEFENSE_THREAT_RATING).addAll(defenseThreatRater.drawShapes());
		return new DefenseBotThreatDefData(bot, centerBackData, m2mData.orElse(null), threatRating);
	}
}
