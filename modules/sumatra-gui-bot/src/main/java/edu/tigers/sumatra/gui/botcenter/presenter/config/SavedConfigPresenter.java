/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.botcenter.presenter.config;

import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.gui.botcenter.view.config.IBotConfigObserver;
import edu.tigers.sumatra.gui.botcenter.view.config.SavedConfigFilePanel;
import edu.tigers.sumatra.gui.botcenter.view.config.SavedConfigsPanel;
import edu.tigers.sumatra.model.SumatraModel;
import org.apache.commons.lang.Validate;

import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class SavedConfigPresenter implements IBotConfigObserver
{
	private final ConfigFileDatabaseManager databaseManager;

	private final SavedConfigsPanel savedConfigsPanel;
	private final Map<Integer, SavedConfigFilePanel> panels = new HashMap<>();


	public SavedConfigPresenter(
			final SavedConfigsPanel savedConfigsPanel,
			final ConfigFileDatabaseManager databaseManager
	)
	{
		this.databaseManager = databaseManager;
		this.savedConfigsPanel = savedConfigsPanel;
		setTabs(this.databaseManager.getAllSavedConfigs());
	}


	private SavedConfigFilePanel getPanel(int configId)
	{
		Validate.isTrue(panels.containsKey(configId));
		return panels.get(configId);
	}


	private void onSave(ConfigFile file)
	{
		databaseManager.addEntry(file);
		if (databaseManager.isAutoUpdate(file.getConfigId(), file.getVersion()))
		{
			saveToAll(file);
		}
	}


	private void onDelete(int configId, Integer version)
	{
		databaseManager.deleteEntry(configId, version);
		setTabs(databaseManager.getAllSavedConfigs());
	}


	private void onVersionSelected(int configId, int version)
	{
		SavedConfigFilePanel panel = getPanel(configId);
		Optional<ConfigFile> file = databaseManager.getSelectedEntry(configId, version);
		file.ifPresent(panel::setFields);
		panel.setAutoUpdate(databaseManager.isAutoUpdate(configId, version));
	}


	/**
	 * Mark config version for automatic updates and broadcast the config to all available bots once.
	 * If the automatic update is enabled, configId and version are stored for autoUpdates in the ConfigFileDatabase.
	 *
	 * @param configId unique config identifier
	 * @param version  current version number of the config
	 * @param update   true, if automatic update should be enabled
	 */
	private void onEnableAutoUpdate(int configId, Integer version, boolean update)
	{
		databaseManager.setAutoUpdateFor(configId, version, update);
		if (update)
		{
			databaseManager.getSelectedEntry(configId, version).ifPresent(this::saveToAll);
		}
	}


	private void setTabs(Map<Integer, Map<Integer, Map<String, Object>>> database)
	{
		savedConfigsPanel.clearTabs();
		Map<Integer, String> configIds = new HashMap<>();
		Map<Integer, List<Integer>> idVersionsMap = new HashMap<>();
		for (Map.Entry<Integer, Map<Integer, Map<String, Object>>> config : database.entrySet())
		{
			List<Integer> versions = new ArrayList<>();
			for (Map.Entry<Integer, Map<String, Object>> versionedConf : config.getValue().entrySet())
			{
				configIds.put(config.getKey(), (String) versionedConf.getValue().get("name"));
				versions.add(versionedConf.getKey());
			}
			versions.sort(Comparator.reverseOrder());
			idVersionsMap.put(config.getKey(), versions);
		}
		for (Map.Entry<Integer, String> entry : configIds.entrySet())
		{
			int id = entry.getKey();
			final SavedConfigFilePanel panel = new SavedConfigFilePanel(idVersionsMap.get(entry.getKey()));
			setListener(id, panel);
			savedConfigsPanel.addTab(entry.getValue(), panel);
			panels.put(id, panel);
			onVersionSelected(id, idVersionsMap.get(id).getFirst());
		}
	}


	private void setListener(int configId, SavedConfigFilePanel panel)
	{
		panel.getConfigVersion().addActionListener(new VersionSelectedActionListener(configId));
		panel.getDelete()
				.addActionListener(ae -> onDelete(configId, (Integer) panel.getConfigVersion().getSelectedItem()));

		panel.getSave().addActionListener(ae -> {
			panel.parseValues();
			onSave(panel.getFile());
		});

		panel.getAutoUpdate().addItemListener(
				ie -> onEnableAutoUpdate(
						configId, (Integer) panel.getConfigVersion().getSelectedItem(),
						panel.getAutoUpdate().getModel().isSelected()
				));
	}


	private void saveToAll(final ConfigFile file)
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.ifPresent(mgr ->
						mgr.getBots().values().forEach(b -> b.sendConfigFile(file))
				);
	}


	@Override
	public void onSaveToFile(ConfigFile file)
	{
		databaseManager.addEntry(file);
		setTabs(databaseManager.getAllSavedConfigs());
	}


	private class VersionSelectedActionListener implements java.awt.event.ActionListener
	{
		private int configId;


		public VersionSelectedActionListener(int configId)
		{
			super();
			this.configId = configId;
		}


		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Integer version = (Integer) ((JComboBox<Integer>) e.getSource()).getSelectedItem();
			if (version != null && version > -1)
			{
				onVersionSelected(configId, version);
			}
		}
	}
}
