/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.action.situation;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.targetrater.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTreeMap;
import edu.tigers.sumatra.trees.OffensiveTreeProvider;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Estimates how good a given Situation is, in regard to the offensive strategy
 */
@RequiredArgsConstructor
public class OffensiveSituationRatingCalc extends ACalculator
{
	private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	@Configurable(defValue = "false")
	private static boolean drawValuedField = false;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveSituationRatingCalc.class);
	}

	private final Supplier<BallPossession> ballPossession;
	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<Map<BotID, OffensiveAction>> offensiveActions;
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTargets;

	@Getter
	private OffensiveActionTreePath actionTreePath;
	@Getter
	private OffensiveActionTreeMap actionTrees;
	@Getter
	private EOffensiveSituation currentSituation;

	private EBallPossession oldBallPoss = EBallPossession.NO_ONE;
	private EOffensiveExecutionStatus oldStatus = EOffensiveExecutionStatus.GETTING_READY;


	@Override
	protected boolean isCalculationNecessary()
	{
		return SumatraModel.getInstance().getModuleOpt(OffensiveTreeProvider.class).isPresent();
	}


	@Override
	protected void reset()
	{
		actionTrees = new OffensiveActionTreeMap();
		actionTreePath = new OffensiveActionTreePath();
		currentSituation = EOffensiveSituation.DEFAULT_SITUATION;
	}


	@Override
	public void doCalc()
	{
		var treeProvider = SumatraModel.getInstance().getModuleOpt(OffensiveTreeProvider.class).orElseThrow();

		actionTrees = treeProvider.getTreeMap(getWFrame().getTeamColor());
		EBallPossession ballPoss = ballPossession.get().getEBallPossession();
		currentSituation = determineCurrentSituation(ballPoss);

		if ((oldBallPoss != EBallPossession.WE) && (ballPoss == EBallPossession.WE))
		{
			// trigger Start of tracking
			actionTreePath.clear();
			oldStatus = EOffensiveExecutionStatus.GETTING_READY;
			oldBallPoss = ballPoss;
		} else if ((ballPoss != EBallPossession.WE) && (oldBallPoss == EBallPossession.WE))
		{
			oldBallPoss = ballPoss;
			// threw away back prop if tree was updated
			if (!actionTreePath.isEmpty())
			{
				// back propagate tree here
				filterCurrentPath(actionTreePath);
				actionTrees.getActionTrees().get(currentSituation)
						.updateTree(actionTreePath.getCurrentPath().stream().map(Enum::toString).collect(Collectors.toList()),
								actionTreePath.getCurrentScores());
				actionTreePath.clear();
			}
			return;
		} else if (ballPoss != EBallPossession.WE)
		{
			// nothing to do if we dont have ball possession
			oldBallPoss = ballPoss;
			return;
		}

		if (offensiveStrategy.get().getAttackerBot().isEmpty())
		{
			return;
		}

		BotID attackerBot = offensiveStrategy.get().getAttackerBot()
				.orElseThrow(IllegalStateException::new);

		var actionMove = offensiveActions.get().get(attackerBot);
		if (actionMove != null)
		{
			addNewEntryToPathAndScore(actionMove.getMove());

			double via = actionMove.getViability().getScore();
			List<IDrawableShape> shapes = getShapes(EAiShapesLayer.OFFENSIVE_SITUATION);
			shapes.add(getDrawableText(oldStatus.toString(), 0));
			shapes.add(getDrawableText(decimalFormat.format(via), 15));
			shapes.add(getDrawableText(actionMove.getMove().name(), 30));
			shapes.add(getDrawableText(Arrays.toString(actionTreePath.getCurrentPath().toArray()), 45));
			shapes.add(getDrawableText(currentSituation.toString(), 60));
			shapes.add(getDrawableText("score:" + calcScore(getWFrame().getBall().getPos()), 75));
		}

		if (drawValuedField)
		{
			drawValuedField();
		}
	}


	private void drawValuedField()
	{
		double width = Geometry.getFieldWidth();
		double height = Geometry.getFieldLength();

		int numX = 400;
		int numY = 200;

		List<Double> ratings = new ArrayList<>();

		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));

				ratings.add(calcScore(getWFrame(), Vector2.fromXY(x, y)));
			}
		}
		double[] ratingsArray = ratings.stream().mapToDouble(Double::doubleValue).toArray();
		ValuedField field = new ValuedField(ratingsArray, numX, numY, 0);
		getShapes(EAiShapesLayer.OFFENSIVE_SITUATION).add(field);
	}


	private void addNewEntryToPathAndScore(final EOffensiveActionMove moveType)
	{
		EOffensiveExecutionStatus status = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(AttackerRole.class::cast)
				.map(AttackerRole::getExecutionStatus)
				.findAny().orElse(EOffensiveExecutionStatus.GETTING_READY);
		if ((oldStatus == EOffensiveExecutionStatus.GETTING_READY) && (status == EOffensiveExecutionStatus.IMMINENT))
		{
			actionTreePath.addEntry(moveType, calcScore(getWFrame().getBall().getPos()));
		}
		oldStatus = status;
	}


	private EOffensiveSituation determineCurrentSituation(final EBallPossession ballPoss)
	{
		EOffensiveSituation situation = calcCurrentSituation();
		if (((ballPoss != EBallPossession.WE) && actionTreePath.isEmpty())
				|| (situation == EOffensiveSituation.STANDARD_DEFENSIVE)
				|| (situation == EOffensiveSituation.STANDARD_AGGRESSIVE))
		{
			return situation;
		}
		// return old situation
		return currentSituation;
	}


	private DrawableBorderText getDrawableText(final String text, final int offset)
	{
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 105.0 + offset), text, Color.BLUE);
		dt.setFontSize(EFontSize.LARGE);
		return dt;
	}


	/**
	 * Current path and current Score should be filtered for invalid states
	 */
	private void filterCurrentPath(final OffensiveActionTreePath currentPath)
	{
		List<EOffensiveActionMove> filteredMoves = new ArrayList<>();
		List<Double> filteredScores = new ArrayList<>();
		for (int i = 0; i < currentPath.getCurrentPath().size(); i++)
		{
			// multiple receives in a row should be filtered
			if (!filteringMultipleReceives(currentPath.getCurrentPath(), i))
			{
				// add current move to filtered move
				filteredMoves.add(currentPath.getCurrentPath().get(i));
				filteredScores.add(currentPath.getCurrentScores().get(i));

				// after shooting on the goal there can be no other strategy
				if (filterEndingActions(currentPath, i))
					break;
			}
		}
		currentPath.setCurrentPath(filteredMoves);
		currentPath.setCurrentScores(filteredScores);
	}


	private boolean filteringMultipleReceives(final List<EOffensiveActionMove> currentPath, final int i)
	{
		if (i > 0)
		{
			EOffensiveActionMove oldMove = currentPath.get(i - 1);
			EOffensiveActionMove newMove = currentPath.get(i);
			return oldMove == EOffensiveActionMove.RECEIVE_BALL && newMove == EOffensiveActionMove.RECEIVE_BALL;
		}
		return false;
	}


	private boolean filterEndingActions(final OffensiveActionTreePath currentPath, final int i)
	{
		return (currentPath.getCurrentPath().get(i) == EOffensiveActionMove.GOAL_KICK) ||
				(currentPath.getCurrentPath().get(i) == EOffensiveActionMove.REDIRECT_GOAL_KICK) ||
				(currentPath.getCurrentPath().get(i) == EOffensiveActionMove.LOW_CHANCE_GOAL_KICK);
	}


	/**
	 * Calculates current Situation
	 *
	 * @return the new current Situation
	 */
	private EOffensiveSituation calcCurrentSituation()
	{
		// calculate the current Situation
		if (skirmishInformation.get().getStrategy() != ESkirmishStrategy.NONE)
		{
			if (getBall().getPos().x() < 0)
			{
				return EOffensiveSituation.CLOSE_COMBAT_DEFENSIVE;
			}
			return EOffensiveSituation.CLOSE_COMBAT_AGGRESSIVE;
		} else if (getAiFrame().getGameState().isStandardSituationForUs())
		{
			if (getBall().getPos().x() < 0)
			{
				return EOffensiveSituation.STANDARD_DEFENSIVE;
			}
			return EOffensiveSituation.STANDARD_AGGRESSIVE;
		}
		return EOffensiveSituation.DEFAULT_SITUATION;
	}


	/**
	 * @return score for current Situation
	 */
	private Double calcScore(final IVector2 point)
	{
		double slackScore = getSlackScore(point);
		double targetScore = bestGoalKickTargets.get().values().stream()
				.map(GoalKick::getRatedTarget)
				.mapToDouble(IRatedTarget::getScore).max().orElse(0.0);
		return (targetScore + (slackScore * 2.0)) / 3.0;
	}


	private double getSlackScore(final IVector2 point)
	{
		// rate the current Situation
		// slack time and directShotScore
		double slackTime = calcSlackTime(point, getWFrame().getOpponentBots(), getWFrame().getTigerBotsVisible());
		double slackScore = Math.max(-1, slackTime); // negative slackTimes are bad anyway
		slackScore = 1 + (Math.min(3, slackScore) / 4.0); // everything bigger than 3 seconds is perfect.
		return slackScore;
	}


	private double calcScore(final WorldFrame worldFrame, final IVector2 point)
	{
		double slackScore = getSlackScore(point);
		List<ITrackedBot> opponentBots = new ArrayList<>(worldFrame.getOpponentBots().values());
		ValuePoint bestTargetFromBot = BestDirectShotBallPossessingBot
				.getBestShot(Geometry.getGoalTheir(), point, opponentBots)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0));
		return ((slackScore * 2.0) + bestTargetFromBot.getValue()) / 3.0;
	}


	private double calcSlackTime(final IVector2 target, final Map<BotID, ITrackedBot> opponents,
			final Map<BotID, ITrackedBot> tigers)
	{
		double minOpponentArrivalTime = opponents.values().stream()
				.mapToDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		double minTigerArrivalTime = 1000000;
		Optional<ITrackedBot> fastestTiger = tigers.values().stream()
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.min(Comparator
						.comparingDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime()));
		if (fastestTiger.isPresent())
		{
			minTigerArrivalTime = TrajectoryGenerator.generatePositionTrajectory(fastestTiger.get(), target)
					.getTotalTime();
		}
		return minOpponentArrivalTime - minTigerArrivalTime;
	}
}
