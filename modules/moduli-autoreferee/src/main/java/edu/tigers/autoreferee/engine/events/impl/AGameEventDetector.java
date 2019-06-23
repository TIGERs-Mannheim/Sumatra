/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;

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
	
	
	/**
	 * @param gamestate The gamestate this rule will be active in
	 */
	protected AGameEventDetector(final EGameState gamestate)
	{
		this(EnumSet.of(gamestate));
	}
	
	
	/**
	 * @param activeStates the list of game states that the rule will be active in
	 */
	protected AGameEventDetector(final Set<EGameState> activeStates)
	{
		this.activeStates = activeStates;
	}
	
	
	@Override
	public boolean isActiveIn(final EGameState state)
	{
		return activeStates.contains(state);
	}
	
	
	protected static void registerClass(final Class<?> clazz)
	{
		ConfigRegistration.registerClass("autoreferee", clazz);
	}
}
