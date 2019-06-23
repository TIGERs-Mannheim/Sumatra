/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
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
	
	
	protected APreparingGameEventDetector(final EGameEventDetectorType type, final EGameState gamestate)
	{
		super(type, gamestate);
	}
	
	
	protected APreparingGameEventDetector(final EGameEventDetectorType type, final Set<EGameState> gamestates)
	{
		super(type, gamestates);
	}
	
	
	@Override
	public final Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (firstUpdate)
		{
			prepare(frame);
			firstUpdate = false;
		}
		return doUpdate(frame);
	}
	
	
	protected abstract void prepare(IAutoRefFrame frame);
	
	
	protected abstract Optional<IGameEvent> doUpdate(IAutoRefFrame frame);
	
	
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
