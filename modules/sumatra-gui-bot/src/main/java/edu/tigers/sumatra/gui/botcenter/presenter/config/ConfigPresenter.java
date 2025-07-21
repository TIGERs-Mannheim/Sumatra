/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.presenter.config;

import edu.tigers.sumatra.botmanager.ACommandBasedBotManager;
import edu.tigers.sumatra.botmanager.bots.CommandBasedBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.botmanager.configs.IConfigFileDatabaseObserver;
import edu.tigers.sumatra.gui.botcenter.view.config.BotConfigPanel;
import edu.tigers.sumatra.gui.botcenter.view.config.IBotConfigPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Presenter for bot config logic.
 */
@Log4j2
public class ConfigPresenter implements IBotConfigPanelObserver, IConfigFileDatabaseObserver
{
	private final BotConfigPanel configPanel;
	private final Map<Integer, ConfigFile> files = new HashMap<>();

	private final ConfigFileDatabaseManager database;
	private CommandBasedBot bot;


	public ConfigPresenter(final BotConfigPanel configPanel, ConfigFileDatabaseManager database)
	{
		this.configPanel = configPanel;
		this.configPanel.addObserver(this);
		this.database = database;
		this.database.addObserver(this);
	}


	public void setBot(final CommandBasedBot bot)
	{
		this.bot = bot;
	}


	private Optional<CommandBasedBot> getBot()
	{
		return Optional.ofNullable(bot);
	}


	public void dispose()
	{
		this.configPanel.removeObserver(this);
	}


	public void onNewCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_CONFIG_FILE_STRUCTURE -> newCommandConfigFileStructure(cmd);
			case CMD_CONFIG_ITEM_DESC -> newCommandConfigItemDesc(cmd);
			case CMD_CONFIG_READ -> newCommandConfigRead(cmd);
		}
	}


	private void newCommandConfigRead(final ACommand cmd)
	{
		TigerConfigRead read = (TigerConfigRead) cmd;

		ConfigFile cfgFile = files.get(read.getConfigId());
		if (cfgFile == null)
		{
			return;
		}

		cfgFile.setValues(read);

		log.info("Config complete:" + cfgFile.getName());

		SwingUtilities.invokeLater(() -> configPanel.addConfigFile(
				cfgFile,
				database.getSelectedEntry(cfgFile.getConfigId(), cfgFile.getVersion()).orElse(null)
		));

		// Continue with next incomplete config, if any
		files.values().stream()
				.filter(c -> !c.isComplete())
				.findFirst()
				.ifPresent(c -> getBot().ifPresent(b -> b.sendCommand(c.getNextRequest())));
	}


	private void newCommandConfigItemDesc(final ACommand cmd)
	{
		TigerConfigItemDesc desc = (TigerConfigItemDesc) cmd;

		ConfigFile cfgFile = files.get(desc.getConfigId());
		if (cfgFile == null)
		{
			return;
		}

		cfgFile.setItemDesc(desc);
		log.debug(
				"Loading description {} of config {} ({}) with element {}", desc.getName(), cfgFile.getName(),
				desc.getConfigId(), desc.getElement()
		);

		if (cfgFile.isComplete())
		{
			log.debug("Config file descriptions complete for {} ({}), reading config", desc.getName(), desc.getConfigId());
			getBot().ifPresent(b -> b.sendCommand(new TigerConfigRead(desc.getConfigId())));
		} else
		{
			log.debug("Config file descriptions incomplete, requesting next");
			getBot().ifPresent(b -> b.sendCommand(cfgFile.getNextRequest()));
		}
	}


	private void newCommandConfigFileStructure(final ACommand cmd)
	{
		TigerConfigFileStructure structure = (TigerConfigFileStructure) cmd;
		log.debug("Got new file structure for config {}", structure.getConfigId());

		if (files.remove(structure.getConfigId()) != null)
		{
			log.debug("Reloading config {}", structure.getConfigId());
			configPanel.removeConfigFile(structure.getConfigId());
		}

		boolean anyIncompleteFile = files.values().stream()
				.anyMatch(c -> !c.isComplete());

		ConfigFile cfgFile = new ConfigFile(structure);
		files.put(structure.getConfigId(), cfgFile);

		if (anyIncompleteFile)
		{
			// Finish other incomplete file(s) first
			return;
		}

		bot.sendCommand(cfgFile.getNextRequest());
	}


	@Override
	public void onQueryFileList()
	{
		log.debug("Requesting file list");
		onClearFileList();
		getBot().ifPresent(b -> b.sendCommand(new TigerConfigQueryFileList()));
	}


	@Override
	public void onClearFileList()
	{
		files.keySet().forEach(configPanel::removeConfigFile);
		files.clear();
	}


	@Override
	public void onSave(final ConfigFile file)
	{
		getBot().ifPresent(b -> b.sendCommand(file.getWriteCmd()));
	}


	@Override
	public void onSaveToAll(final ConfigFile file)
	{
		SumatraModel.getInstance().getModuleOpt(ACommandBasedBotManager.class)
				.ifPresent(mgr ->
						mgr.getBots().values().forEach(b ->
								b.sendCommand(file.getWriteCmd())));
	}


	@Override
	public void onRefresh(final ConfigFile file)
	{
		log.debug("Request configs for {} ({})", file.getName(), file.getConfigId());
		getBot().ifPresent(b -> b.sendCommand(new TigerConfigRead(file.getConfigId())));
	}


	@Override
	public void onConfigFileAdded(final ConfigFile file)
	{
		SwingUtilities.invokeLater(() -> configPanel.updateSavedValues(file));
	}


	@Override
	public void onConfigFileRemoved(final int configId, final int version)
	{
		SwingUtilities.invokeLater(() -> configPanel.removeSavedValues(configId, version));
	}
}
