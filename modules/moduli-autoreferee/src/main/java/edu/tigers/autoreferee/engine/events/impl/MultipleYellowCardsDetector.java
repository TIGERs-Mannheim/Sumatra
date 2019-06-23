/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Detect if a team has received three yellow cards in a row and give a penalty kick to the opposing team in this case
 */
public class MultipleYellowCardsDetector extends AGameEventDetector
{
	private static final int PRIORITY = 2;
	
	@Configurable(defValue = "3", comment = "Number of yellow cards until a penalty kick is given to the opposing team")
	private static int numberOfYellowCards = 3;
	
	private boolean penaltyGivenInThisStopPhase = false;
	private final Map<ETeamColor, Integer> cardOffsets = new EnumMap<>(ETeamColor.class);
	
	
	public MultipleYellowCardsDetector()
	{
		super(EGameEventDetectorType.MULTIPLE_YELLOW_CARDS, EGameState.STOP);
		cardOffsets.put(ETeamColor.YELLOW, 0);
		cardOffsets.put(ETeamColor.BLUE, 0);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (penaltyGivenInThisStopPhase)
		{
			return Optional.empty();
		}
		
		int blueCards = frame.getRefereeMsg().getTeamInfoBlue().getYellowCards()
				- cardOffsets.get(ETeamColor.BLUE);
		int yellowCards = frame.getRefereeMsg().getTeamInfoYellow().getYellowCards()
				- cardOffsets.get(ETeamColor.YELLOW);
		
		ETeamColor team;
		if (blueCards >= numberOfYellowCards && yellowCards >= numberOfYellowCards)
		{
			team = Math.random() < 0.5 ? ETeamColor.YELLOW : ETeamColor.BLUE;
		} else if (blueCards >= numberOfYellowCards)
		{
			team = ETeamColor.BLUE;
		} else if (yellowCards >= numberOfYellowCards)
		{
			team = ETeamColor.YELLOW;
		} else
		{
			return Optional.empty();
		}
		
		cardOffsets.computeIfPresent(team, (k, v) -> v + numberOfYellowCards);
		penaltyGivenInThisStopPhase = true;
		
		FollowUpAction followUp = new FollowUpAction(FollowUpAction.EActionType.PENALTY, team.opposite(),
				NGeometry.getPenaltyMark(team));
		
		return Optional.of(new GameEvent(EGameEvent.MULTIPLE_YELLOW_CARDS, frame.getTimestamp(), team, followUp));
	}
	
	
	@Override
	public void reset()
	{
		penaltyGivenInThisStopPhase = false;
	}
}
