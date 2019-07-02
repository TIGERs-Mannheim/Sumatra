/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.action.situation;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.targetrater.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AOffensiveState;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMapConst;
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

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Estimates how good a given Situation is, in regard to the offensive strategy
 */
public class OffensiveSituationRatingCalc extends ACalculator
{
	
	private OffensiveActionTreePath currentPath = new OffensiveActionTreePath();
	
	private EBallPossession oldBallPoss = EBallPossession.NO_ONE;
	private EOffensiveExecutionStatus oldStatus = EOffensiveExecutionStatus.GETTING_READY;
	
	private OffensiveActionTreeMap treeMap = new OffensiveActionTreeMap();
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	@Configurable(defValue = "false")
	private static boolean drawValuedField = false;
	
	static
	{
		ConfigRegistration.registerClass("metis", OffensiveSituationRatingCalc.class);
	}
	
	
	public OffensiveSituationRatingCalc()
	{
		// nothing
	}
	

	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final OffensiveTreeProvider treeProvider = SumatraModel.getInstance().getModuleOpt(OffensiveTreeProvider.class).orElse(null);
		if (treeProvider == null)
		{
			getNewTacticalField().setActionTrees(new OffensiveActionTreeMap());
			getNewTacticalField().setCurrentPath(new OffensiveActionTreePath());
			return;
		}
		treeMap = treeProvider.getTreeMap(getWFrame().getTeamColor());
		storeTacticalFieldInformation();
		EBallPossession ballPoss = newTacticalField.getBallPossession().getEBallPossession();
		EOffensiveSituation currentSituation = determineCurrentSituation(ballPoss);
		getNewTacticalField().setCurrentSituation(currentSituation);
		
		if ((oldBallPoss != EBallPossession.WE) && (ballPoss == EBallPossession.WE))
		{
			// trigger Start of tracking
			currentPath.clear();
			oldStatus = EOffensiveExecutionStatus.GETTING_READY;
			oldBallPoss = ballPoss;
		} else if ((ballPoss != EBallPossession.WE) && (oldBallPoss == EBallPossession.WE))
		{
			oldBallPoss = ballPoss;
			// threw away back prop if tree was updated
			if (!currentPath.isEmpty())
			{
				// back propagate tree here
				currentPath = filterCurrentPath(currentPath);
				treeMap.getActionTrees().get(currentSituation)
						.updateTree(currentPath.getCurrentPath().stream().map(Enum::toString).collect(Collectors.toList()),
								currentPath.getCurrentScores());
				currentPath.clear();
			}
			storeTacticalFieldInformation();
			return;
		} else if (ballPoss != EBallPossession.WE)
		{
			// nothing to do if we dont have ball possession
			oldBallPoss = ballPoss;
			return;
		}
		
		storeTacticalFieldInformation();
		
		if (!newTacticalField.getOffensiveStrategy().getAttackerBot().isPresent())
		{
			return;
		}
		
		BotID attackerBot = newTacticalField.getOffensiveStrategy().getAttackerBot()
				.orElseThrow(IllegalStateException::new);
		
		Optional<EOffensiveActionMove> moveType = getAiFrame().getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(r -> (AttackerRole) r)
				.map(r -> ((AOffensiveState) r.getCurrentState()).getCurrentOffensiveActionMove())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findAny();
		
		moveType.ifPresent(this::addNewEntryToPathAndScore);
		
		double via = newTacticalField.getOffensiveActions().get(attackerBot).getViability();
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_SITUATION);
		shapes.add(getDrawableText(oldStatus.toString(), 0));
		shapes.add(getDrawableText(decimalFormat.format(via), 15));
		moveType.ifPresent(eOffensiveActionMove -> shapes.add(getDrawableText(eOffensiveActionMove.toString(), 30)));
		shapes.add(getDrawableText(Arrays.toString(currentPath.getCurrentPath().toArray()), 45));
		shapes.add(getDrawableText(currentSituation.toString(), 60));
		shapes.add(getDrawableText("score:" + calcScore(getWFrame().getBall().getPos()), 75));
		
		if (drawValuedField)
		{
			drawValuedField();
		}
	}
	
	
	private void storeTacticalFieldInformation()
	{
		getNewTacticalField().setActionTrees(treeMap);
		getNewTacticalField().setCurrentPath(new OffensiveActionTreePath(currentPath));
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
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_SITUATION).add(field);
	}
	
	
	private void addNewEntryToPathAndScore(final EOffensiveActionMove moveType)
	{
		EOffensiveExecutionStatus status = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(r -> (AttackerRole) r)
				.map(AttackerRole::getExecutionStatus)
				.findAny().orElse(EOffensiveExecutionStatus.GETTING_READY);
		if ((oldStatus == EOffensiveExecutionStatus.GETTING_READY) && (status == EOffensiveExecutionStatus.IMMINENT))
		{
			currentPath.addEntry(moveType, calcScore(getWFrame().getBall().getPos()));
		}
		oldStatus = status;
	}
	
	
	private EOffensiveSituation determineCurrentSituation(final EBallPossession ballPoss)
	{
		EOffensiveSituation situation = calcCurrentSituation();
		if (((ballPoss != EBallPossession.WE) && currentPath.isEmpty())
				|| (situation == EOffensiveSituation.STANDARD_DEFENSIVE)
				|| (situation == EOffensiveSituation.STANDARD_AGGRESSIVE))
		{
			return situation;
		}
		// return old situation
		return getAiFrame().getPrevFrame().getTacticalField().getCurrentSituation();
	}
	
	
	private DrawableBorderText getDrawableText(final String text, final int offset)
	{
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 105.0 + offset), text, Color.BLUE);
		dt.setFontSize(12);
		return dt;
	}
	
	
	/**
	 * Current path and current Score should be filtered for invalid states
	 */
	private OffensiveActionTreePath filterCurrentPath(final OffensiveActionTreePath currentPath)
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
		return currentPath;
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
		if (getNewTacticalField().getSkirmishInformation().isSkirmishDetected())
		{
			if (getBall().getPos().x() < 0)
			{
				return EOffensiveSituation.CLOSE_COMBAT_DEFENSIVE;
			}
			return EOffensiveSituation.CLOSE_COMBAT_AGGRESSIVE;
		} else if (getAiFrame().getGamestate().isStandardSituationForUs())
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
		Optional<IRatedTarget> oTarget = getNewTacticalField().getBestGoalKickTarget();
		double targetScore = 0;
		if (oTarget.isPresent())
		{
			targetScore = oTarget.get().getScore();
		}
		return (targetScore + (slackScore * 2.0)) / 3.0;
	}
	
	
	private double getSlackScore(final IVector2 point)
	{
		// rate the current Situation
		// slack time and directShotScore
		double slackTime = calcSlackTime(point, getWFrame().getFoeBots(), getWFrame().getTigerBotsVisible());
		double slackScore = Math.max(-1, slackTime); // negative slackTimes are bad anyway
		slackScore = 1 + (Math.min(3, slackScore) / 4.0); // everything bigger than 3 seconds is perfect.
		return slackScore;
	}
	
	
	private double calcScore(final WorldFrame worldFrame, final IVector2 point)
	{
		double slackScore = getSlackScore(point);
		List<ITrackedBot> foeBots = new ArrayList<>(worldFrame.getFoeBots().values());
		ValuePoint bestTargetFromBot = BestDirectShotBallPossessingBot
				.getBestShot(Geometry.getGoalTheir(), point, foeBots)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0));
		return ((slackScore * 2.0) + bestTargetFromBot.getValue()) / 3.0;
	}
	
	
	private double calcSlackTime(final IVector2 target, final BotIDMapConst<ITrackedBot> foes,
			final BotIDMapConst<ITrackedBot> tigers)
	{
		double minFoeArrivalTime = foes.values().stream()
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
		return minFoeArrivalTime - minTigerArrivalTime;
	}
}
