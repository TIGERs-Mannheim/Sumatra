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
import java.util.LinkedList;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class GameStateHistoryCalc implements IRefereeCalc
{
	private static final int		HISTORY_SIZE	= 5;
	
	// Using the implementation directly is normally considered bad practice, but a LinkedList implements both the List
	// as well as the Queue interface, which makes it very convenient for internal use
	private LinkedList<GameState>	stateHistory;
	
	
	/**
	 * 
	 */
	public GameStateHistoryCalc()
	{
		stateHistory = new LinkedList<>(Collections.singletonList(GameState.HALT));
	}
	
	
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
