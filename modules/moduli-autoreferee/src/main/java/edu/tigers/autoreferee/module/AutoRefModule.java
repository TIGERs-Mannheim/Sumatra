/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.module;

import edu.tigers.autoreferee.IAutoRefObserver;
import edu.tigers.autoreferee.engine.AutoRefEngine;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class AutoRefModule extends AModule implements IWorldFrameObserver
{
	private static final Logger log = LogManager.getLogger(AutoRefModule.class.getName());

	private final List<IAutoRefObserver> observers = new CopyOnWriteArrayList<>();

	private AutoRefRunner runner = new AutoRefRunner(this::notifyNewGameEvent);


	@Override
	public void startModule()
	{
		if (!observers.isEmpty())
		{
			log.warn("There are observers left: {}", observers);
			observers.clear();
		}

		runner.start();
		performAutoStart();
	}


	@Override
	public void stopModule()
	{
		runner.stop();
	}


	private void notifyNewGameEvent(final IGameEvent gameEvent)
	{
		observers.forEach(o -> o.onNewGameEventDetected(gameEvent));
	}


	private void performAutoStart()
	{
		String autoRefMode = System.getProperty("autoref.mode");
		if (autoRefMode != null)
		{
			try
			{
				EAutoRefMode mode = EAutoRefMode.valueOf(autoRefMode);
				changeMode(mode);
			} catch (IllegalArgumentException e)
			{
				log.warn("Could not parse autoRef mode: {}", autoRefMode, e);
			}
		}
	}


	public void addObserver(final IAutoRefObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final IAutoRefObserver observer)
	{
		observers.remove(observer);
	}


	public void changeMode(final EAutoRefMode mode)
	{
		log.debug("Changing AutoRef mode to {}", mode);
		runner.changeMode(mode);
		observers.forEach(o -> o.onAutoRefModeChanged(mode));
		log.info("Changed AutoRef mode to {}", mode);
	}


	public void setGameEventDetectorActive(EGameEventDetectorType type, boolean active)
	{
		runner.setDetectorActive(type, active);
	}


	public AutoRefEngine getEngine()
	{
		return runner.getEngine();
	}


	public EAutoRefMode getMode()
	{
		return runner.getMode();
	}
}
