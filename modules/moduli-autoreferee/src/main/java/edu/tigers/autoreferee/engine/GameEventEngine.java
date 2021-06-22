/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.detector.IGameEventDetector;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * The engine consults the {@link IGameEventDetector}s.
 */
@Log4j2
public class GameEventEngine
{
	private final List<IGameEventDetector> allDetectors = new ArrayList<>();
	private final Set<EGameEventDetectorType> activeDetectors;


	public GameEventEngine(Set<EGameEventDetectorType> activeDetectors)
	{
		this.activeDetectors = activeDetectors;

		for (EGameEventDetectorType eCalc : EGameEventDetectorType.values())
		{
			ConfigRegistration.registerClass("autoreferee", eCalc.getInstanceableClass().getImpl());
			if (eCalc.getInstanceableClass().getImpl() != null)
			{
				try
				{
					IGameEventDetector inst = (IGameEventDetector) eCalc.getInstanceableClass().newDefaultInstance();
					allDetectors.add(inst);
				} catch (InstanceableClass.NotCreateableException e)
				{
					log.error("Could not instantiate calculator: " + eCalc, e);
				}
			}
		}
	}


	/**
	 * @param frame
	 * @return
	 */
	public List<IGameEvent> update(final IAutoRefFrame frame)
	{
		GameState currentState = frame.getGameState();
		GameState lastState = frame.getPreviousFrame().getGameState();

		/*
		 * Retrieve all rules which are active in the current gamestate
		 */
		List<IGameEventDetector> detectors = this.allDetectors.stream()
				.filter(d -> activeDetectors.contains(d.getType()))
				.filter(d -> d.isActiveIn(currentState.getState()))
				.collect(Collectors.toList());

		/*
		 * Reset the detectors which have now become active
		 */
		detectors.stream()
				.filter(detector -> !detector.isActiveIn(lastState.getState()))
				.forEach(IGameEventDetector::reset);

		List<IGameEvent> gameEvents = new ArrayList<>();
		for (IGameEventDetector detector : detectors)
		{
			Optional<IGameEvent> result = detector.update(frame);
			result.ifPresent(gameEvents::add);
			result.ifPresent(event -> log.debug("Detected game event: {}", event));
		}

		return gameEvents;
	}


	public void reset()
	{
		allDetectors.forEach(IGameEventDetector::reset);
	}
}
