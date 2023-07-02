/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Track the ongoing pass by matching passes with the ball kick fit state.
 */
@Log4j2
public class PassStatisticsCalc extends ACalculator
{
	@Configurable(defValue = "500.0", comment = "[mm]")
	private static double filterBlockedPassesMinDist = 500.0;

	private final Supplier<Optional<OngoingPass>> ongoingPass;

	private OngoingPass currentPass;

	@Getter
	private final PassStats passStats;


	public PassStatisticsCalc(Supplier<Optional<OngoingPass>> ongoingPass)
	{
		this.ongoingPass = ongoingPass;
		Map<EOffensiveZone, Map<EOffensiveZone, ZonePassStats>> zonePassSuccessMap;
		zonePassSuccessMap = new EnumMap<>(EOffensiveZone.class);
		for (EOffensiveZone outerZone : EOffensiveZone.values())
		{
			Map<EOffensiveZone, ZonePassStats> innerMap = new EnumMap<>(EOffensiveZone.class);

			for (EOffensiveZone innerZone : EOffensiveZone.values())
			{
				innerMap.put(innerZone, new ZonePassStats());
			}
			zonePassSuccessMap.put(outerZone, innerMap);
		}
		passStats = new PassStats(zonePassSuccessMap);
	}


	@Override
	protected void doCalc()
	{

		var ongoingPassOpt = ongoingPass.get();
		if (ongoingPassOpt.isPresent())
		{
			if (currentPass != null && ongoingPassOpt.get().getKickStartTime() != currentPass.getKickStartTime())
			{
				// new pass started, while old was still active
				storeOngoingPass(currentPass);
			}
			currentPass = ongoingPassOpt.get();
		} else if (currentPass != null)
		{
			// old pass vanished
			storeOngoingPass(currentPass);
			currentPass = null;
		}

		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.PASS_STATS);
		shapes.add(getDrawableText("Pass Stats: ", 0));
		shapes.add(getDrawableText("num of passes: " + passStats.getNPasses(), 1));
		shapes.add(getDrawableText("num successful: " + passStats.getSuccessfulPasses(), 2));
		shapes.add(getDrawableText(
				"ratio: " + String.format("%.2f", passStats.getSuccessfulPasses() / (double) passStats.getNPasses()), 3));
	}


	private void storeOngoingPass(OngoingPass currentPass)
	{
		if (currentPass.getPass().getKick().getSource().distanceTo(getWFrame().getBall().getPos()) < filterBlockedPassesMinDist)
		{
			return;
		}

		passStats.setNPasses(passStats.getNPasses() + 1);
		var newValue = new ZonePassStats();
		if (passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
				.containsKey(currentPass.getTargetZone()))
		{
			newValue = passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
					.get(currentPass.getTargetZone());
		}
		if (OngoingPassSuccessRater.isOngoingPassASuccess(getWFrame(), currentPass))
		{
			passStats.setSuccessfulPasses(passStats.getSuccessfulPasses() + 1);
			newValue = new ZonePassStats(newValue.getTotalPasses() + 1, newValue.getSuccessfulPasses() + 1);
		} else
		{
			newValue = new ZonePassStats(newValue.getTotalPasses() + 1, newValue.getSuccessfulPasses());
		}
		passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
				.put(currentPass.getTargetZone(), newValue);
	}


	private IDrawableShape getDrawableText(final String text, final int offset)
	{
		return new DrawableBorderText(Vector2.fromXY(1, 5.0 + offset), text)
				.setColor(getWFrame().getTeamColor() == ETeamColor.YELLOW ? Color.YELLOW : Color.BLUE);
	}
}
