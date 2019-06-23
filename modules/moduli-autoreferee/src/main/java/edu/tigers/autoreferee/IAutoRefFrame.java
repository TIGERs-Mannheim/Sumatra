/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 12, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefFrame
{
	
	/**
	 * @return
	 */
	public IAutoRefFrame getPreviousFrame();
	
	
	/**
	 * @return
	 */
	public SimpleWorldFrame getWorldFrame();
	
	
	/**
	 * @return
	 */
	public EGameStateNeutral getGameState();
	
	
	/**
	 * @return
	 */
	public BotPosition getBotLastTouchedBall();
	
	
	/**
	 * @return
	 */
	public Optional<BotPosition> getBotTouchedBall();
	
	
	/**
	 * @return
	 */
	public IVector2 getBallLeftFieldPos();
	
	
	/**
	 * @return
	 */
	public RefereeMsg getRefereeMsg();
	
	
	/**
	 * Returns a list of a specified number of previous game states as well as the current one
	 * 
	 * @return the list, not empty, unmodifiable, the current state has the index 0
	 */
	public List<EGameStateNeutral> getStateHistory();
	
	
	/**
	 * @return timestamp in ns
	 */
	public long getTimestamp();
	
	
	/**
	 * 
	 */
	public void cleanUp();
	
	
	/**
	 * @return
	 */
	public ShapeMap getShapes();
	
	
	/**
	 * @return
	 */
	public Optional<PossibleGoal> getPossibleGoal();
	
}
