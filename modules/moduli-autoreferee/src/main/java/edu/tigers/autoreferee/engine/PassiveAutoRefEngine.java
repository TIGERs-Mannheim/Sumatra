package edu.tigers.autoreferee.engine;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;

import java.util.Set;


/**
 * The passive autoRef engine does not communicate with the game-controller.
 */
public class PassiveAutoRefEngine extends AutoRefEngine
{
	public PassiveAutoRefEngine(final Set<EGameEventDetectorType> activeDetectors)
	{
		super(activeDetectors);
	}


	@Override
	public void process(final IAutoRefFrame frame)
	{
		processEngine(frame).forEach(this::processGameEvent);
	}
}
