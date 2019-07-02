/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * Collect a history of recent game states.
 */
public class GameStateHistoryAutoRefCalc implements IAutoRefereeCalc
{
	private static final int HISTORY_SIZE = 5;
	private final Deque<GameState> stateHistory = new LinkedList<>(Collections.singletonList(GameState.HALT));
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		if (!stateHistory.peekFirst().isSameStateAndForTeam(frame.getGameState()))
		{
			add(frame.getGameState());
		}
		
		frame.setStateHistory(new ArrayList<>(stateHistory));
	}
	
	
	private void add(final GameState state)
	{
		if (stateHistory.size() >= HISTORY_SIZE)
		{
			stateHistory.pollLast();
		}
		stateHistory.offerFirst(state);
	}
}
