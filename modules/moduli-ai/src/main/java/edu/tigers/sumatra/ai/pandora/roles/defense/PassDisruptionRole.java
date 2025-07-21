/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefensePassDisruptionStrategyType;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Defender that protects near an opponent (marking the threat).
 */
public class PassDisruptionRole extends ADefenseRole
{


	public PassDisruptionRole()
	{
		super(ERole.PASS_DISRUPTION_DEFENDER);

		setInitialState(new DefendState());
	}


	@Override
	protected void beforeUpdate()
	{
		getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).add(
				new DrawableCircle(getAssignment().getInterceptionPoint(), 30, Color.MAGENTA)
		);
		getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).add(
				new DrawableCircle(getAssignment().getMovementDestination(), 30, Color.MAGENTA)
		);
	}


	private DefensePassDisruptionAssignment getAssignment()
	{
		return getTacticalField().getDefensePassDisruptionAssignment();
	}


	private class DefendState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.setKickParams(calcKickParams());

			final IVector2 destination = moveToValidDest(findDest());
			skill.updateDestination(destination);
			skill.updateTargetAngle(getBall().getVel().multiplyNew(-1).getAngle());
			skill.getMoveCon().setIgnoredBots(getIgnoredBots());
			skill.getMoveCon().setBallObstacle(false);
		}


		private IVector2 findDest()
		{
			if (getAssignment().getStrategyType() == EDefensePassDisruptionStrategyType.DISRUPT_PASS)
			{
				return getAssignment().getMovementDestination();
			}

			// Ensure that we do not stop battling for the interception position
			var opponent = getWFrame().getOpponentBot(getAssignment().getThreatId());
			var opponentTravelLine = Lines.segmentFromPoints(opponent.getPos(), getAssignment().getInterceptionPoint());

			var defendedArea = Circle.createCircle(getPos(), 2 * Geometry.getBotRadius());

			var destination = opponentTravelLine.intersect(defendedArea).stream()
					.min(Comparator.comparingDouble(intersection -> opponent.getPos().distanceTo(intersection)))
					.orElse(getAssignment().getInterceptionPoint());

			getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).addAll(List.of(
					new DrawableLine(opponentTravelLine, Color.PINK),
					new DrawableLine(getAssignment().getInterceptionPoint(), destination, Color.BLUE)
			));

			return destination;
		}


		private Set<BotID> getIgnoredBots()
		{
			var opponent = getTacticalField().getOpponentPassReceiver();
			if (getAssignment().isIgnoreOpponentPassReceiverInPathPlanning() && opponent != null)
			{
				return Stream.concat(
						Stream.of(opponent.getBotId()),
						skill.getMoveCon().getIgnoredBots().stream()
				).collect(Collectors.toUnmodifiableSet());
			}
			return skill.getMoveCon().getIgnoredBots();
		}
	}
}
