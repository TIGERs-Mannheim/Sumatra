/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBallThreatSourceType;
import edu.tigers.sumatra.ai.metis.defense.data.EDefensePassDisruptionStrategyType;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class DefensePassDisruptionCalc extends ACalculator
{
	@Configurable(defValue = "false", comment = "Draw more shapes stored in an extra layer")
	private static boolean drawDebugShapes = false;
	@Configurable(defValue = "0.8", comment = "[m/s] if our robot is slower than this on the disturbance pos it is allowed to risk a collision")
	private static double maxVelToRiskCollision = 0.8;
	@Configurable(defValue = "1.3", comment = "The factor multiplied to the maximum time our offense has to reach it's intercept point")
	private static double offenseTimeFactor = 1.3;
	@Configurable(defValue = "1.3", comment = "[s] A constant offset added to the maximum time our offense has to reach it's intercept point")
	private static double offenseTimeOffset = 0.1;
	@Configurable(defValue = "0.3", comment = "[m/s] Tolerance the actual disruption point can have from the best one regarding the velocity")
	private static double toleranceVel = 0.3;
	@Configurable(defValue = "10.0", comment = "[mm] Tolerance the actual disruption point can have from the best one regarding the distance")
	private static double toleranceDist = 10.0;

	private final Supplier<Set<BotID>> crucialOffender;
	private final Supplier<DefenseBallThreat> ballThreat;
	private final Supplier<Optional<OngoingPass>> ongoingPass;

	@Getter
	private DefensePassDisruptionAssignment currentAssignment = null;
	private BotID opponentPassReceiver;
	private DisruptionData lastData = null;
	private EOffenseOpponentConstellation lastConstellation;


	@Override
	protected void reset()
	{
		lastData = null;
		currentAssignment = null;
		lastConstellation = EOffenseOpponentConstellation.OPPONENT_IS_FIRST_OR_CLOSE;
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isGameRunning()
				&& ballThreat.get().getSourceType() == EDefenseBallThreatSourceType.PASS_RECEIVE
				&& ongoingPass.get().isEmpty();
	}


	@Override
	protected void doCalc()
	{
		opponentPassReceiver = ballThreat.get().getPassReceiver().orElseThrow().getBotId();
		var offense = crucialOffender.get().stream()
				.map(this::createOffenseTimePosDist)
				.flatMap(Optional::stream)
				.min(Comparator.comparingDouble(BallTimePosDist::time))
				.orElse(null);
		var opponent = createOpponentTimePosDist();
		var newConstellation = determineOffenseOpponentConstellation(lastConstellation, offense, opponent);

		DisruptionData newData;

		if (lastData != null)
		{
			newData = updateDisruptionData(lastData, opponent)
					.orElseGet(() -> findNewDisruptionData(offense, opponent, newConstellation).orElse(null));
		} else
		{
			newData = findNewDisruptionData(offense, opponent, newConstellation).orElse(null);
		}

		if (newData != null)
		{
			currentAssignment = newData.buildAssignment(newConstellation);
			getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).addAll(newData.draw(true).toList());
		} else
		{
			currentAssignment = null;
		}

		getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION)
				.add(new DrawableAnnotation(ballThreat.get().getPos(), newConstellation.toString()));
		if (offense != null)
		{
			getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).addAll(offense.draw(Color.CYAN).toList());
		}
		getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).addAll(opponent.draw(Color.ORANGE).toList());

		lastConstellation = newConstellation;
		lastData = newData;
	}


	private Optional<BallTimePosDist> createOffenseTimePosDist(BotID botID)
	{
		var bot = getWFrame().getBot(botID);
		if (bot == null)
		{
			return Optional.empty();
		}
		var ratedAction = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(botID);
		if (ratedAction == null)
		{
			return Optional.empty();
		}
		var action = ratedAction.getAction();
		if (action == null)
		{
			return Optional.empty();
		}
		var ballContactPos = action.getBallContactPos();
		if (ballContactPos == null)
		{
			return Optional.empty();
		}
		var timePosDist = BallTimePosDist.fromPos(getBall(), ballContactPos);
		var robotTime = TrajectoryGenerator.generatePositionTrajectory(bot, ballContactPos).getTotalTime();

		if (robotTime > timePosDist.time * offenseTimeFactor + offenseTimeOffset)
		{
			return Optional.empty();
		}
		return Optional.of(timePosDist);
	}


	private BallTimePosDist createOpponentTimePosDist()
	{
		var pos = ballThreat.get().getPos();
		var penArea = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius());
		if (penArea.isPointInShapeOrBehind(pos))
		{
			pos = pos.nearestToOpt(penArea.intersectPerimeterPath(getBall().getTrajectory().getTravelLine())).orElse(pos);
		}
		return BallTimePosDist.fromPos(getBall(), pos);
	}


	private Optional<DisruptionData> updateDisruptionData(DisruptionData oldData, BallTimePosDist opponent)
	{
		if (oldData.opponentReceiver != opponentPassReceiver)
		{
			// Pass receiver changed
			return Optional.empty();
		}

		var defender = oldData.strategy.defender.getBotId();
		if (getAiFrame().getKeeperId() == defender || crucialOffender.get().contains(defender))
		{
			return Optional.empty();
		}

		var newStrategy = oldData.strategy.update(getWFrame(), opponent);

		return newStrategy
				.filter(strategy -> strategy.disruption.dist <= opponent.dist)
				.map(strategy -> DisruptionData.fromStrategy(oldData.opponentReceiver, strategy))
				.filter(data -> data.feasibility.category != EFeasibilityCategory.UNFEASIBLE);
	}


	private Optional<DisruptionData> findNewDisruptionData(
			BallTimePosDist offense,
			BallTimePosDist opponent,
			EOffenseOpponentConstellation constellation
	)
	{
		var disruptionData = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.filter(bot -> !crucialOffender.get().contains(bot.getBotId()))
				.map(bot -> creatDisruptionDataFromDefender(bot, offense, opponent, constellation))
				.flatMap(List::stream)
				.toList();

		if (drawDebugShapes)
		{
			getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION_DEBUG).addAll(
					disruptionData.stream().flatMap(data -> data.draw(false)).toList()
			);
		}

		var bestFully = selectBestFromCategory(disruptionData, EFeasibilityCategory.FULLY, toleranceVel);
		if (bestFully.isPresent())
		{
			return bestFully;
		}
		return selectBestFromCategory(disruptionData, EFeasibilityCategory.PARTIAL, toleranceDist);
	}


	private Optional<DisruptionData> selectBestFromCategory(
			List<DisruptionData> disruptionData,
			EFeasibilityCategory wantedCategory,
			double scoreTolerance
	)
	{
		var filtered = disruptionData.stream()
				.filter(data -> data.feasibility.category == wantedCategory)
				.toList();
		if (filtered.isEmpty())
		{
			return Optional.empty();
		}
		var minScore = filtered.stream()
				.mapToDouble(data -> data.feasibility.scoreToMinimize)
				.min()
				.orElseThrow();
		return filtered.stream()
				.filter(data -> SumatraMath.isEqual(data.feasibility.scoreToMinimize, minScore, scoreTolerance))
				.min(Comparator.comparingDouble(data -> data.strategy.disruption.time));
	}


	private List<DisruptionData> creatDisruptionDataFromDefender(
			ITrackedBot defender,
			BallTimePosDist offense,
			BallTimePosDist opponent,
			EOffenseOpponentConstellation constellation
	)
	{
		return createDisruptionStrategies(defender, offense, opponent, constellation).stream()
				.map(strategy -> DisruptionData.fromStrategy(opponentPassReceiver, strategy))
				.toList();
	}


	private List<DisruptionStrategy> createDisruptionStrategies(
			ITrackedBot defender,
			BallTimePosDist offense,
			BallTimePosDist opponent,
			EOffenseOpponentConstellation constellation
	)
	{
		return switch (constellation)
		{
			case OUR_OFFENSE_IS_FIRST_WITH_MARGIN ->
					createDefStrategiesForDistanceRange(defender, offense.dist + 4 * Geometry.getBotRadius(), opponent);
			case OUR_OFFENSE_IS_FIRST -> List.of(DisruptionStrategy.opponentDisturbance(getWFrame(), defender, opponent));
			case OPPONENT_IS_FIRST_OR_CLOSE -> createDefStrategiesForDistanceRange(defender, 0, opponent);
		};
	}


	private List<DisruptionStrategy> createDefStrategiesForDistanceRange(
			ITrackedBot defender,
			double distMin,
			BallTimePosDist opponent
	)
	{
		if (distMin >= opponent.dist)
		{
			return List.of();
		}
		var strategies = new ArrayList<DisruptionStrategy>();
		strategies.add(DisruptionStrategy.opponentDisturbance(getWFrame(), defender, opponent));
		var dist = distMin;
		var penArea = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius());
		while (dist < opponent.dist - Geometry.getBotRadius())
		{
			var newStrategy = DisruptionStrategy.ballIntercept(
					getWFrame(), defender,
					BallTimePosDist.fromDist(getBall(), dist)
			);

			var ballHeight = getBall().getTrajectory().getPosByTime(newStrategy.disruption.time).z();
			if (!penArea.isPointInShapeOrBehind(newStrategy.disruption.pos)
					&& ballHeight <= newStrategy.defender.getRobotInfo().getBotParams().getDimensions().getHeight())
			{
				strategies.add(newStrategy);
			}
			dist += Geometry.getBotRadius();
		}
		return Collections.unmodifiableList(strategies);
	}


	private EOffenseOpponentConstellation determineOffenseOpponentConstellation(
			EOffenseOpponentConstellation constellationPrior,
			BallTimePosDist offense,
			BallTimePosDist opponent
	)
	{
		if (offense == null)
		{
			return EOffenseOpponentConstellation.OPPONENT_IS_FIRST_OR_CLOSE;
		}

		var closeMargin = constellationPrior == EOffenseOpponentConstellation.OPPONENT_IS_FIRST_OR_CLOSE
				? 4 * Geometry.getBotRadius()
				: 2 * Geometry.getBotRadius();
		if (offense.dist + closeMargin > opponent.dist)
		{
			return EOffenseOpponentConstellation.OPPONENT_IS_FIRST_OR_CLOSE;
		}

		var wellInFrontMargin = constellationPrior == EOffenseOpponentConstellation.OUR_OFFENSE_IS_FIRST_WITH_MARGIN
				? 7 * Geometry.getBotRadius()
				: 10 * Geometry.getBotRadius();

		if (offense.dist + wellInFrontMargin < opponent.dist)
		{
			return EOffenseOpponentConstellation.OUR_OFFENSE_IS_FIRST_WITH_MARGIN;
		}
		return EOffenseOpponentConstellation.OUR_OFFENSE_IS_FIRST;
	}


	private enum EOffenseOpponentConstellation
	{
		OUR_OFFENSE_IS_FIRST_WITH_MARGIN,
		OUR_OFFENSE_IS_FIRST,
		OPPONENT_IS_FIRST_OR_CLOSE
	}

	private enum EFeasibilityCategory
	{
		UNFEASIBLE,
		PARTIAL,
		FULLY
	}


	private record BallTimePosDist(double time, IVector2 pos, double dist)
	{

		static BallTimePosDist fromTime(ITrackedBall ball, double time)
		{
			var pos = ball.getTrajectory().getPosByTime(time).getXYVector();
			var dist = ball.getPos().distanceTo(pos);
			return new BallTimePosDist(time, pos, dist);
		}


		static BallTimePosDist fromPos(ITrackedBall ball, IVector2 pos)
		{
			var dist = ball.getPos().distanceTo(pos);
			return fromDist(ball, dist);
		}


		static BallTimePosDist fromDist(ITrackedBall ball, double dist)
		{
			var time = ball.getTrajectory().getTimeByDist(dist);
			var pos = ball.getTrajectory().getPosByTime(time).getXYVector();
			return new BallTimePosDist(time, pos, dist);
		}


		Stream<IDrawableShape> draw(Color color)
		{
			return Stream.of(
					new DrawablePoint(pos, color),
					new DrawableAnnotation(pos, String.format("%.2f s%n%.2f mm", time, dist), color)
			);
		}

	}

	private record DisruptionStrategy(
			BallTimePosDist disruption,
			ITrackedBot defender,
			ITrajectory<IVector2> trajectory,
			EDefensePassDisruptionStrategyType type,
			long creationTimestamp
	)
	{

		static DisruptionStrategy opponentDisturbance(
				WorldFrame wFrame,
				ITrackedBot defenderBot,
				BallTimePosDist ballTimePosDist
		)
		{
			return new DisruptionStrategy(
					ballTimePosDist,
					defenderBot,
					TrajectoryGenerator.generatePositionTrajectory(defenderBot, ballTimePosDist.pos),
					EDefensePassDisruptionStrategyType.DISRUPT_OPPONENT_RECEIVER,
					wFrame.getTimestamp()
			);
		}


		static DisruptionStrategy ballIntercept(
				WorldFrame wFrame,
				ITrackedBot defenderBot,
				BallTimePosDist ballTimePosDist
		)
		{
			return new DisruptionStrategy(
					ballTimePosDist,
					defenderBot,
					TrajectoryGenerator.generatePositionTrajectoryToReachPointInTime(
							defenderBot, ballTimePosDist.pos,
							ballTimePosDist.time
					),
					EDefensePassDisruptionStrategyType.DISRUPT_PASS,
					wFrame.getTimestamp()
			);
		}


		Optional<DisruptionStrategy> update(WorldFrame wFrame, BallTimePosDist opponent)
		{
			var timePassed = (wFrame.getTimestamp() - creationTimestamp) * 1e-9;

			var newDefender = wFrame.getBot(defender.getBotId());
			if (newDefender == null)
			{
				return Optional.empty();
			}

			return Optional.of(switch (type)
			{
				case DISRUPT_OPPONENT_RECEIVER -> DisruptionStrategy.opponentDisturbance(wFrame, newDefender, opponent);
				case DISRUPT_PASS -> DisruptionStrategy.ballIntercept(
						wFrame, newDefender,
						BallTimePosDist.fromTime(wFrame.getBall(), disruption.time - timePassed)
				);
			});
		}


		Feasibility determineFeasibility()
		{
			double maxDistForFully;
			double maxVelForFully;

			if (type == EDefensePassDisruptionStrategyType.DISRUPT_PASS)
			{
				maxDistForFully = 0.1 * Geometry.getBotRadius();
				maxVelForFully = 0.5;
			} else
			{
				maxDistForFully = 0.5 * Geometry.getBotRadius();
				maxVelForFully = 1.0;
			}


			var distAtIntercept = distAtIntercept();
			if (distAtIntercept > Geometry.getBotRadius())
			{
				return new Feasibility(EFeasibilityCategory.UNFEASIBLE, 0);
			}
			var velAtIntercept = velAtIntercept();
			if (distAtIntercept < maxDistForFully && velAtIntercept < maxVelForFully)
			{
				return new Feasibility(EFeasibilityCategory.FULLY, velAtIntercept);
			}
			return new Feasibility(EFeasibilityCategory.PARTIAL, distAtIntercept);
		}


		double distAtIntercept()
		{
			return trajectory.getPositionMM(disruption.time).distanceTo(disruption.pos);
		}


		double velAtIntercept()
		{
			return trajectory.getVelocity(disruption.time).getLength();
		}


		Stream<IDrawableShape> draw(Color color)
		{
			return Stream.concat(
					disruption.draw(color),
					Stream.of(
							new DrawableAnnotation(
									disruption.pos.addNew(Vector2.fromY(135)),
									String.format(
											"%s%n%.2f s | %.2f mm | %.2f m/s", type, trajectory.getTotalTime(),
											distAtIntercept(), velAtIntercept()
									), color
							)
					)
			);
		}
	}

	private record Feasibility(EFeasibilityCategory category, double scoreToMinimize)
	{
	}

	private record DisruptionData(
			BotID opponentReceiver,
			DisruptionStrategy strategy,
			Feasibility feasibility
	)
	{
		static DisruptionData fromStrategy(BotID opponentReceiver, DisruptionStrategy strategy)
		{
			return new DisruptionData(
					opponentReceiver,
					strategy,
					strategy.determineFeasibility()
			);
		}


		Stream<IDrawableShape> draw(boolean highlighted)
		{

			var pos = strategy.disruption.pos;
			if (highlighted)
			{
				var color = switch (feasibility.category)
				{
					case UNFEASIBLE -> Color.RED;
					case PARTIAL -> Color.YELLOW;
					case FULLY -> Color.GREEN;
				};
				return Stream.concat(
						Stream.of(new DrawableLine(strategy.defender.getPos(), pos, color).setStrokeWidth(10)),
						strategy.draw(color)
				);
			}
			var color = switch (feasibility.category)
			{
				case UNFEASIBLE -> Color.BLACK;
				case PARTIAL -> Color.GRAY;
				case FULLY -> Color.WHITE;
			};
			return Stream.of(new DrawableLine(strategy.defender.getPos(), pos, color).setStrokeWidth(3));
		}


		DefensePassDisruptionAssignment buildAssignment(EOffenseOpponentConstellation constellation)
		{
			return new DefensePassDisruptionAssignment(
					opponentReceiver,
					strategy.defender.getBotId(),
					strategy.disruption.pos,
					strategy.trajectory.getFinalDestination(),
					constellation != EOffenseOpponentConstellation.OUR_OFFENSE_IS_FIRST_WITH_MARGIN,
					strategy.trajectory.getVelocity(strategy.disruption.time).getLength() < maxVelToRiskCollision,
					strategy.type
			);
		}
	}
}
