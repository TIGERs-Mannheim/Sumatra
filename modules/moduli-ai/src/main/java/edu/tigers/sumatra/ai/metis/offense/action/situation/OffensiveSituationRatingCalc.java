/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.situation;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
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
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Estimates how good a given Situation is, in regard to the offensive strategy
 */
public class OffensiveSituationRatingCalc extends ACalculator
{
	
	private OffensiveActionTreePath currentPath = new OffensiveActionTreePath();
	
	private EBallPossession oldBallPoss = EBallPossession.NO_ONE;
	private EOffensiveExecutionStatus oldStatus = EOffensiveExecutionStatus.GETTING_READY;
	
	// key is a Situation
	private Map<EOffensiveSituation, OffensiveActionTree> actionTrees = new EnumMap<>(EOffensiveSituation.class);
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	
	@Configurable(defValue = "false")
	private static boolean drawValuedField = false;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveSituationRatingCalc.class);
	}


	public OffensiveSituationRatingCalc()
	{
		for (EOffensiveSituation key : EOffensiveSituation.values())
		{
			actionTrees.put(key, new OffensiveActionTree());
		}
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setActionTrees(actionTrees);
		newTacticalField.setCurrentPath(currentPath);
		
		EBallPossession ballPoss = newTacticalField.getBallPossession().getEBallPossession();
		EOffensiveSituation currentSituation = determineCurrentSituation(ballPoss);
		
		if (oldBallPoss != EBallPossession.WE && ballPoss == EBallPossession.WE)
		{
			// trigger Start of tracking
			currentPath.clear();
			oldStatus = EOffensiveExecutionStatus.GETTING_READY;
			oldBallPoss = ballPoss;
		} else if (ballPoss != EBallPossession.WE && oldBallPoss == EBallPossession.WE)
		{
			oldBallPoss = ballPoss;
			if (!currentPath.isEmpty())
			{
				// back propagate tree here
				currentPath = filterCurrentPath(currentPath);
				actionTrees.get(currentSituation)
						.updateTree(currentPath.getCurrentPath(), currentPath.getCurrentScores());
				currentPath.clear();
			}
			return;
		} else if (ballPoss != EBallPossession.WE)
		{
			// nothing to do if we dont have ball possession
			oldBallPoss = ballPoss;
			return;
		}
		
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
				.map(r -> ((AOffensiveState) r.getCurrentState()).getCurrentOffensiveAction())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(OffensiveAction::getMove)
				.findAny();

		moveType.ifPresent(e -> addNewEntryToPathAndScore(newTacticalField, e));
		
		double via = newTacticalField.getOffensiveActions().get(attackerBot).getViability();
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_SITUATION);
		shapes.add(getDrawableText(oldStatus.toString(), 0));
		shapes.add(getDrawableText(decimalFormat.format(via), 15));
		moveType.ifPresent(eOffensiveActionMove -> shapes.add(getDrawableText(eOffensiveActionMove.toString(), 30)));
		shapes.add(getDrawableText(Arrays.toString(currentPath.getCurrentPath().toArray()), 45));
		shapes.add(getDrawableText(currentSituation.toString(), 60));
		shapes.add(getDrawableText("score:" + calcScore(newTacticalField, getWFrame().getBall().getPos()), 75));

		if (drawValuedField)
		{
			drawValuedField(newTacticalField);
		}
	}


	private void drawValuedField(final TacticalField newTacticalField)
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

		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_SITUATION).add(field);
	}
	
	
	private void addNewEntryToPathAndScore(final TacticalField newTacticalField,
			final EOffensiveActionMove moveType)
	{
		EOffensiveExecutionStatus status = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(r -> (AttackerRole) r)
				.map(AttackerRole::getExecutionStatus)
				.findAny().orElse(EOffensiveExecutionStatus.GETTING_READY);
		if (oldStatus == EOffensiveExecutionStatus.GETTING_READY && status == EOffensiveExecutionStatus.IMMINENT)
		{
			currentPath.addEntry(moveType, calcScore(newTacticalField, getWFrame().getBall().getPos()));
		}
		oldStatus = status;
	}
	
	
	private EOffensiveSituation determineCurrentSituation(final EBallPossession ballPoss)
	{
		if (ballPoss != EBallPossession.WE && currentPath.isEmpty())
		{
			return calcCurrentSituation();
		}
		if (getAiFrame().getPrevFrame() == null)
		{
			return EOffensiveSituation.THE_SITUATION;
		}
		// return old situation
		return getAiFrame().getPrevFrame().getTacticalField().getCurrentSituation();
	}
	
	
	private DrawableBorderText getDrawableText(String text, int offset)
	{
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 105.0 + offset), text, Color.BLUE);
		dt.setFontSize(12);
		return dt;
	}
	
	
	/**
	 * Current path and current Score should be filtered for invalid states
	 */
	private OffensiveActionTreePath filterCurrentPath(OffensiveActionTreePath currentPath)
	{
		List<EOffensiveActionMove> filteredMoves = new ArrayList<>();
		List<Double> filteredScores = new ArrayList<>();
		for (int i = 0; i < currentPath.getCurrentPath().size(); i++)
		{
			filteredMoves.add(currentPath.getCurrentPath().get(i));
			filteredScores.add(currentPath.getCurrentScores().get(i));
			
			// after shooting on the goal there can be no other strategy
			if (currentPath.getCurrentPath().get(i) == EOffensiveActionMove.GOAL_KICK ||
					currentPath.getCurrentPath().get(i) == EOffensiveActionMove.REDIRECT_GOAL_KICK ||
					currentPath.getCurrentPath().get(i) == EOffensiveActionMove.LOW_CHANCE_GOAL_KICK)
			{
				break;
			}
		}
		currentPath.setCurrentPath(filteredMoves);
		currentPath.setCurrentScores(filteredScores);
		return currentPath;
	}
	
	
	/**
	 * Calculates current Situation
	 * 
	 * @return the new current Situation
	 */
	private EOffensiveSituation calcCurrentSituation()
	{
		// calculate the current Situation
		return EOffensiveSituation.THE_SITUATION;
	}
	
	
	/**
	 * @param newTacticalField tactical field
	 * @return score for current Situation
	 */
	private Double calcScore(final TacticalField newTacticalField, IVector2 point)
	{
		double slackScore = getSlackScore(point);
		Optional<IRatedTarget> oTarget = newTacticalField.getBestGoalKickTarget();
		double targetScore = 0;
		if (oTarget.isPresent())
		{
			targetScore = oTarget.get().getScore();
		}
		return (targetScore + slackScore * 2.0) / 3.0;
	}


	private double getSlackScore(final IVector2 point)
	{
		// rate the current Situation
		// slack time and directShotScore
		double slackTime = calcSlackTime(point, getWFrame().getFoeBots(), getWFrame().getTigerBotsVisible());
		double slackScore = Math.max(-1, slackTime); // negative slackTimes are bad anyway
		slackScore = 1 + Math.min(3, slackScore) / 4.0; // everything bigger than 3 seconds is perfect.
		return slackScore;
	}


	private double calcScore(WorldFrame worldFrame, IVector2 point)
	{
		double slackScore = getSlackScore(point);
		List<ITrackedBot> foeBots = new ArrayList<>(worldFrame.getFoeBots().values());
		ValuePoint bestTargetFromBot = BestDirectShotBallPossessingBot
				.getBestShot(Geometry.getGoalTheir(), point, foeBots)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0));
		return (slackScore * 2.0 + bestTargetFromBot.getValue()) / 3.0;
	}


	private double calcSlackTime(IVector2 target, BotIDMapConst<ITrackedBot> foes,
			BotIDMapConst<ITrackedBot> tigers)
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
