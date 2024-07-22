/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ball.trajectory.BallFactory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.SimpleTimeAwareBallObstacle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class PassObstacleCalc extends ACalculator
{
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	@Getter
	private Map<EOffensiveActionType, List<IObstacle>> primaryObstacles = Map.of();
	@Getter
	private Map<EOffensiveActionType, List<IObstacle>> allPassObstacles = Map.of();


	@Override
	protected void doCalc()
	{
		var byBot = offensiveActions.get().entrySet().stream()
				.filter(entry -> desiredBots.get().getOrDefault(EPlay.OFFENSIVE, Set.of()).contains(entry.getKey()))
				.map(entry -> createObstacles(entry.getKey(), entry.getValue()))
				.flatMap(Optional::stream)
				.toList();

		primaryObstacles = reorderByBotToByActionType(byBot, true);
		allPassObstacles = reorderByBotToByActionType(byBot, false);

		getShapes(EAiShapesLayer.OFFENSE_PASS_OBSTACLES).addAll(
				allPassObstacles.values().stream()
						.flatMap(List::stream)
						.map(IObstacle::getShapes)
						.flatMap(List::stream)
						.map(shape -> shape.setColor(getAiFrame().getTeamColor().getColor()))
						.toList()
		);
	}


	private Optional<ObstaclesData> createObstacles(BotID botID, RatedOffensiveAction ratedOffensiveAction)
	{
		var offensiveAction = ratedOffensiveAction.getAction();
		if (offensiveAction == null)
		{
			return Optional.empty();
		}
		var kick = offensiveAction.getKick();
		if (kick == null)
		{
			return Optional.empty();
		}
		var fac = new BallFactory(getBall().getTrajectory().getParameters());
		var trajectory = fac.createTrajectoryFromKickedBallWithoutSpin(kick.getSource(),
				kick.getKickVel().multiplyNew(1000));
		var tStart = getBall().getTrajectory().getTimeByPos(kick.getSource());
		var passObstacle = new SimpleTimeAwareBallObstacle(trajectory,
				2 * Geometry.getBotRadius() + Geometry.getBallRadius(), tStart, 1);
		var type = offensiveAction.getType();
		if (tStart == 0)
		{
			return Optional.of(new ObstaclesData(botID, type, tStart, List.of(passObstacle)));
		} else
		{
			return Optional.of(new ObstaclesData(botID, type, tStart, List.of(
					new SimpleTimeAwareBallObstacle(getBall().getTrajectory(),
							2 * Geometry.getBotRadius() + Geometry.getBallRadius(), 0, tStart),
					passObstacle
			)));
		}
	}


	private Map<EOffensiveActionType, List<IObstacle>> reorderByBotToByActionType(List<ObstaclesData> data,
			boolean filterOnlyFirst)
	{
		return Arrays.stream(EOffensiveActionType.values())
				.map(key -> extractOfType(data, key, filterOnlyFirst))
				.flatMap(Optional::stream)
				.collect(Collectors.toUnmodifiableMap(ObstaclesData::type, ObstaclesData::obstacles));
	}


	private Optional<ObstaclesData> extractOfType(List<ObstaclesData> data, EOffensiveActionType type,
			boolean filterOnlyFirst)
	{
		List<IObstacle> obstacles;
		if (filterOnlyFirst)
		{
			obstacles = data.stream()
					.filter(d -> d.type == type)
					.min(Comparator.comparingDouble(ObstaclesData::tStart))
					.map(ObstaclesData::obstacles)
					.orElseGet(List::of);
		} else
		{
			obstacles = data.stream()
					.filter(d -> d.type == type)
					.map(ObstaclesData::obstacles)
					.flatMap(List::stream)
					.toList();
		}
		if (obstacles.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(new ObstaclesData(BotID.noBot(), type, -1, obstacles));
	}


	private record ObstaclesData(BotID botID, EOffensiveActionType type, double tStart, List<IObstacle> obstacles)
	{
	}
}
