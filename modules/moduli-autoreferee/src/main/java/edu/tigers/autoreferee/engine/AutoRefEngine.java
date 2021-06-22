/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


public class AutoRefEngine
{
	private final GameEventEngine gameEventEngine;
	private final List<IAutoRefEngineObserver> observers = new CopyOnWriteArrayList<>();
	
	
	public AutoRefEngine(Set<EGameEventDetectorType> activeDetectors)
	{
		gameEventEngine = new GameEventEngine(activeDetectors);
	}
	
	
	public void addObserver(IAutoRefEngineObserver observer)
	{
		observers.add(observer);
	}
	
	
	public void removeObserver(IAutoRefEngineObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected List<IGameEvent> processEngine(final IAutoRefFrame frame)
	{
		return gameEventEngine.update(frame);
	}
	
	
	public void process(final IAutoRefFrame frame)
	{
		// empty
	}
	
	
	public void start()
	{
		// empty
	}
	
	
	public void stop()
	{
		gameEventEngine.reset();
	}
	
	
	protected void processGameEvent(final IGameEvent gameEvent)
	{
		observers.forEach(o -> o.onNewGameEventDetected(gameEvent));
	}
}
