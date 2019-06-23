/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Abstract implementation which provides a prepare method to do initial work
 * 
 * @author "Lukas Magel"
 */
public abstract class APreparingGameEventDetector extends AGameEventDetector
{
	
	private boolean firstUpdate = true;
	
	
	/**
	 * @param gamestate
	 */
	protected APreparingGameEventDetector(final EGameState gamestate)
	{
		super(gamestate);
	}
	
	
	/**
	 * @param gamestates
	 */
	protected APreparingGameEventDetector(final Set<EGameState> gamestates)
	{
		super(gamestates);
	}
	
	
	@Override
	public final Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if (firstUpdate)
		{
			prepare(frame);
			firstUpdate = false;
		}
		return doUpdate(frame, violations);
	}
	
	
	protected abstract void prepare(IAutoRefFrame frame);
	
	
	protected abstract Optional<IGameEvent> doUpdate(IAutoRefFrame frame, List<IGameEvent> violations);
	
	
	@Override
	public final void reset()
	{
		firstUpdate = true;
		doReset();
	}
	
	
	protected void doReset()
	{
	}
	
}
