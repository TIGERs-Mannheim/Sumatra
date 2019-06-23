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

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;


/**
 * A generic implementation of a config selection menu
 * 
 * @author Gero
 */
public class ConfigControlMenu implements IConfigObserver, ILookAndFeelStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String							configKey;
	private final JMenu							configMenu;
	private final Map<String, JMenuItem>	configs	= new LinkedHashMap<String, JMenuItem>();
	
	
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
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		ConfigManager.getInstance().registerObserverAt(configKey, this);
		
		updateConfigMenu();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
				final List<String> files = ConfigManager.getInstance().getAvailableConfigs(configKey);
				final String selectedFileName = ConfigManager.getInstance().getLoadedFileName(configKey);
				
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
			final String currentFileName = ConfigManager.getInstance().getLoadedFileName(configKey);
			final Object result = JOptionPane.showInputDialog(null, "Please specify the name of the config file:",
					currentFileName);
			if (result == null)
			{
				// Aborted.
				return;
			}
			
			final String newFileName = result.toString();
			
			// Save...
			ConfigManager.getInstance().saveConfig(configKey, newFileName);
			
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
			ConfigManager.getInstance().loadConfig(configKey, filename);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigObserver ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onLoad(HierarchicalConfiguration newConfig)
	{
		updateConfigMenu();
	}
	
	
	@Override
	public void onReload(HierarchicalConfiguration freshConfig)
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
