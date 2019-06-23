/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class GameStateHistoryCalc implements IRefereeCalc
{
	private static int							historySize	= 5;
	
	// Using the implementation directly is normally considered bad practice, but a LinkedList implements both the List
	// as well as the Queue interface, which makes it very convenient for internal use
	private LinkedList<EGameStateNeutral>	stateHistory;
	
	
	/**
	 * 
	 */
	public GameStateHistoryCalc()
	{
		stateHistory = new LinkedList<>(Arrays.asList(EGameStateNeutral.UNKNOWN));
	}
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		if (stateHistory.peekFirst() != frame.getGameState())
		{
			add(frame.getGameState());
		}
		
		frame.setStateHistory(Collections.unmodifiableList(stateHistory));
	}
	
	
	private void add(final EGameStateNeutral state)
	{
		if (stateHistory.size() >= historySize)
		{
			stateHistory.pollLast();
		}
		stateHistory.offerFirst(state);
	}
	
}
