/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.impl.AttackerToDefenseAreaDistanceDetector;
import edu.tigers.autoreferee.engine.events.impl.AttackerTouchKeeperDetector;
import edu.tigers.autoreferee.engine.events.impl.BallHoldInPenAreaDetector;
import edu.tigers.autoreferee.engine.events.impl.BallLeftFieldDetector;
import edu.tigers.autoreferee.engine.events.impl.BallSpeedingDetector;
import edu.tigers.autoreferee.engine.events.impl.BotCollisionDetector;
import edu.tigers.autoreferee.engine.events.impl.BotInDefenseAreaDetector;
import edu.tigers.autoreferee.engine.events.impl.BotNumberDetector;
import edu.tigers.autoreferee.engine.events.impl.BotStopSpeedDetector;
import edu.tigers.autoreferee.engine.events.impl.DefenderToKickPointDistanceDetector;
import edu.tigers.autoreferee.engine.events.impl.DoubleTouchDetector;
import edu.tigers.autoreferee.engine.events.impl.DribblingDetector;
import edu.tigers.autoreferee.engine.events.impl.GoalDetector;
import edu.tigers.autoreferee.engine.events.impl.KickTimeoutDetector;
import edu.tigers.autoreferee.engine.events.impl.MultipleYellowCardsDetector;
import edu.tigers.autoreferee.engine.events.impl.NoProgressDetector;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class GameEventEngine
{
	private final List<IGameEventDetector> allDetectors = new ArrayList<>();
	private Set<EGameEventDetectorType> activeDetectors;
	
	
	public GameEventEngine()
	{
		allDetectors.add(new AttackerToDefenseAreaDistanceDetector());
		allDetectors.add(new AttackerTouchKeeperDetector());
		allDetectors.add(new BallLeftFieldDetector());
		allDetectors.add(new BallSpeedingDetector());
		allDetectors.add(new BotCollisionDetector());
		allDetectors.add(new BotInDefenseAreaDetector());
		allDetectors.add(new BotNumberDetector(ETeamColor.YELLOW));
		allDetectors.add(new BotNumberDetector(ETeamColor.BLUE));
		allDetectors.add(new BotStopSpeedDetector());
		allDetectors.add(new DoubleTouchDetector());
		allDetectors.add(new DribblingDetector());
		allDetectors.add(new DefenderToKickPointDistanceDetector());
		allDetectors.add(new GoalDetector());
		allDetectors.add(new KickTimeoutDetector());
		allDetectors.add(new BallHoldInPenAreaDetector());
		allDetectors.add(new NoProgressDetector());
		allDetectors.add(new MultipleYellowCardsDetector());
		activeDetectors = new HashSet<>(Arrays.asList(EGameEventDetectorType.values()));
	}
	
	
	/**
	 * @param detectorTypes
	 */
	public void setActiveDetectors(final Set<EGameEventDetectorType> detectorTypes)
	{
		this.activeDetectors = detectorTypes;
	}
	
	
	/**
	 * reset
	 */
	public void reset()
	{
		allDetectors.forEach(IGameEventDetector::reset);
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
				.sorted(Comparator.comparingInt(IGameEventDetector::getPriority))
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
		}
		
		return gameEvents;
	}
}
