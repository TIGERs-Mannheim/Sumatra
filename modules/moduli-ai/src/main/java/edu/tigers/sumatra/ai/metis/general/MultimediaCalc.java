/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.Goal;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Control the multimedia features of our robots.
 */
@Log4j2
@RequiredArgsConstructor
public class MultimediaCalc extends ACalculator
{
	private final TimestampTimer timeoutTimer = new TimestampTimer(0.3);
	private final TimestampTimer cheeringTimer = new TimestampTimer(3.0);
	private BotID currentTimeoutBot = null;
	private boolean startedCheering = false;

	private final Supplier<Set<BotID>> crucialDefender;
	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBotsMap;

	@Getter
	private Map<BotID, MultimediaControl> multimediaControls;


	@Override
	public void doCalc()
	{
		GameState gameState = getAiFrame().getGameState();
		multimediaControls = new IdentityHashMap<>();

		if (gameState.getState() == EGameState.TIMEOUT)
		{
			handleTimeout();
		} else
		{
			handleRegularPlays();
		}

		cheerWhenWeShootAGoal();

		multimediaControls = Collections.unmodifiableMap(multimediaControls);
	}


	private MultimediaControl getControl(BotID botID)
	{
		return multimediaControls.computeIfAbsent(botID, id -> new MultimediaControl());
	}


	private void handleRegularPlays()
	{
		allActiveBots().forEach(id -> getControl(id).setLedColor(ELedColor.OFF));
		botsByPlay(EPlay.OFFENSIVE).forEach(id -> getControl(id).setLedColor(ELedColor.SLIGHTLY_ORANGE_YELLOW));
		botsByPlay(EPlay.SUPPORT).forEach(id -> getControl(id).setLedColor(ELedColor.WHITE));
		botsByPlay(EPlay.DEFENSIVE).forEach(id -> getControl(id).setLedColor(ELedColor.LIGHT_BLUE));
		botsByPlay(EPlay.KEEPER).forEach(id -> getControl(id).setLedColor(ELedColor.BLUE));
		botsByPlay(EPlay.BALL_PLACEMENT).forEach(id -> getControl(id).setLedColor(ELedColor.PURPLE));
		attacker().ifPresent(id -> getControl(id).setLedColor(ELedColor.PURPLE));
		botsByPlay(EPlay.KICKOFF).forEach(id -> getControl(id).setLedColor(ELedColor.PURPLE));
		crucialDefender().forEach(id -> getControl(id).setLedColor(ELedColor.GREEN));
	}


	private Set<BotID> crucialDefender()
	{
		return crucialDefender.get();
	}


	private Optional<BotID> attacker()
	{
		return offensiveStrategy.get().getAttackerBot();
	}


	private Set<BotID> botsByPlay(final EPlay support)
	{
		return desiredBotsMap.get().getOrDefault(support, Collections.emptySet());
	}


	private void handleTimeout()
	{
		if (currentTimeoutBot == null)
		{
			currentTimeoutBot = BotID.createBotId(0, getAiFrame().getTeamColor());
		}

		timeoutTimer.update(getWFrame().getTimestamp());
		if (timeoutTimer.isTimeUp(getWFrame().getTimestamp()))
		{
			timeoutTimer.reset();
			currentTimeoutBot = nextBot();
		}

		for (BotID botID : allAssignedBots())
		{
			if (botID == currentTimeoutBot)
			{
				getControl(botID).setLedColor(ELedColor.PURPLE);
			} else
			{
				getControl(botID).setLedColor(ELedColor.SLIGHTLY_ORANGE_YELLOW);
			}
		}
	}


	private Set<BotID> allAssignedBots()
	{
		return desiredBotsMap.get().values().stream().flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}


	private Set<BotID> allActiveBots()
	{
		return getWFrame().getTigerBotsAvailable().keySet();
	}


	private BotID nextBot()
	{
		final Set<BotID> timeoutBots = allAssignedBots();
		BotID id = currentTimeoutBot;
		for (int i = 0; i < BotID.BOT_ID_MAX; i++)
		{
			final int number = (id.getNumber() + 1) % (BotID.BOT_ID_MAX + 1);
			id = BotID.createBotId(number, getAiFrame().getTeamColor());
			if (timeoutBots.contains(id))
			{
				return id;
			}
		}
		return id;
	}


	private void cheerWhenWeShootAGoal()
	{
		final Optional<Goal> goalEvent = getAiFrame().getRefereeMsg().getGameEvents().stream()
				.filter(e -> e.getType() == EGameEvent.GOAL)
				.map(Goal.class::cast)
				.filter(goal -> goal.getTeam() == getAiFrame().getTeamColor())
				.findFirst();
		if (goalEvent.isPresent())
		{
			if (!startedCheering)
			{
				startedCheering = true;
				log.debug("Starting cheering song");
				cheeringTimer.start(getWFrame().getTimestamp());
			}
		} else
		{
			startedCheering = false;
		}
		if (cheeringTimer.isTimeUp(getWFrame().getTimestamp()))
		{
			log.debug("Stop cheering song");
			cheeringTimer.reset();
		} else if (cheeringTimer.isRunning())
		{
			allActiveBots().forEach(id -> getControl(id).setSong(ESong.CHEERING));
		}
	}
}
