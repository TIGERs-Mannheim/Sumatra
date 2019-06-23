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

import java.awt.Component;

import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ManagedConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.config.ConfigEditorView;
import edu.dhbw.mannheim.tigers.sumatra.view.config.IConfigEditorViewObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This is the presenter part of the ConfigEditor-module. The module provides common access to XML configurations which
 * are registered and allows the user to change them at runtime.
 * 
 * @author Gero
 * 
 */
public class ConfigEditorPresenter implements IConfigManagerObserver, IConfigEditorViewObserver, ISumatraViewPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ConfigEditorView	view;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ConfigEditorPresenter()
	{
		view = new ConfigEditorView();
		
		// Get initial state
		for (final ManagedConfig config : ConfigManager.getInstance().getLoadedConfigs())
		{
			onConfigAdded(config);
		}
		
		ConfigManager.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigManagerObserver -----------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public final void onConfigAdded(ManagedConfig newConfig)
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
		ConfigManager.getInstance().notifyConfigEdited(configKey);
	}
	
	
	@Override
	public boolean onSavePressed(String configKey)
	{
		return ConfigManager.getInstance().saveConfig(configKey);
	}
	
	
	@Override
	public void onReloadPressed(String configKey)
	{
		ConfigManager.getInstance().reloadConfig(configKey);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public Component getComponent()
	{
		return view;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return view;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
	}
}
