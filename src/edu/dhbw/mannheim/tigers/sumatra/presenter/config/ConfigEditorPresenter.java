/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.config;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ManagedConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.config.ConfigEditorView;
import edu.dhbw.mannheim.tigers.sumatra.view.config.IConfigEditorViewObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This is the presenter part of the ConfigEditor-module. The module provides common access to XML configurations which
 * are registered and allows the user to change them at runtime.
 * 
 * @author Gero
 * 
 */
public class ConfigEditorPresenter implements IModuliStateObserver, IConfigManagerObserver, IConfigEditorViewObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log				= Logger.getLogger(ConfigEditorPresenter.class.getName());
	
	private final SumatraModel			model				= SumatraModel.getInstance();
	private final ModuliStateAdapter	moduliAdapter	= ModuliStateAdapter.getInstance();
	
	private AConfigManager				configManager	= null;
	
	private final ConfigEditorView	view;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ConfigEditorPresenter()
	{
		view = new ConfigEditorView();
		moduliAdapter.addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigManagerObserver -----------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onConfigAdded(ManagedConfig newConfig)
	{
		if (newConfig.getClient().isEditable())
		{
			view.addConfigModel(newConfig, this);
		}
	}
	
	
	@Override
	public void onConfigReloaded(ManagedConfig config)
	{
		if (config.getClient().isEditable())
		{
			view.refreshConfigModel(config);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigEditorViewObserver --------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onApplyPressed(String configKey)
	{
		configManager.notifyConfigEdited(configKey);
	}
	
	
	@Override
	public boolean onSavePressed(String configKey)
	{
		return configManager.saveConfig(configKey);
	}
	
	
	@Override
	public void onReloadPressed(String configKey)
	{
		configManager.reloadConfig(configKey);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case RESOLVED:
				if (configManager == null)
				{
					try
					{
						configManager = (AConfigManager) model.getModule(AConfigManager.MODULE_ID);
						
						// Get initial state
						for (final ManagedConfig config : configManager.getLoadedConfigs())
						{
							onConfigAdded(config);
						}
						
					} catch (final ModuleNotFoundException err1)
					{
						log.error("Unable to getModule '" + AConfigManager.MODULE_ID + "'!");
						return;
					}
				}
				
				configManager.removeObserver(this);
				configManager.addObserver(this);
				
				break;
			case ACTIVE:
				break;
			case NOT_LOADED:
				break;
			default:
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return view;
	}
}
