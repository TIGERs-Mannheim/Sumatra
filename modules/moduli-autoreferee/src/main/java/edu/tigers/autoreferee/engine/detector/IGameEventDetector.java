package edu.tigers.autoreferee.engine.detector;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;

import java.util.Optional;


/**
 * Detects {@link IGameEvent}s
 */
public interface IGameEventDetector
{
	/**
	 * @param state
	 * @return
	 */
	boolean isActiveIn(EGameState state);


	/**
	 * @param frame
	 * @return
	 */
	Optional<IGameEvent> update(IAutoRefFrame frame);


	/**
	 * Reset
	 */
	void reset();


	/**
	 * @return the type of the game event detector
	 */
	EGameEventDetectorType getType();
}
