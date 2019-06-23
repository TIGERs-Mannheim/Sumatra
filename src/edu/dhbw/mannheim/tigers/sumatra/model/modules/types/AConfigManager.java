/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.moduli.AModule;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This is the base type for any implementations of the config-module. It lets others register themselves as
 * {@link IConfigClient} and then handles the lifecycle of the specified configs. Furthermore, these clients will be
 * notified if their config was edited or changed in any other ways. If someone implements and registers as
 * {@link IConfigManagerObserver} he will get notified when new configs are added.
 * 
 * @author Gero
 * 
 */
public abstract class AConfigManager extends AModule implements IModuliStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String							MODULE_TYPE				= "AConfigEditor";
	/** */
	public static final String							MODULE_ID				= "config";
	
	protected static final Set<IConfigClient>		REGISTERED_CLIENTS	= new HashSet<IConfigClient>();
	
	private boolean										alreadyResolved		= false;
	
	private final List<IConfigManagerObserver>	observers				= new LinkedList<IConfigManagerObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AConfigManager()
	{
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Use this method to register a config which should be managed by the concrete config-manager implementation
	 * 
	 * @param newClient
	 */
	public static void registerConfigClient(IConfigClient newClient)
	{
		REGISTERED_CLIENTS.add(newClient);
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case RESOLVED:
				if (!alreadyResolved)
				{
					onStartup();
					alreadyResolved = true;
				}
				break;
			
			case NOT_LOADED:
				// Clear if moduli-config is reloaded
				REGISTERED_CLIENTS.clear();
				onStop();
				break;
			case ACTIVE:
				break;
			default:
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- observable -----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param newObserver
	 */
	public void addObserver(IConfigManagerObserver newObserver)
	{
		synchronized (observers)
		{
			observers.add(newObserver);
		}
	}
	
	
	/**
	 * 
	 * @param oldObserver
	 * @return
	 */
	public boolean removeObserver(IConfigManagerObserver oldObserver)
	{
		synchronized (observers)
		{
			return observers.remove(oldObserver);
		}
	}
	
	
	protected void notifyConfigAdded(ManagedConfig newConfig)
	{
		synchronized (observers)
		{
			for (final IConfigManagerObserver observer : observers)
			{
				observer.onConfigAdded(newConfig);
			}
		}
	}
	
	
	protected void notifyConfigReloaded(ManagedConfig config)
	{
		synchronized (observers)
		{
			for (final IConfigManagerObserver observer : observers)
			{
				observer.onConfigReloaded(config);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- abstract methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return All {@link ManagedConfig} that are currently loaded
	 */
	public abstract Collection<ManagedConfig> getLoadedConfigs();
	
	
	/**
	 * Called once when all modules has been loaded the first time
	 */
	protected abstract void onStartup();
	
	
	/**
	 * Called whenever moduli is going to be reloaded (first step in new process)
	 */
	protected abstract void onStop();
	
	
	/**
	 * Called whenever a new client is registered
	 * 
	 * @param newClient
	 */
	protected abstract void onNewClient(IConfigClient newClient);
	
	
	/**
	 * Loads a config from the filename which is associated with the given key
	 * 
	 * @param configKey
	 * @param newFileName
	 * @return Success
	 */
	public abstract boolean loadConfig(String configKey, String newFileName);
	
	
	/**
	 * Save the config associated with the given key
	 * 
	 * @param configKey
	 * @return Success
	 */
	public abstract boolean saveConfig(String configKey);
	
	
	/**
	 * Saves the config associated with the given configKey under the given file name
	 * 
	 * @param configKey
	 * @param newFileName
	 * @return Success
	 */
	public abstract boolean saveConfig(String configKey, String newFileName);
	
	
	/**
	 * Reload the config associated with the given key
	 * 
	 * @param configKey
	 * @return Success
	 */
	public abstract boolean reloadConfig(String configKey);
	
	
	/**
	 * Notify the client of the config associated with the given key that it has changed (e.g., by editing in the editor)
	 * 
	 * @param configKey
	 */
	public abstract void notifyConfigEdited(String configKey);
	
	
	// --------------------------------------------------------------------------
	// --- manage configs -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configKey
	 * @return A list of available config files
	 */
	public abstract List<String> getAvailableConfigs(String configKey);
	
	
	/**
	 * @param configKey
	 * @return The name of the currently loaded config-file the given key is associated to (or <code>null</code> if there
	 *         is no config for this key)
	 */
	public abstract String getLoadedFileName(String configKey);
	
	
	/**
	 * If there is no {@link ManagedConfig} for the given key, there is an empty instance
	 * 
	 * @param configKey
	 * @param newObserver
	 */
	public abstract void registerObserverAt(String configKey, IConfigObserver newObserver);
	
	
	/**
	 * @param configKey
	 * @param oldObserver
	 * @return <code>false</code> if there is no config associated with the given key
	 */
	public abstract boolean unregisterObserverAt(String configKey, IConfigObserver oldObserver);
}
