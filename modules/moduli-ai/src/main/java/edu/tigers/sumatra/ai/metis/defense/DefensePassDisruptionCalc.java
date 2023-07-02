/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class DefensePassDisruptionCalc extends ACalculator
{
	@Configurable(defValue = "0.05", comment = "[s] Maximum Time after the pass kick the Pass disruption must start")
	private double maxTimeAfterPass = 0.05;

	private final Supplier<ITrackedBot> opponentPassReceiver;


	@Getter
	private DefensePassDisruptionAssignment currentAssignment = null;


	private Map<ThreatToDefenderAssignment, TimestampTimer> coolDownTimer = new HashMap<>();


	@Override
	protected void reset()
	{
		currentAssignment = null;
	}


	@Override
	protected void doCalc()
	{
		updateTimers();
		if (opponentPassReceiver.get() == null)
		{
			currentAssignment = null;
			return;
		}


		if (currentAssignment == null || !currentAssignment.getThreatId().equals(opponentPassReceiver.get().getBotId()))
		{
			currentAssignment = findNewDisruptionAssignment().orElse(null);
		}
		if (currentAssignment != null)
		{
			getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION)
					.add(new DrawableLine(
							Lines.segmentFromPoints(getWFrame().getBot(currentAssignment.getThreatId()).getPos(),
									getWFrame().getBot(currentAssignment.getDefenderId()).getPos()), Color.RED));
		}
	}


	private void updateTimers()
	{
		var tNow = getWFrame().getTimestamp();
		var lastFrameActiveAssignments = getAiFrame().getPrevFrame().getTacticalField().getDefenseOuterThreatAssignments()
				.stream()
				.filter(dta -> dta.getThreat().getType() == EDefenseThreatType.BOT_M2M)
				.filter(dta -> dta.getThreat().getObjectId().isBot())
				.flatMap(dta -> dta.getBotIds().stream()
						.map(botID -> new ThreatToDefenderAssignment((BotID) dta.getThreat().getObjectId(),
								botID))
				)
				.collect(Collectors.toUnmodifiableSet());
		lastFrameActiveAssignments
				.forEach(ass -> coolDownTimer.computeIfAbsent(ass, k -> new TimestampTimer(maxTimeAfterPass)));
		coolDownTimer.entrySet().stream()
				.filter(e -> !(lastFrameActiveAssignments.contains(e.getKey())))
				.forEach(e -> e.getValue().update(tNow));
		coolDownTimer.forEach((k, v) -> getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION)
				.add(new DrawableAnnotation(getBall().getPos(),
						k.threatId() + " - " + k.defenderId() + ": " + v.getRemainingTime(tNow),
						getAiFrame().getTeamColor().getColor())
						.withOffset(Vector2.fromY(k.threatId().getNumberWithColorOffset() * 50.0))));
		coolDownTimer.entrySet().removeIf(e -> e.getValue().isTimeUp(tNow));
	}


	private Optional<DefensePassDisruptionAssignment> findNewDisruptionAssignment()
	{
		return coolDownTimer.keySet().stream()
				.filter(assignment -> assignment.threatId().equals(opponentPassReceiver.get().getBotId()))
				.min(Comparator.comparingDouble(ass -> getWFrame().getBot(ass.defenderId()).getPos()
						.distanceToSqr(getWFrame().getBot(ass.threatId()).getPos()))
				)
				.map(this::toDisruptionAssignment)
				.filter(this::assignmentIsFeasible);
	}


	private DefensePassDisruptionAssignment toDisruptionAssignment(ThreatToDefenderAssignment assignment)
	{
		var ballLine = Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel());
		var interceptionPoint = ballLine.closestPointOnPath(
				getWFrame().getBot(assignment.defenderId()).getPosByTime(0.3));
		return new DefensePassDisruptionAssignment(assignment.threatId(), assignment.defenderId(),
				interceptionPoint);
	}


	private boolean assignmentIsFeasible(DefensePassDisruptionAssignment assign)
	{
		var bot = getWFrame().getBot(assign.getDefenderId());
		var robotTime = TrajectoryGenerator.generatePositionTrajectory(bot, assign.getInterceptionPoint()).getTotalTime();
		var ballTime = getBall().getTrajectory().getTimeByPos(assign.getInterceptionPoint());

		var a = bot.getMoveConstraints().getAccMax();
		var v = bot.getMoveConstraints().getVelMax();
		// The time diff between breaking from velMax to a full stop vs keep going with velMax and overshoot
		// In addition to a constant offset
		var maxTimeDiff = 0.5 * v / a + 0.3;

		return robotTime - ballTime <= maxTimeDiff;
	}


	private record ThreatToDefenderAssignment(BotID threatId, BotID defenderId)
	{
	}
}
