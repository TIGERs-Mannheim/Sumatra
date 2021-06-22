/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.config;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClientsObserver;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;

import javax.swing.SwingUtilities;
import java.awt.Component;


/**
 * This is the presenter part of the ConfigEditor-module. The module provides common access to XML configurations which
 * are registered and allows the user to change them at runtime.
 *
 * @author Gero
 */
public class ConfigEditorPresenter implements ISumatraViewPresenter, IConfigClientsObserver,
		IConfigEditorViewObserver
{
	private final ConfigEditorPanel view;


	/**
	 */
	public ConfigEditorPresenter()
	{
		view = new ConfigEditorPanel();

		ConfigRegistration.addObserver(this);

		SwingUtilities.invokeLater(() -> ConfigRegistration.getConfigClients().forEach(this::onNewConfigClient));
	}


	@Override
	public void onNewConfigClient(final String newClient)
	{
		view.addConfigModel(newClient, this);
	}


	@Override
	public void onApplyPressed(final String configKey)
	{
		ConfigRegistration.applyConfig(configKey);
	}


	@Override
	public boolean onSavePressed(final String configKey)
	{
		return ConfigRegistration.save(configKey);
	}


	@Override
	public void onReloadPressed(final String configKey)
	{
		view.refreshConfigModel(configKey, ConfigRegistration.loadConfig(configKey));
	}


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
}
