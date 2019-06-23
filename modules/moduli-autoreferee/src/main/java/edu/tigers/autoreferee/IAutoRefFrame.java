/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefFrame
{
	
	/**
	 * @return
	 */
	IAutoRefFrame getPreviousFrame();
	
	
	/**
	 * @return
	 */
	SimpleWorldFrame getWorldFrame();
	
	
	/**
	 * @return
	 */
	GameState getGameState();
	
	
	/**
	 * @return
	 */
	BotPosition getLastBotCloseToBall();
	
	
	/**
	 * @return
	 */
	BotPosition getBotLastTouchedBall();
	
	
	/**
	 * @return
	 */
	Optional<BotPosition> getBotTouchedBall();
	
	
	/**
	 * @return
	 */
	TimedPosition getBallLeftFieldPos();
	
	
	/**
	 * @return
	 */
	boolean isBallInsideField();
	
	
	/**
	 * @return
	 */
	IVector2 getLastStopBallPosition();
	
	
	/**
	 * @return
	 */
	RefereeMsg getRefereeMsg();
	
	
	/**
	 * Returns a list of a specified number of previous game states as well as the current one
	 * 
	 * @return the list, not empty, unmodifiable, the current state has the index 0
	 */
	List<GameState> getStateHistory();
	
	
	/**
	 * @return timestamp in ns
	 */
	long getTimestamp();
	
	
	/**
	 * Clean up reference to previous frame
	 */
	void cleanUp();
	
	
	/**
	 * @return
	 */
	ShapeMap getShapes();
	
	
	/**
	 * @return
	 */
	Optional<PossibleGoal> getPossibleGoal();
}
