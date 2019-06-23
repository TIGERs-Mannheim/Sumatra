/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEventDetector;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Abstract base class that contains common operations for the game rules
 * 
 * @author "Lukas Magel"
 */
public abstract class AGameEventDetector implements IGameEventDetector
{
	private final Set<EGameState> activeStates;
	private final EGameEventDetectorType type;
	
	
	/**
	 * @param gamestate The gamestate this rule will be active in
	 */
	protected AGameEventDetector(final EGameEventDetectorType type, final EGameState gamestate)
	{
		this(type, EnumSet.of(gamestate));
	}
	
	
	/**
	 * @param activeStates the list of game states that the rule will be active in
	 */
	protected AGameEventDetector(final EGameEventDetectorType type, final Set<EGameState> activeStates)
	{
		this.type = type;
		this.activeStates = activeStates;
	}
	
	
	@Override
	public boolean isActiveIn(final EGameState state)
	{
		return activeStates.contains(state);
	}
	
	
	@Override
	public EGameEventDetectorType getType()
	{
		return type;
	}
	
	
	protected static void registerClass(final Class<?> clazz)
	{
		ConfigRegistration.registerClass("autoreferee", clazz);
	}
}
