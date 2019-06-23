/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard, AndreR
 * *********************************************************
 */
package edu.tigers.sumatra;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.lookandfeel.ILookAndFeelStateObserver;
import edu.tigers.sumatra.lookandfeel.LookAndFeelStateAdapter;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.view.AboutDialog;
import edu.tigers.sumatra.view.MainFrame;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;


/**
 * The main presenter for Sumatra.
 * It is the brain of Sumatra.
 * It loads the modules, controls them and interact with
 * the view (GUI).
 * 
 * @author BernhardP, AndreR
 */
public class MainPresenter extends AMainPresenter implements IModuliStateObserver,
		ILookAndFeelStateObserver, IToolbarObserver
{
	private static final Logger		log						= Logger.getLogger(MainPresenter.class.getName());
																			
	private static final String		LAST_LAYOUT_FILENAME	= "last.ly";
	private static final String		LAYOUT_DEFAULT			= "default.ly";
	private static final String		KEY_LAYOUT_PROP		= MainPresenter.class.getName() + ".layout";
	private static final String		KEY_LAF_PROP			= MainPresenter.class.getName() + ".lookAndFeel";
																			
	private final MainFrame				mainFrame;
	private final ToolbarPresenter	toolbarPresenter;
												
	private ASkillSystem					skillSystem				= null;
	private Agent							agentYellow				= null;
	private Agent							agentBlue				= null;
																			
																			
	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 */
	public MainPresenter()
	{
		super(new MainFrame());
		log.trace("Create MainPresenter");
		
		mainFrame = (MainFrame) getMainFrame();
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		toolbarPresenter = new ToolbarPresenter(mainFrame.getToolbar());
		
		mainFrame.getToolbar().addObserver(toolbarPresenter);
		mainFrame.getToolbar().addObserver(this);
		
		loadConfig();
		refreshModuliItems();
		
		GlobalShortcuts.register(EShortcut.MATCH_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("match.ly");
			}
		});
		
		GlobalShortcuts.register(EShortcut.TIMEOUT_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("timeout.ly");
			}
		});
		
		GlobalShortcuts.register(EShortcut.DEFAULT_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("default.ly");
			}
		});
		
		
		ModuliStateAdapter.getInstance().addObserver(this);
		
		mainFrame.activate();
		
		log.trace("Created MainPresenter");
	}
	
	
	private void refreshModuliItems()
	{
		final ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all config-files from config-folder ---
		final File dir = new File(SumatraModel.MODULI_CONFIG_PATH);
		final File[] fileList = dir.listFiles();
		if (fileList != null)
		{
			for (final File file : fileList)
			{
				if (file != null)
				{
					if (!file.isHidden() && !file.getName().startsWith("."))
					{
						filenames.add(file.getName());
					}
				}
			}
		}
		
		mainFrame.setMenuModuliItems(filenames);
		mainFrame.selectModuliItem(SumatraModel.getInstance().getCurrentModuliConfig());
	}
	
	
	private void loadConfig()
	{
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();
		
		// ## Init moduli config
		final String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
		mainFrame.selectModuliItem(moduliConfig);
		
		// ## Init Look-and-Feel
		final LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		String lookAndFeel = userSettings.getProperty(KEY_LAF_PROP,
				UIManager.getSystemLookAndFeelClassName());
				
		boolean found = false;
		for (final LookAndFeelInfo info : lafs)
		{
			if (info.getClassName().equals(lookAndFeel))
			{
				log.trace("Loading look and feel: " + info.getName());
				onSelectLookAndFeel(info);
				mainFrame.selectLookAndFeelItem(info.getName());
				found = true;
				break;
			}
		}
		
		if (!found)
		{
			// Causes it to select system default
			onSelectLookAndFeel(null);
		}
	}
	
	
	private String getCurrentLookAndFeel()
	{
		return SumatraModel.getInstance().getUserProperty(KEY_LAF_PROP);
	}
	
	
	private void setCurrentLookAndFeel(final String newLookAndFeel)
	{
		SumatraModel.getInstance().setUserProperty(KEY_LAF_PROP, newLookAndFeel);
	}
	
	
	@Override
	protected String getLayoutKey()
	{
		return KEY_LAYOUT_PROP;
	}
	
	
	@Override
	protected String getLastLayoutFile()
	{
		return LAST_LAYOUT_FILENAME;
	}
	
	
	@Override
	protected String getDefaultLayout()
	{
		return LAYOUT_DEFAULT;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					skillSystem = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get skillsystem module");
				}
				try
				{
					agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				
				mainFrame.setModuliMenuEnabled(false);
				break;
			case NOT_LOADED:
			case RESOLVED:
				mainFrame.setModuliMenuEnabled(true);
				break;
		}
	}
	
	
	@Override
	public void onAbout()
	{
		new AboutDialog().setVisible(true);
	}
	
	
	@Override
	public void onExit()
	{
		super.onExit();
		
		if (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE)
		{
			SumatraModel.getInstance().stopModules();
		}
		
		// ### Persist user settings
		SumatraModel.getInstance().saveUserProperties();
		
		// --- exit application ---
		System.exit(0);
	}
	
	
	@Override
	public void onSelectLookAndFeel(final LookAndFeelInfo info)
	{
		try
		{
			String currentLafName = null;
			if (info == null)
			{
				currentLafName = UIManager.getSystemLookAndFeelClassName();
			} else
			{
				currentLafName = info.getClassName();
			}
			
			setCurrentLookAndFeel(currentLafName);
			UIManager.setLookAndFeel(currentLafName);
		} catch (final ClassNotFoundException err)
		{
		} catch (final InstantiationException err)
		{
		} catch (final IllegalAccessException err)
		{
		} catch (final UnsupportedLookAndFeelException err)
		{
		}
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		EventQueue.invokeLater(() -> mainFrame.setLookAndFeel(getCurrentLookAndFeel()));
	}
	
	
	@Override
	public void onStartStopModules()
	{
		// this is done in ToolbarPresenter
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		if (agentYellow != null)
		{
			agentYellow.getAthena().changeMode(EAIControlState.EMERGENCY_MODE);
		}
		if (agentBlue != null)
		{
			agentBlue.getAthena().changeMode(EAIControlState.EMERGENCY_MODE);
		}
		if (skillSystem != null)
		{
			skillSystem.emergencyStop();
		}
	}
}
