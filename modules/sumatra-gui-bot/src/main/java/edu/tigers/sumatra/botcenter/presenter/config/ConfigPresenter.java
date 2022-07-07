/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.presenter.config;

import edu.tigers.sumatra.botcenter.view.config.BotConfigPanel;
import edu.tigers.sumatra.botcenter.view.config.IBotConfigPanelObserver;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.botmanager.configs.IConfigFileDatabaseObserver;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

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
	private TigerBot bot;


	public ConfigPresenter(final BotConfigPanel configPanel, ConfigFileDatabaseManager database)
	{
		this.configPanel = configPanel;
		this.configPanel.addObserver(this);
		this.database = database;
		this.database.addObserver(this);
	}


	public void setBot(final TigerBot bot)
	{
		this.bot = bot;
	}


	private Optional<TigerBot> getBot()
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

		configPanel.addConfigFile(cfgFile,
				database.getSelectedEntry(cfgFile.getConfigId(), cfgFile.getVersion()).orElse(null));
	}


	private void newCommandConfigItemDesc(final ACommand cmd)
	{
		TigerConfigItemDesc desc = (TigerConfigItemDesc) cmd;

		ConfigFile cfgFile = files.get(desc.getConfigId());
		if (cfgFile == null)
		{
			return;
		}

		log.debug("Loading description {} of config {} ({}) with {} element", desc.getName(), desc.getName(),
				desc.getConfigId(), desc.getElement());
		cfgFile.setItemDesc(desc);

		if (cfgFile.isComplete())
		{
			log.debug("Config file descriptions complete for {} ({}), reading config", desc.getName(), desc.getConfigId());
			getBot().ifPresent(b -> b.execute(new TigerConfigRead(desc.getConfigId())));
		} else
		{
			log.debug("Config file descriptions incomplete, requesting next");
			getBot().ifPresent(b -> b.execute(cfgFile.getNextRequest()));
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

		ConfigFile cfgFile = new ConfigFile(structure);
		files.put(structure.getConfigId(), cfgFile);

		bot.execute(cfgFile.getNextRequest());
	}


	@Override
	public void onQueryFileList()
	{
		log.debug("Requesting file list");
		getBot().ifPresent(b -> b.execute(new TigerConfigQueryFileList()));
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
		getBot().ifPresent(b -> b.execute(file.getWriteCmd()));
	}


	@Override
	public void onSaveToAll(final ConfigFile file)
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.ifPresent(b -> b.broadcast(file.getWriteCmd()));
	}


	@Override
	public void onRefresh(final ConfigFile file)
	{
		log.debug("Request configs for {} ({})", file.getName(), file.getConfigId());
		getBot().ifPresent(b -> b.execute(new TigerConfigRead(file.getConfigId())));
	}


	@Override
	public void onConfigFileAdded(final ConfigFile file)
	{
		configPanel.updateSavedValues(file);
	}


	@Override
	public void onConfigFileRemoved(final int configId, final int version)
	{
		configPanel.removeSavedValues(configId, version);
	}
}
