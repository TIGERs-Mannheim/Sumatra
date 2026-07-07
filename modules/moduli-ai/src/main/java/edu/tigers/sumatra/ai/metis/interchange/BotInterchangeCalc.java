package edu.tigers.sumatra.ai.metis.interchange;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Determine the number of bots to be interchanged. Bots are interchanged during STOP and also in any gameState
 * if the number of robots is reduced (yellow card).
 */
@RequiredArgsConstructor
public class BotInterchangeCalc extends ACalculator
{
	@Configurable(comment = "Should automatic robot interchange be processed?", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean robotInterchangeEnabled = true;

	@Configurable(comment = "Should automatic robot interchange also remove weak bots?", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean interchangeWeakBots = true;

	@Configurable(comment = "Should bots also interchange during running game?", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean interchangeDuringRunning = true;

	@Configurable(comment = "Should bots interchange in powerplay when yellow card was given?", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean activeForPowerPlay = false;

	@Getter
	private List<BotID> botsToInterchange = new ArrayList<>();

	@Getter
	private int numInterchangeBots;

	private boolean isPowerPlay;

	private final Supplier<List<BotID>> weakBots;

	private final TimestampTimer botSizeChangedTimer = new TimestampTimer(0.5);
	private int currentRobotCount = 0;


	@Override
	public boolean isCalculationNecessary()
	{
		// We normally do not want any bots waiting for interchange during a running game
		// We might consider situation where we have the advantage and a forced stop could disturb us
		isPowerPlay = getWFrame().getTigerBotsVisible().size() > getWFrame().getOpponentBots().size();
		return robotInterchangeEnabled && (getAiFrame().getGameState().isStoppedGame() || interchangeDuringRunning ||
				(isPowerPlay && activeForPowerPlay));
	}


	@Override
	protected void reset()
	{
		botsToInterchange = Collections.emptyList();
		numInterchangeBots = 0;
	}


	private void drawSubstitutionBots()
	{
		var tigersBotsInSubstitutionArea = getWFrame().getTigerBotsInSubstitutionArea().values();
		var opponentBotsInSubstitutionArea = getWFrame().getOpponentBotsInSubstitutionArea().values();

		tigersBotsInSubstitutionArea.forEach(
				bot -> getShapes(EAiShapesLayer.AI_SUBSTITUTIONAREA_BOTS).add(new DrawableCircle(
						bot.getPos(), Geometry.getBotRadius() + 20,
						Color.cyan
				))
		);
		opponentBotsInSubstitutionArea.forEach(
				bot -> getShapes(EAiShapesLayer.AI_SUBSTITUTIONAREA_BOTS).add(new DrawableCircle(
						bot.getPos(), Geometry.getBotRadius() + 20,
						Color.pink
				))
		);
	}


	@Override
	public void doCalc()
	{
		drawSubstitutionBots();

		int robotCount = getWFrame().getTigerBotsVisible().size();
		if (robotCount > currentRobotCount)
		{
			currentRobotCount = robotCount;
			botSizeChangedTimer.reset();
		} else if (robotCount < currentRobotCount)
		{
			botSizeChangedTimer.update(getWFrame().getTimestamp());
			if (botSizeChangedTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				currentRobotCount = robotCount;
				botSizeChangedTimer.reset();
			}
		}

		if (isMaintenanceAfterGame())
		{
			botsToInterchange = getWFrame().getTigerBotsAvailable().keySet().stream().toList();
			numInterchangeBots = botsToInterchange.size();
			return;
		}
		// substitution intent (removed!) on our side is usually triggered when a bot does bullshit or loses parts,
		// in that case the weakest bot is usually not the bot wanted to be exchanged
		int numWeakBots = 0;
		int numBotsToBeRemoved = Math.max(0, currentRobotCount - numberOfAllowedBots());
		if (interchangeWeakBots)
		{
			numWeakBots = weakBots.get().size();
		}
		int numBotsToInterchange = Math.max(numBotsToBeRemoved, numWeakBots);
		// During running, we only want to interchange bots because of yellow cards
		if (getAiFrame().getGameState().isRunning() && (interchangeDuringRunning || (activeForPowerPlay && isPowerPlay)))
		{
			numBotsToInterchange = numBotsToBeRemoved;
		}

		botsToInterchange = weakestBots(numBotsToInterchange);
		numInterchangeBots = botsToInterchange.size();
	}


	private int numberOfAllowedBots()
	{
		return getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor()).getMaxAllowedBots();
	}


	private List<BotID> weakestBots(final long numberOfBotsToBeRemoved)
	{
		List<BotID> weakestBots = new ArrayList<>(weakBots.get().stream().toList());
		weakestBots.addAll(getWFrame().getTigerBotsInSubstitutionArea().values().stream()
				.map(ITrackedBot::getBotId)
				.toList());
		weakestBots.addAll(botsToInterchange);
		weakestBots.addAll(supporterBotsSortedByBattery().stream()
				.toList()
		);
		weakestBots.addAll(getWFrame().getTigerBotsAvailable().values().stream()
				.map(ITrackedBot::getBotId)
				.toList()
		);

		// Do not use bots that do ball placement currently, as they may drive through the substitution area intentionally
		Set<BotID> ballPlacementBots = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.BALL_PLACEMENT, Set.of());
		weakestBots.removeAll(ballPlacementBots);

		weakestBots.remove(getAiFrame().getKeeperId());
		weakestBots.add(getAiFrame().getKeeperId());

		weakestBots.retainAll(getWFrame().getTigerBotsAvailable().keySet());

		return weakestBots.stream()
				.distinct()
				.limit(numberOfBotsToBeRemoved)
				.toList();
	}


	private List<BotID> supporterBotsSortedByBattery()
	{
		var botList = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT).stream()
				.map(ARole::getBotID).toList();
		if (botList.isEmpty())
		{
			botList = getWFrame().getTigerBotsAvailable().values().stream().map(ITrackedBot::getBotId).toList();
		}
		return botList.stream()
				.filter(botID -> getWFrame().getTigerBotsAvailable().containsKey(botID))
				.sorted((bot1, bot2) -> Float.compare(
						getWFrame().getBot(bot1).getRobotInfo().getBatteryRelative(),
						getWFrame().getBot(bot2).getRobotInfo().getBatteryRelative()
				))
				.toList();
	}


	private boolean isMaintenanceAfterGame()
	{
		boolean isPostGame = getAiFrame().getGameState().getState() == EGameState.POST_GAME;
		Map<ETeamColor, Integer> goals = getAiFrame().getRefereeMsg().getGoals();
		ETeamColor ourTeam = getAiFrame().getTeamColor();
		int ourGoals = goals.get(ourTeam);
		int theirGoals = goals.get(ourTeam.opposite());
		boolean cheering = ourGoals > theirGoals || (ourGoals == theirGoals && ourTeam == ETeamColor.YELLOW);
		return isPostGame && !cheering;
	}
}
