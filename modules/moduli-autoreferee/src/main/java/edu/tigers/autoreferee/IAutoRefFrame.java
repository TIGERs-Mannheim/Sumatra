package edu.tigers.autoreferee;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;

import java.util.List;
import java.util.Optional;


/**
 * The autoRef frame contains information that are required for further processing
 */
public interface IAutoRefFrame
{
	IAutoRefFrame getPreviousFrame();


	SimpleWorldFrame getWorldFrame();


	GameState getGameState();


	List<BotPosition> getBotsLastTouchedBall();


	List<BotPosition> getBotsTouchingBall();


	Optional<BallLeftFieldPosition> getBallLeftFieldPos();


	boolean isBallInsideField();


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


	ShapeMap getShapes();
}
