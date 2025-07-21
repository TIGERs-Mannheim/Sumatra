/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
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

	private TimeLimitedBuffer<IVector2> velocities = new TimeLimitedBuffer<>();

	private Map<EOffensiveZone, Map<EOffensiveZone, ZonePassStats>> zonePassSuccessMap;
	private List<PassStatsKickReceiveVelocity> receiveVelocities;

	private int nPasses = 0;
	private int successfulPasses = 0;
	private int numPassLineReachedOnTime = 0;

	@Getter
	private PassStats passStats;


	public PassStatisticsCalc(Supplier<Optional<OngoingPass>> ongoingPass)
	{
		this.ongoingPass = ongoingPass;
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
		receiveVelocities = new ArrayList<>();
		passStats = new PassStats(Collections.emptyMap(), Collections.emptyList(), 0, 0, 0);
	}


	@Override
	protected void doCalc()
	{
		var ongoingPassOpt = ongoingPass.get();
		if (ongoingPassOpt.isPresent())
		{
			velocities.setMaxDuration(0.2);
			velocities.setMaxElements(50);
			velocities.reduceByAbsoluteDuration(getWFrame().getTimestamp());

			if (getWFrame().getBots().containsKey(ongoingPassOpt.get().getPass().getReceiver()) &&
					!getWFrame().getBot(ongoingPassOpt.get().getPass().getReceiver()).getBallContact().hasContact())
			{
				velocities.add(getWFrame().getTimestamp(), getBall().getVel());
			}

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

		passStats = new PassStats(
				zonePassSuccessMap, receiveVelocities.stream().toList(), nPasses, successfulPasses,
				numPassLineReachedOnTime
		);

		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.PASS_STATS);
		shapes.add(getDrawableText("Pass Stats: ", 0));
		shapes.add(getDrawableText("num of passes: " + passStats.getNPasses(), 1));
		shapes.add(getDrawableText("num successful: " + passStats.getSuccessfulPasses(), 2));
		shapes.add(getDrawableText(
				"ratio: " + String.format("%.2f", passStats.getSuccessfulPasses() / (double) passStats.getNPasses()), 3));
		shapes.add(getDrawableText("num reached: " + passStats.getNumPassLineReachedOnTime(), 4));
		shapes.add(getDrawableText(
				"ratio: " + String.format(
						"%.2f",
						passStats.getNumPassLineReachedOnTime() / (double) passStats.getNPasses()
				), 5
		));
	}


	private void storeOngoingPass(OngoingPass currentPass)
	{
		if (currentPass.getPass().getKick().getSource().distanceTo(getWFrame().getBall().getPos())
				< filterBlockedPassesMinDist)
		{
			// probably an invalid pass
			return;
		}

		if (currentPass.isPassLineBeenReachedByReceiver())
		{
			numPassLineReachedOnTime++;
		}

		nPasses++;
		var newValue = new ZonePassStats();
		if (passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
				.containsKey(currentPass.getTargetZone()))
		{
			newValue = passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
					.get(currentPass.getTargetZone());
		}
		if (OngoingPassSuccessRater.isOngoingPassASuccess(getWFrame(), currentPass))
		{
			if (!velocities.getElements().isEmpty())
			{
				storeVelocities(currentPass, true);
			}

			successfulPasses++;
			newValue = new ZonePassStats(newValue.getTotalPasses() + 1, newValue.getSuccessfulPasses() + 1);
		} else
		{
			if (!velocities.getElements().isEmpty())
			{
				storeVelocities(currentPass, false);
			}

			newValue = new ZonePassStats(newValue.getTotalPasses() + 1, newValue.getSuccessfulPasses());
		}
		passStats.getZonePassSuccessMap().get(currentPass.getOriginatingZone())
				.put(currentPass.getTargetZone(), newValue);
	}


	private void storeVelocities(OngoingPass currentPass, boolean success)
	{
		IVector2 plannedVel = currentPass.getPass().getKick().getTarget()
				.subtractNew(currentPass.getPass().getKick().getSource())
				.scaleToNew(currentPass.getPass().getReceivingSpeed());
		IVector2 actualVel = velocities.getElements().get(Math.max(0, velocities.getElements().size() - 3));
		double actualPassDuration = (getWFrame().getTimestamp() - currentPass.getKickStartTime()) * 1e-9;
		receiveVelocities
				.add(new PassStatsKickReceiveVelocity(
						plannedVel, actualVel, success, currentPass.getPass().getDuration(),
						actualPassDuration, currentPass.getPass().getReceiveMode() == EBallReceiveMode.REDIRECT
				));
	}


	private IDrawableShape getDrawableText(final String text, final int offset)
	{
		double baseOffset = getWFrame().getTeamColor() == ETeamColor.YELLOW ? 9.8 : 16;
		return new DrawableBorderText(Vector2.fromXY(1, baseOffset + offset), text)
				.setColor(getWFrame().getTeamColor() == ETeamColor.YELLOW ? Color.YELLOW : Color.BLUE);
	}
}
