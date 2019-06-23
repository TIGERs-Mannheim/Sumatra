/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.module;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.wp.IWorldFrameObserver;


/**
 * @author "Lukas Magel"
 */
public class AutoRefModule extends AModule implements IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(AutoRefModule.class.getName());
	
	private List<IAutoRefStateObserver> refObserver = new CopyOnWriteArrayList<>();
	private AutoRefRunner runner;
	private ERemoteControlType remoteControlType;
	private boolean log2File;
	
	
	@Override
	public void initModule()
	{
		remoteControlType = ERemoteControlType.valueOf(
				getSubnodeConfiguration().getString("remoteControlType", ERemoteControlType.REMOTE_SSL_REFBOX.name()));
		log2File = getSubnodeConfiguration().getBoolean("log2file", true);
	}
	
	
	@Override
	public void deinitModule()
	{
		// No shutdown needed
	}
	
	
	@Override
	public void startModule()
	{
		// Load all classes to execute the static blocks for config registration
		new ActiveAutoRefEngine(null);
		AutoRefConfig.touch();
		
		if (!refObserver.isEmpty())
		{
			log.warn("There are observers left: " + refObserver);
			refObserver.clear();
		}
		
		runner = new AutoRefRunner(refObserver, remoteControlType, log2File);
	}
	
	
	@Override
	public void stopModule()
	{
		runner.stop();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAutoRefStateObserver observer)
	{
		refObserver.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAutoRefStateObserver observer)
	{
		refObserver.remove(observer);
	}
	
	
	/**
	 * @return
	 */
	public IAutoRefEngine getEngine()
	{
		return runner.getEngine();
	}
	
	
	/**
	 * @return
	 */
	public AutoRefState getState()
	{
		return runner.getState();
	}
	
	
	/**
	 * @param mode
	 * @throws StartModuleException
	 */
	public void start(final AutoRefMode mode) throws StartModuleException
	{
		runner.start(mode);
	}
	
	
	/**
	 * Stop the auto referee entirely
	 */
	public void stop()
	{
		runner.stop();
		runner = new AutoRefRunner(refObserver, remoteControlType, log2File);
	}
	
	
	/**
	 * Pause the auto referee if it is currently running
	 */
	public void pause()
	{
		runner.pause();
	}
	
	
	/**
	 * Resume the auto referee if it is active and paused
	 */
	public void resume()
	{
		runner.resume();
	}
	
	
	public AutoRefRunner getRunner()
	{
		return runner;
	}
}
