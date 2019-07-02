/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.moves.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates offensive Actions for the OffenseRole.
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveActionsCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(OffensiveActionsCalc.class.getName());
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	private static final Color COLOR = Color.magenta;

	private EnumMap<EOffensiveActionMove, AOffensiveActionMove> actionMoves = new EnumMap<>(EOffensiveActionMove.class);


	static
	{
		for (EOffensiveActionMove actionMove : EOffensiveActionMove.values())
		{
			ConfigRegistration.registerClass("metis", actionMove.getInstanceableClass().getImpl());
		}
	}


	/**
	 * @author MarkG
	 */
	public OffensiveActionsCalc()
	{
		for (EOffensiveActionMove key : EOffensiveActionMove.values())
		{
			try
			{
				AOffensiveActionMove actionMove = (AOffensiveActionMove) key.getInstanceableClass().getConstructor()
						.newInstance();
				actionMoves.put(key, actionMove);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				log.error("Could not create offensive action move", e);
			}
		}
	}


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Map<String, Double> adjustedScores = calcAdjustedOffensiveViabilityScores(newTacticalField);
		Map<BotID, OffensiveAction> offensiveActions = new HashMap<>();
		IBotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		botMap.putAll(baseAiFrame.getWorldFrame().getTigerBotsAvailable());
		if (!newTacticalField.isInsaneKeeper())
		{
			botMap.remove(baseAiFrame.getKeeperId());
		}
		for (Map.Entry<BotID, ITrackedBot> entry : botMap)
		{
			BotID botID = entry.getKey();
			ITrackedBot tBot = entry.getValue();
			Map<EOffensiveActionMove, EActionViability> viabilities = new EnumMap<>(EOffensiveActionMove.class);
			Map<EOffensiveActionMove, Double> viabilityScores = new EnumMap<>(EOffensiveActionMove.class);

			OffensiveAction action = calcOffensiveAction(botID, newTacticalField, viabilities, viabilityScores,
					adjustedScores);
			if (action == null)
			{
				continue;
			}

			offensiveActions.put(botID, action);

			if (OffensiveConstants.isEnableOffensiveStatistics())
			{
				OffensiveStatisticsFrame sFrame = newTacticalField.getOffensiveStatistics();
				sFrame.getBotFrames().get(botID).setActiveAction(action.getAction());
				sFrame.getBotFrames().get(botID).setMoveViabilities(viabilities);
				sFrame.getBotFrames().get(botID).setMoveViabilityScores(viabilityScores);
			}

			drawAction(newTacticalField.getDrawableShapes(), tBot, action);
		}

		newTacticalField.setOffensiveActions(offensiveActions);
	}


	private void drawAction(final ShapeMap shapes, final ITrackedBot tBot, final OffensiveAction action)
	{
		action.getRatedPassTarget().ifPresent(passTarget -> shapes.get(EAiShapesLayer.OFFENSIVE_ACTION)
				.add(new DrawableCircle(Circle.createCircle(passTarget.getPos(), 80), COLOR)
						.withFill(true)));

		shapes.get(EAiShapesLayer.OFFENSIVE_ACTION)
				.add(new DrawableLine(Line.fromPoints(tBot.getBotKickerPos(), action.getKickTarget().getTarget().getPos()),
						COLOR));
		shapes.get(EAiShapesLayer.OFFENSIVE_ACTION)
				.add(new DrawableCircle(Circle.createCircle(action.getKickTarget().getTarget().getPos(), 40),
						COLOR.brighter())
						.withFill(true));

		final String actionMetadata = action.getMove().name() + "\n" +
				action.getAction() + "\n" +
				"viability: " + DF.format(action.getViability()) + "\n" +
				(action.isAllowRedirect() ? "redirect allowed" : "redirect forbidden");
		shapes.get(EAiShapesLayer.OFFENSIVE_ACTION_DEBUG).add(
				new DrawableAnnotation(tBot.getPos(), actionMetadata, COLOR)
						.withOffset(Vector2f.fromX(150)));

		final String actionTargetMetadata = action.getKickTarget().getChipPolicy().name()
				+ "\nballSpeed: " + DF.format(action.getKickTarget().getBallSpeedAtTarget());
		shapes.get(EAiShapesLayer.OFFENSIVE_ACTION_DEBUG).add(
				new DrawableAnnotation(action.getKickTarget().getTarget().getPos(), actionTargetMetadata, COLOR)
						.withOffset(Vector2f.fromY(150))
						.withCenterHorizontally(true));
	}


	private Map<String, Double> calcAdjustedOffensiveViabilityScores(final TacticalField newTacticalField)
	{
		Optional<Map<String, Double>> adjustedScores = Optional.empty();
		if (getAiFrame().getPrevFrame() != null
				&& getAiFrame().getPrevFrame().getTacticalField().getActionTrees() != null)
		{
			adjustedScores = getAdjustedScores(newTacticalField);
		}
		if (!adjustedScores.isPresent())
		{
			Map<String, Double> adjustedScoresRaw = new HashMap<>();
			for (EOffensiveActionMove key : EOffensiveActionMove.values())
			{
				adjustedScoresRaw.put(key.toString(), 1.0);
			}
			return adjustedScoresRaw;
		}
		return adjustedScores.get();
	}


	private Optional<Map<String, Double>> getAdjustedScores(final TacticalField newTacticalField)
	{
		final Map<String, Double> adjustedScores;
		EOffensiveActionMove[] currentPath = new EOffensiveActionMove[newTacticalField.getCurrentPath()
				.getCurrentPath().size()];
		currentPath = getAiFrame().getPrevFrame().getTacticalField().getCurrentPath().getCurrentPath()
				.toArray(currentPath);
		Map<EOffensiveSituation, OffensiveActionTree> trees = getAiFrame().getPrevFrame().getTacticalField()
				.getActionTrees()
				.getActionTrees();

		if (trees.containsKey(newTacticalField.getCurrentSituation()))
		{
			adjustedScores = trees.get(newTacticalField.getCurrentSituation())
					.getAdjustedScoresForCurrentPath(
							Arrays.stream(currentPath).map(Enum::toString).toArray(String[]::new));
			for (EOffensiveActionMove key : EOffensiveActionMove.values())
			{
				if (!adjustedScores.containsKey(key.toString()))
				{
					adjustedScores.put(key.name(), 1.0);
				}
			}
			return Optional.of(adjustedScores);
		}
		return Optional.empty();
	}


	private OffensiveAction calcOffensiveAction(final BotID botID, final TacticalField newTacticalField,
			final Map<EOffensiveActionMove, EActionViability> viabilities,
			final Map<EOffensiveActionMove, Double> viabilityScores,
			final Map<String, Double> adjustedScores)
	{
		OffensiveAction action = parseActionMoveSet(actionMoves, botID, newTacticalField, viabilities, viabilityScores,
				adjustedScores);
		if (action != null)
		{
			// found standard action
			return action;
		}

		// what to do if no move is possible ??? -> desperate Shot
		log.warn("No offensive action has been declared... desperation level > 9000");
		return null;
	}


	private OffensiveAction parseActionMoveSet(Map<EOffensiveActionMove, AOffensiveActionMove> moves, final BotID botID,
			final TacticalField newTacticalField,
			final Map<EOffensiveActionMove, EActionViability> viabilities,
			final Map<EOffensiveActionMove, Double> viabilityScores,
			final Map<String, Double> adjustedScores)
	{
		List<AOffensiveActionMove> viableMoves = new LinkedList<>();
		List<AOffensiveActionMove> partiallyMoves = new LinkedList<>();

		for (Map.Entry<EOffensiveActionMove, AOffensiveActionMove> entry : moves.entrySet())
		{
			EActionViability viability = entry.getValue().isActionViable(botID, newTacticalField,
					getAiFrame());
			entry.getValue().calcViability(botID, newTacticalField, getAiFrame(),
					adjustedScores.get(entry.getKey().toString()));
			viabilities.put(entry.getKey(), viability);
			viabilityScores.put(entry.getKey(), entry.getValue().getViabilityScore());
			if (viability == EActionViability.TRUE)
			{
				viableMoves.add(entry.getValue());
			} else if (viability == EActionViability.PARTIALLY)
			{
				partiallyMoves.add(entry.getValue());
			}
		}

		// activate first move that got declared as viable
		if (!viableMoves.isEmpty())
		{
			AOffensiveActionMove move = viableMoves.get(0);
			return move.activateAction(botID, newTacticalField, getAiFrame());
		}

		// choose partially actionMoves.
		partiallyMoves.sort((e, e1) -> (int) Math.signum(e1.getViabilityScore() - e.getViabilityScore()));
		if (!partiallyMoves.isEmpty())
		{
			return partiallyMoves.get(0).activateAction(botID, newTacticalField, getAiFrame());
		}
		return null;
	}
}
