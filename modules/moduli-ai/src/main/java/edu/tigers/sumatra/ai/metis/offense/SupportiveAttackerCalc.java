/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Activate the supportive attacker if required
 */
@RequiredArgsConstructor
public class SupportiveAttackerCalc extends ACalculator
{
	@Configurable(defValue = "100.0")
	private static double marginOpponentPenAreaToDeactivateSupportiveAttacker = 100.0;

	@Configurable(defValue = "2800.0", comment = "[mm]")
	private static double marginAroundOurPenAreaToConsiderOpponentFinisherDangerous = 2800.0;

	@Configurable(defValue = "400.0", comment = "[mm]")
	private static double opponentFinisherBlockerSelectionHystBot = 400.0;

	@Configurable(defValue = "200.0", comment = "[mm]")
	private static double opponentFinisherBlockerSelectionHystPos = 200.0;

	@Configurable(defValue = "0.4", comment = "[s]")
	private static double selectionHyst = 0.4;

	@Configurable(defValue = "true")
	private static boolean activateSupportiveInterceptor = true;

	@Configurable(comment = "enable supportive Attacker", defValue = "true")
	private static boolean enableSupportiveAttacker = true;

	@Configurable(comment = "enable opponent finisher blocker", defValue = "true")
	private static boolean enableOpponentFinisherBlocker = true;

	@Configurable(defValue = "1500.0")
	private static double marginAroundOurPenAreaToDeactivateSupportiveAttackers = 1500;

	private List<IVector2> supportiveOpponentFinisherBlockPositionsLastFrame = Collections.emptyList();

	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<RedirectorDetectionInformation> redirectorDetectionInformation;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<IVector2> supportiveAttackerPos;
	private final Supplier<Set<BotID>> bestBallDefenderCandidates;
	private final Supplier<IVector2> supportiveBlockerPos;
	private final Supplier<List<IVector2>> supportiveOpponentFinisherBlockPositions;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	@Getter
	private List<BotID> supportiveAttackers;

	@Getter
	private Map<BotID, IVector2> supportiveAttackerOpponentFinisherBlocker;


	@Override
	protected void reset()
	{
		supportiveAttackers = Collections.emptyList();
		supportiveAttackerOpponentFinisherBlocker = Collections.emptyMap();
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return enableSupportiveAttacker && getAiFrame().getGameState().isRunning();
	}


	@Override
	public void doCalc()
	{
		var opponentFinisherBlocker = getFinisherBlockingSupportiveAttackers();
		if (!opponentFinisherBlocker.isEmpty() && skirmishInformation.get().getStrategy() == ESkirmishStrategy.BLOCKING
				&& enableOpponentFinisherBlocker)
		{
			supportiveAttackers = opponentFinisherBlocker.keySet().stream().toList();
			supportiveAttackerOpponentFinisherBlocker = opponentFinisherBlocker;
			return;
		}
		supportiveAttackerOpponentFinisherBlocker = Collections.emptyMap();

		if (!shouldAddSupportiveAttacker())
		{
			supportiveAttackers = Collections.emptyList();
			return;
		}

		if (supportiveBlockerPos.get() != null)
		{
			supportiveAttackers = activateSingleSupportiveAttacker(supportiveBlockerPos.get());
		} else if (skirmishInformation.get().getStrategy() != ESkirmishStrategy.NONE
				|| (activateSupportiveInterceptor && redirectDetectionRequiresSupportiveAttacker()))
		{
			supportiveAttackers = activateSingleSupportiveAttacker(supportiveAttackerPos.get());
		} else
		{
			supportiveAttackers = Collections.emptyList();
		}
	}


	private boolean redirectDetectionRequiresSupportiveAttacker()
	{
		var rInfo = redirectorDetectionInformation.get();
		// DISRUPT_OPPONENT: The disrupt is done by the attacker, but an additional supportive attacker is also added
		// DOUBLE_ATTACKER: Add a supportive attacker that supports the attacker
		return rInfo.getRecommendedAction() == ERecommendedReceiverAction.DISRUPT_OPPONENT ||
				rInfo.getRecommendedAction() == ERecommendedReceiverAction.DOUBLE_ATTACKER;
	}


	private List<BotID> activateSingleSupportiveAttacker(IVector2 supportPos)
	{
		return potentialOffensiveBots.get().stream()
				.filter(b -> generallyAllowedToUseASingleBallCandidate() || !bestBallDefenderCandidates.get().contains(b))
				.filter(b -> !ballHandlingBots.get().contains(b))
				.map(b -> getWFrame().getBot(b))
				.min(Comparator.comparingDouble(
						e -> getTotalTimeWithHyst(supportPos, e)))
				.map(ITrackedBot::getBotId)
				.stream()
				.toList();
	}


	private double getTotalTimeWithHyst(IVector2 supportPos, ITrackedBot bot)
	{
		double hyst = 0;
		if (getAiFrame()
				.getPrevFrame()
				.getPlayStrategy()
				.getActiveRoles(ERole.SUPPORTIVE_ATTACKER)
				.stream()
				.anyMatch(e -> e.getBotID().equals(bot.getBotId())))
		{
			hyst = selectionHyst;
		}
		return TrajectoryGenerator.generatePositionTrajectory(bot, supportPos).getTotalTime() - hyst;
	}


	private Map<BotID, IVector2> getFinisherBlockingSupportiveAttackers()
	{
		IVector2 ballPos = kickOrigins.get().values().stream().findFirst().map(KickOrigin::getPos)
				.orElse(getBall().getPos());
		if (Geometry.getPenaltyAreaOur().withMargin(5).isPointInShape(ballPos)
				|| !Geometry.getPenaltyAreaOur().withMargin(marginAroundOurPenAreaToConsiderOpponentFinisherDangerous)
				.isPointInShape(ballPos))
		{
			return Collections.emptyMap();
		}
		var consideredBots = potentialOffensiveBots.get().stream()
				.filter(b -> !ballHandlingBots.get().contains(b))
				.map(b -> getWFrame().getBot(b))
				.toList();

		int size = Math.min(supportiveOpponentFinisherBlockPositionsLastFrame.size(),
				supportiveOpponentFinisherBlockPositions.get().size());

		Map<BotID, Integer> botIdToBlockerPositionIndex = new HashMap<>();
		for (int i = 0; i < size; i++)
		{
			// we match old and new point by ID in the list, this works since the list is ordered and
			// always created with the same pattern
			var oldPos = supportiveOpponentFinisherBlockPositionsLastFrame.get(i);
			var newPos = supportiveOpponentFinisherBlockPositions.get().get(i);

			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_FINISHER_BLOCK).add(
					new DrawableCircle(Circle.createCircle(oldPos, 80))
							.setFill(true)
							.setColor(new Color(96, 191, 2, 101))
			);
			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_FINISHER_BLOCK).add(
					new DrawableCircle(Circle.createCircle(newPos, 50))
							.setFill(true)
							.setColor(new Color(179, 65, 214, 191))
			);
			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_FINISHER_BLOCK).add(
					new DrawableLine(Lines.segmentFromPoints(oldPos, newPos)));

			// we do keep the assignment only if there was no noticeable jump in the position
			if (oldPos.distanceTo(newPos) < Geometry.getBotRadius() * 2)
			{
				// we fetch the old bot ID that fits the old position
				var oldEntryCandidate = supportiveAttackerOpponentFinisherBlocker.entrySet().stream()
						.min(Comparator.comparingDouble(e -> e.getValue().distanceTo(oldPos)));

				// we store the botID with its index in the new list
				int finalI = i;
				oldEntryCandidate.ifPresent(e -> botIdToBlockerPositionIndex.put(e.getKey(), finalI));
			}
		}

		Map<BotID, IVector2> supportiveFinisherBlockerMap = new HashMap<>();
		for (int i = 0; i < supportiveOpponentFinisherBlockPositions.get().size(); i++)
		{
			var supportPos = supportiveOpponentFinisherBlockPositions.get().get(i);
			if (singleAvailableBallCandidateUsedUp(supportiveFinisherBlockerMap))
			{
				consideredBots = consideredBots.stream()
						.filter(bot -> !bestBallDefenderCandidates.get().contains(bot.getBotId()))
						.toList();
			}

			int finalI = i;
			var botLastFrameAssignedToCurrentPosition = botIdToBlockerPositionIndex.entrySet().stream()
					.filter(e -> e.getValue() == finalI)
					.map(Map.Entry::getKey)
					.findFirst();

			consideredBots
					.stream()
					.filter(e -> !supportiveFinisherBlockerMap.containsKey(e.getBotId()))
					.min(Comparator.comparingDouble(
							bot -> getDistWithHyst(supportPos, bot,
									bot.getBotId() == botLastFrameAssignedToCurrentPosition.orElse(null))))
					.ifPresent(e -> supportiveFinisherBlockerMap.put(e.getBotId(), supportPos));
		}

		supportiveOpponentFinisherBlockPositionsLastFrame = supportiveOpponentFinisherBlockPositions.get();
		return supportiveFinisherBlockerMap;
	}


	private boolean generallyAllowedToUseASingleBallCandidate()
	{
		return ballHandlingBots.get().stream().noneMatch(botID -> bestBallDefenderCandidates.get().contains(botID));
	}


	private boolean singleAvailableBallCandidateUsedUp(Map<BotID, IVector2> supportiveFinisherBlockerMap)
	{
		if (!generallyAllowedToUseASingleBallCandidate())
		{
			return true;
		}
		return supportiveFinisherBlockerMap.keySet().stream()
				.anyMatch(botID -> bestBallDefenderCandidates.get().contains(botID));
	}


	private double getDistWithHyst(IVector2 supportPos, ITrackedBot bot, boolean applyAdditionalPosHyst)
	{
		double hyst = 0;
		if (this.supportiveAttackerOpponentFinisherBlocker.containsKey(bot.getBotId()))
		{
			hyst = opponentFinisherBlockerSelectionHystBot;
		}
		if (applyAdditionalPosHyst)
		{
			hyst = hyst + opponentFinisherBlockerSelectionHystPos;
			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_FINISHER_BLOCK).add(
					new DrawableLine(Lines.segmentFromPoints(bot.getPos(), supportPos))
							.setColor(Color.RED)
			);
			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_FINISHER_BLOCK).add(
					new DrawableAnnotation(supportPos, "Hyst: " + hyst)
							.withOffset(Vector2.fromY(100))
							.setColor(Color.RED));
		}

		return bot.getPos().distanceTo(supportPos) - hyst;
	}


	private boolean shouldAddSupportiveAttacker()
	{
		return getAiFrame().getGameState().isRunning()
				&& !(Geometry.getPenaltyAreaOur().withMargin(marginAroundOurPenAreaToDeactivateSupportiveAttackers)
				.isPointInShape(getBall().getPos()))
				&& !Geometry.getPenaltyAreaTheir().withMargin(marginOpponentPenAreaToDeactivateSupportiveAttacker)
				.isPointInShape(getBall().getPos());
	}
}
