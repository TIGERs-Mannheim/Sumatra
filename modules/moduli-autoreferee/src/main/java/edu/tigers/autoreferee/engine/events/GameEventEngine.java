/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEventDetector.EGameEventDetectorType;
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
import edu.tigers.autoreferee.engine.events.impl.NoProgressDetector;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class GameEventEngine
{
	private Map<EGameEventDetectorType, IGameEventDetector> detectors = new EnumMap<>(EGameEventDetectorType.class);
	
	
	/**
	 * constructor
	 */
	public GameEventEngine()
	{
		this(new HashSet<>(Arrays.asList(EGameEventDetectorType.values())));
	}
	
	
	/**
	 * @param detectorTypes
	 */
	public GameEventEngine(final Set<EGameEventDetectorType> detectorTypes)
	{
		detectorTypes.forEach(type -> detectors.put(type, createDetector(type)));
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private static IGameEventDetector createDetector(final EGameEventDetectorType type)
	{
		switch (type)
		{
			case ATTACKER_TO_DEFENSE_DISTANCE:
				return new AttackerToDefenseAreaDistanceDetector();
			case ATTACKER_TOUCHED_KEEPER:
				return new AttackerTouchKeeperDetector();
			case BALL_LEFT_FIELD_ICING:
				return new BallLeftFieldDetector();
			case BALL_SPEEDING:
				return new BallSpeedingDetector();
			case BOT_COLLISION:
				return new BotCollisionDetector();
			case BOT_IN_DEFENSE_AREA:
				return new BotInDefenseAreaDetector();
			case BOT_NUMBER:
				return new BotNumberDetector();
			case BOT_STOP_SPEED:
				return new BotStopSpeedDetector();
			case DOUBLE_TOUCH:
				return new DoubleTouchDetector();
			case DRIBBLING:
				return new DribblingDetector();
			case DEFENDER_TO_KICK_POINT_DISTANCE:
				return new DefenderToKickPointDistanceDetector();
			case GOAL:
				return new GoalDetector();
			case KICK_TIMEOUT:
				return new KickTimeoutDetector();
			case BALL_HOLD_IN_PENAREA:
				return new BallHoldInPenAreaDetector();
			case NO_PROGRESS:
				return new NoProgressDetector();
			default:
				throw new IllegalArgumentException("Please add the new type \"" + type + "\" to this switch case clause!");
		}
	}
	
	
	/**
	 * @param detectorTypes
	 */
	public void setActiveDetectors(final Set<EGameEventDetectorType> detectorTypes)
	{
		Set<EGameEventDetectorType> toBeRemoved = Sets.difference(detectors.keySet(), detectorTypes).immutableCopy();
		Set<EGameEventDetectorType> toBeAdded = Sets.difference(detectorTypes, detectors.keySet()).immutableCopy();
		
		toBeRemoved.forEach(type -> detectors.remove(type));
		toBeAdded.forEach(type -> detectors.put(type, createDetector(type)));
	}
	
	
	/**
	 * @param type
	 */
	public void activateDetector(final EGameEventDetectorType type)
	{
		if (!detectors.containsKey(type))
		{
			detectors.put(type, createDetector(type));
		}
	}
	
	
	/**
	 * @param type
	 */
	public void deactivateDetector(final EGameEventDetectorType type)
	{
		detectors.remove(type);
	}
	
	
	/**
	 * reset
	 */
	public void reset()
	{
		detectors.values().forEach(IGameEventDetector::reset);
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
		List<IGameEventDetector> activeDetectors = detectors.values().stream()
				.filter(detector -> detector.isActiveIn(currentState.getState()))
				.sorted(IGameEventDetector.GameEventDetectorComparator.INSTANCE)
				.collect(Collectors.toList());
		
		/*
		 * Reset the detectors which have now become active
		 */
		activeDetectors.stream()
				.filter(detector -> !detector.isActiveIn(lastState.getState()))
				.forEach(IGameEventDetector::reset);
		
		List<IGameEvent> gameEvents = new ArrayList<>();
		for (IGameEventDetector detector : activeDetectors)
		{
			Optional<IGameEvent> result = detector.update(frame, gameEvents);
			result.ifPresent(gameEvents::add);
		}
		
		return gameEvents;
	}
}
