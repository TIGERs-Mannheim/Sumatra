/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ball.trajectory.BallFactory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.SimpleTimeAwareBallObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.TubeObstacle;
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
	@Configurable(defValue = "500", comment = "Radius of the pass obstacle [mm]. Should be large enough to compensate uncertainty of when the pass will be executed.")
	private static double passObstacleRadius = 500;

	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<Optional<OngoingPass>> ongoingPass;
	@Getter
	private Map<EOffensiveActionType, List<IObstacle>> primaryPlannedObstacles = Map.of();
	@Getter
	private Map<EOffensiveActionType, List<IObstacle>> allPlannedPassObstacles = Map.of();
	@Getter
	private IObstacle ongoingPassObstacle = null;


	@Override
	protected void doCalc()
	{
		var byBot = offensiveActions.get().entrySet().stream()
				.filter(entry -> desiredBots.get().getOrDefault(EPlay.OFFENSIVE, Set.of()).contains(entry.getKey()))
				.map(entry -> createPlannedObstacles(entry.getKey(), entry.getValue()))
				.flatMap(Optional::stream)
				.toList();

		primaryPlannedObstacles = reorderByBotToByActionType(byBot, true);
		allPlannedPassObstacles = reorderByBotToByActionType(byBot, false);
		ongoingPassObstacle = ongoingPass.get().map(this::buildOngoingPassObstacle).orElse(null);

		var shapes = getShapes(EAiShapesLayer.OFFENSE_PASS_OBSTACLES);
		allPlannedPassObstacles.values().stream()
				.flatMap(List::stream)
				.map(IObstacle::getShapes)
				.flatMap(List::stream)
				.map(shape -> shape.setColor(getAiFrame().getTeamColor().getColor()))
				.forEach(shapes::add);
		Optional.ofNullable(ongoingPassObstacle)
				.stream()
				.map(IObstacle::getShapes)
				.flatMap(List::stream)
				.map(shape -> shape.setColor(getAiFrame().getTeamColor().getColor().darker().darker()))
				.forEach(shapes::add);
	}


	private Optional<ObstaclesData> createPlannedObstacles(BotID botID, RatedOffensiveAction ratedOffensiveAction)
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
		var trajectory = fac.createTrajectoryFromKickedBallWithoutSpin(
				kick.getSource(),
				kick.getKickVel().multiplyNew(1000)
		);
		var tStart = getBall().getTrajectory().getTimeByPos(kick.getSource());
		var passObstacle = new SimpleTimeAwareBallObstacle(
				trajectory,
				passObstacleRadius + Geometry.getBallRadius(), tStart, 1
		);
		var type = offensiveAction.getType();
		if (tStart == 0)
		{
			return Optional.of(new ObstaclesData(botID, type, tStart, List.of(passObstacle)));
		} else
		{
			return Optional.of(new ObstaclesData(
					botID, type, tStart, List.of(
					new SimpleTimeAwareBallObstacle(
							getBall().getTrajectory(),
							passObstacleRadius + Geometry.getBallRadius(), 0, tStart
					),
					passObstacle
			)
			));
		}
	}


	private Map<EOffensiveActionType, List<IObstacle>> reorderByBotToByActionType(
			List<ObstaclesData> data,
			boolean filterOnlyFirst
	)
	{
		return Arrays.stream(EOffensiveActionType.values())
				.map(key -> extractOfType(data, key, filterOnlyFirst))
				.flatMap(Optional::stream)
				.collect(Collectors.toUnmodifiableMap(ObstaclesData::type, ObstaclesData::obstacles));
	}


	private Optional<ObstaclesData> extractOfType(
			List<ObstaclesData> data, EOffensiveActionType type,
			boolean filterOnlyFirst
	)
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


	private IObstacle buildOngoingPassObstacle(OngoingPass ongoingPass)
	{
		return new TubeObstacle(
				"Ongoing Pass", Tube.create(
				getBall().getPos(),
				ongoingPass.getPass().getKick().getTarget(),
				2 * Geometry.getBotRadius() + Geometry.getBallRadius()
		)
		);
	}


	private record ObstaclesData(BotID botID, EOffensiveActionType type, double tStart, List<IObstacle> obstacles)
	{
	}
}
