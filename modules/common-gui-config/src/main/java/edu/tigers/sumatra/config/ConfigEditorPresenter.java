/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.config;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClientsObserver;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import javax.swing.SwingUtilities;


/**
 * This is the presenter part of the ConfigEditor-module. The module provides common access to XML configurations which
 * are registered and allows the user to change them at runtime.
 */
public class ConfigEditorPresenter
		implements ISumatraViewPresenter, IConfigClientsObserver, IConfigEditorViewObserver
{
	@Getter
	private final ConfigEditorPanel viewPanel = new ConfigEditorPanel();


	public ConfigEditorPresenter()
	{
		ConfigRegistration.addObserver(this);

		SwingUtilities.invokeLater(() -> ConfigRegistration.getConfigClients().forEach(this::onNewConfigClient));
	}


	@Override
	public void onNewConfigClient(final String newClient)
	{
		SwingUtilities.invokeLater(() -> viewPanel.addConfigModel(newClient, this));
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
		viewPanel.refreshConfigModel(configKey, ConfigRegistration.loadConfig(configKey));
	}
}
