/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DefenseBotThreatDefStrategyDataAccumulatorCalc extends ADefenseThreatCalc
{
	private final Supplier<List<DefenseBotThreatDefStrategyData>> centerBackDefData;
	private final Supplier<List<DefenseBotThreatDefStrategyData>> man2ManDefData;
	private final Supplier<DefenseBallThreat> defenseBallThreat;

	private final DefenseThreatRater defenseThreatRater = new DefenseThreatRater();

	@Getter
	private List<DefenseBotThreatDefData> defenseBotThreatDefData;


	@Override
	protected void doCalc()
	{
		defenseBotThreatDefData = centerBackDefData.get().stream()
				.map(this::buildThreatFromCenterBackData)
				.sorted(Comparator.comparingDouble(DefenseBotThreatDefData::threatRating).reversed())
				.toList();
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
