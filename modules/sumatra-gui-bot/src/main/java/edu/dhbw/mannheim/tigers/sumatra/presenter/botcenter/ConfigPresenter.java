/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotConfigPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotConfigPanel.IBotConfigPanelObserver;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigWrite;


/**
 * Presenter for bot config logic.
 * 
 * @author AndreR
 */
public class ConfigPresenter implements IBotConfigPanelObserver
{
	private final BotConfigPanel				configPanel;
	private ABot									bot;
	private ABotManager							botManager	= null;
	private final Map<Integer, ConfigFile>	files			= new HashMap<>();
	private static final Logger				log			= Logger.getLogger(ConfigPresenter.class.getName());
	
	
	/**
	 * @param configPanel
	 * @param bot
	 */
	public ConfigPresenter(final BotConfigPanel configPanel, final ABot bot)
	{
		this.configPanel = configPanel;
		this.bot = bot;
		
		this.configPanel.addObserver(this);
	}
	
	
	/**
	 * @param bot
	 */
	public void setBot(final ABot bot)
	{
		this.bot = bot;
	}
	
	
	/**
	 * @param botManager
	 */
	public void setBotManager(final ABotManager botManager)
	{
		this.botManager = botManager;
	}
	
	
	/**
	 * @param cmd
	 */
	public void onNewCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_CONFIG_FILE_STRUCTURE:
				newCommandConfigFileStructure(cmd);
				break;
			case CMD_CONFIG_ITEM_DESC:
				newCommandConfigItemDesc(cmd);
				break;
			case CMD_CONFIG_READ:
				newCommandConfigRead(cmd);
				break;
			default:
				break;
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
		
		configPanel.addConfigFile(cfgFile);
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
		
		if (cfgFile.isComplete())
		{
			bot.execute(new TigerConfigRead(desc.getConfigId()));
		} else
		{
			bot.execute(cfgFile.getNextRequest());
		}
	}
	
	
	private void newCommandConfigFileStructure(final ACommand cmd)
	{
		TigerConfigFileStructure structure = (TigerConfigFileStructure) cmd;
		
		if (files.remove(structure.getConfigId()) != null)
		{
			configPanel.removeConfigFile(structure.getConfigId());
		}
		
		ConfigFile cfgFile = new ConfigFile(structure);
		files.put(structure.getConfigId(), cfgFile);
		
		bot.execute(cfgFile.getNextRequest());
	}
	
	
	@Override
	public void onQueryFileList()
	{
		bot.execute(new TigerConfigQueryFileList());
	}
	
	
	@Override
	public void onSave(final ConfigFile file)
	{
		bot.execute(file.getWriteCmd());
	}
	
	
	@Override
	public void onSaveToAll(final ConfigFile file)
	{
		if (botManager == null)
		{
			return;
		}
		
		for (ABot abot : botManager.getAllBots().values())
		{
			abot.execute(file.getWriteCmd());
		}
	}
	
	
	@Override
	public void onRefresh(final ConfigFile file)
	{
		bot.execute(new TigerConfigRead(file.getConfigId()));
	}
	
	/**
	 * Configuration file.
	 */
	public final class ConfigFile
	{
		private final TigerConfigFileStructure	structure;
		private String									name		= null;
		private final List<String>					names		= new ArrayList<>();
		private List<String>							values	= new ArrayList<>();
		
		
		/**
		 * @param structure
		 */
		public ConfigFile(final TigerConfigFileStructure structure)
		{
			this.structure = structure;
		}
		
		
		/**
		 * Check if all item names have been retrieved.
		 * 
		 * @return
		 */
		public boolean isComplete()
		{
			return structure.getElements().size() == names.size();
		}
		
		
		/**
		 * @return
		 */
		public String getName()
		{
			return name;
		}
		
		
		/**
		 * @return
		 */
		public int getConfigId()
		{
			return structure.getConfigId();
		}
		
		
		/**
		 * Get next item description request to complete this file.
		 * 
		 * @return
		 */
		public TigerConfigItemDesc getNextRequest()
		{
			if (name == null)
			{
				return new TigerConfigItemDesc(structure.getConfigId(), TigerConfigItemDesc.CONFIG_ITEM_FILE_NAME);
			}
			
			if (isComplete())
			{
				return null;
			}
			
			return new TigerConfigItemDesc(structure.getConfigId(), names.size());
		}
		
		
		/**
		 * Add an item description to this file.
		 * 
		 * @param desc
		 */
		public void setItemDesc(final TigerConfigItemDesc desc)
		{
			if (desc.getElement() == TigerConfigItemDesc.CONFIG_ITEM_FILE_NAME)
			{
				name = desc.getName();
				return;
			}
			
			if (names.size() != desc.getElement())
			{
				log.warn("Element mismatch: " + names + "(" + desc.getName() + ") " + names.size() + "!="
						+ desc.getElement());
				return;
			}
			
			names.add(desc.getName());
		}
		
		
		/**
		 * Set config values.
		 * 
		 * @param read
		 */
		public void setValues(final TigerConfigRead read)
		{
			values = read.getData(structure);
		}
		
		
		/**
		 * Retrieve write command from current values.
		 * 
		 * @return
		 */
		public TigerConfigWrite getWriteCmd()
		{
			TigerConfigWrite write = new TigerConfigWrite(structure.getConfigId());
			write.setData(structure, values);
			
			return write;
		}
		
		
		/**
		 * @return the names
		 */
		public List<String> getNames()
		{
			return names;
		}
		
		
		/**
		 * @return the values
		 */
		public List<String> getValues()
		{
			return values;
		}
	}
}