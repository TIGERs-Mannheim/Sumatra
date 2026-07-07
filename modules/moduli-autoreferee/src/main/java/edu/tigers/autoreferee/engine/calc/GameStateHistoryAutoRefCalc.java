package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.referee.data.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;


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
