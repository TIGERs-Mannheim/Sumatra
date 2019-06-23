/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.commons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * A generic implementation of a config selection menu
 * 
 * @author Gero
 */
public class ConfigControlMenu implements IModuliStateObserver, IConfigObserver, ILookAndFeelStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log						= Logger.getLogger(ConfigControlMenu.class.getName());
	
	private final String							configKey;
	
	private final SumatraModel					model						= SumatraModel.getInstance();
	private final ModuliStateAdapter			moduliStateAdapter	= ModuliStateAdapter.getInstance();
	/** Needed to distinguish between the first call to {@link #onModuliStateChanged(ModulesState)} and later ones */
	private boolean								alreadyResolved		= false;
	private AConfigManager						manager;
	
	private final JMenu							configMenu;
	private final Map<String, JMenuItem>	configs					= new LinkedHashMap<String, JMenuItem>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param title
	 * @param configKey
	 */
	public ConfigControlMenu(String title, String configKey)
	{
		this.configKey = configKey;
		
		// Menu
		configMenu = new JMenu(title);
		final JMenuItem saveConfig = new JMenuItem("Save");
		saveConfig.addActionListener(new SaveConfig());
		
		configMenu.add(saveConfig);
		configMenu.addSeparator();
		
		// Register as observer
		moduliStateAdapter.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
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
				if (!alreadyResolved)
				{
					try
					{
						manager = (AConfigManager) model.getModule(AConfigManager.MODULE_ID);
						manager.registerObserverAt(configKey, this);
					} catch (final ModuleNotFoundException err)
					{
						log.error("Unable to get ConfigManager, can't setup config menu: ", err);
					}
					
					alreadyResolved = true;
				}
				
				updateConfigMenu();
				break;
			
			case NOT_LOADED:
				// If not the first time
				if (manager != null)
				{
					alreadyResolved = false;
					manager.unregisterObserverAt(configKey, this);
				}
				break;
			case ACTIVE:
				break;
			default:
				break;
		}
	}
	
	
	private void updateConfigMenu()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Clear current menu
				for (final JMenuItem item : configs.values())
				{
					configMenu.remove(item);
				}
				configs.clear();
				
				
				// Build menu
				final List<String> files = manager.getAvailableConfigs(configKey);
				final String selectedFileName = manager.getLoadedFileName(configKey);
				
				final ButtonGroup group = new ButtonGroup();
				for (final String name : files)
				{
					final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
					if (selectedFileName.equals(name))
					{
						item.setSelected(true);
					}
					group.add(item);
					
					item.addActionListener(new LoadConfig(name));
					
					configs.put(name, item);
					configMenu.add(item);
				}
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class SaveConfig implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Ask for filename
			final String currentFileName = manager.getLoadedFileName(configKey);
			final Object result = JOptionPane.showInputDialog(null, "Please specify the name of the config file:",
					currentFileName);
			if (result == null)
			{
				// Aborted.
				return;
			}
			
			final String newFileName = result.toString();
			
			// Save...
			manager.saveConfig(configKey, newFileName);
			
			// Update menu if new config available
			if (!currentFileName.equals(newFileName))
			{
				updateConfigMenu();
			}
		}
	}
	
	
	protected class LoadConfig implements ActionListener
	{
		private final String	filename;
		
		
		/**
		 * @param filename
		 */
		public LoadConfig(String filename)
		{
			this.filename = filename;
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			manager.loadConfig(configKey, filename);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigObserver ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onLoad(Configuration newConfig)
	{
		updateConfigMenu();
	}
	
	
	@Override
	public void onReload(Configuration freshConfig)
	{
		updateConfigMenu();
	}
	
	
	// --------------------------------------------------------------------------
	// --- Look-and-Feel --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.updateComponentTreeUI(configMenu);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the configMenu
	 */
	public JMenu getConfigMenu()
	{
		return configMenu;
	}
}
