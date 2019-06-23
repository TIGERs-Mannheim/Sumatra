/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard, AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import net.infonode.gui.laf.InfoNodeLookAndFeel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.view.main.AboutDialog;
import edu.dhbw.mannheim.tigers.sumatra.view.main.MainFrame;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.IToolbarObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.AICenterView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BallAnalyserView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BotCenterView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BotOverviewView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.ConfigEditorView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.LogView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.OffensiveStrategyView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.RcmView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.RefereeView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.SimulationView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.StatisticsView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.TimerView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.VisualizerView;


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
	private static final Logger	log						= Logger.getLogger(MainPresenter.class.getName());
	
	private static final String	LAST_LAYOUT_FILENAME	= "last.ly";
	private static final String	LAYOUT_DEFAULT			= "default.ly";
	private static final String	KEY_LAYOUT_PROP		= MainPresenter.class.getName() + ".layout";
	
	private final MainFrame			mainFrameGUI			= new MainFrame();
	private ToolbarPresenter		toolbar;
	private ASkillSystem				skillSystem				= null;
	private Agent						agentYellow				= null;
	private Agent						agentBlue				= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 */
	public MainPresenter()
	{
		InfoNodeLookAndFeel.install();
		
		// --- add variable - Listener (stateModules) ---
		ModuliStateAdapter.getInstance().addObserver(this);
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		// --- start GUI ---
		toolbar = new ToolbarPresenter(mainFrameGUI.getToolbar());
		mainFrameGUI.getToolbar().addObserver(toolbar);
		mainFrameGUI.getToolbar().addObserver(this);
		
		mainFrameGUI.addView(new AICenterView(ETeamColor.YELLOW));
		mainFrameGUI.addView(new AICenterView(ETeamColor.BLUE));
		mainFrameGUI.addView(new BotCenterView());
		mainFrameGUI.addView(new ConfigEditorView());
		mainFrameGUI.addView(new LogView(true));
		mainFrameGUI.addView(new RcmView());
		mainFrameGUI.addView(new RefereeView());
		mainFrameGUI.addView(new TimerView());
		mainFrameGUI.addView(new VisualizerView());
		mainFrameGUI.addView(new BotOverviewView());
		mainFrameGUI.addView(new StatisticsView(ETeamColor.YELLOW));
		mainFrameGUI.addView(new StatisticsView(ETeamColor.BLUE));
		mainFrameGUI.addView(new OffensiveStrategyView());
		mainFrameGUI.addView(new SimulationView());
		mainFrameGUI.addView(new BallAnalyserView());
		
		init(mainFrameGUI);
		
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
		
		mainFrameGUI.setMenuModuliItems(filenames);
		mainFrameGUI.selectModuliItem(SumatraModel.getInstance().getCurrentModuliConfig());
	}
	
	
	private void loadConfig()
	{
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();
		
		// ## Init moduli config
		final String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
		mainFrameGUI.selectModuliItem(moduliConfig);
		
		// ## Init Look-and-Feel
		final LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		String lookAndFeel = userSettings.getProperty(AMainPresenter.KEY_LAF_PROP);
		if (lookAndFeel == null)
		{
			log.debug("Unproper Lool-and-Feel, taking System default.");
			lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		}
		
		boolean found = false;
		for (final LookAndFeelInfo info : lafs)
		{
			if (info.getClassName().equals(lookAndFeel))
			{
				log.debug("Loading look and feel: " + info.getName());
				onSelectLookAndFeel(info);
				mainFrameGUI.selectLookAndFeelItem(info.getName());
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
				mainFrameGUI.setModuliMenuEnabled(false);
				break;
			case NOT_LOADED:
			case RESOLVED:
				mainFrameGUI.setModuliMenuEnabled(true);
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
		
		// ### Persist user settings
		final Properties appProps = SumatraModel.getInstance().getUserSettings();
		
		// --- persist application properties ---
		final File uf = SumatraModel.getInstance().getUserPropertiesFile();
		
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(uf);
			appProps.store(out, null);
		} catch (final IOException err)
		{
			log.warn("Could not write to " + uf.getPath() + ", configuration is not saved");
		}
		
		if (out != null)
		{
			try
			{
				out.close();
				log.debug("Saved configuration to: " + uf.getPath());
			} catch (IOException e)
			{
				log.warn("Could not close " + uf.getPath() + ", configuration is not saved");
			}
		}
		
		if (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE)
		{
			SumatraModel.getInstance().stopModules();
		}
		
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
		mainFrameGUI.setLookAndFeel(getCurrentLookAndFeel());
	}
	
	
	@Override
	public void onStartStopModules()
	{
		// this is done in ToolbarPresenter
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		for (ASumatraView sumatraView : mainFrameGUI.getViews())
		{
			if (sumatraView.isInitialized())
			{
				sumatraView.getPresenter().onEmergencyStop();
			}
		}
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
	
	
	// --------------------------------------------------------------------------
	// --- property getter ------------------------------------------------------
	// --------------------------------------------------------------------------
	private String getCurrentLookAndFeel()
	{
		return SumatraModel.getInstance().getUserProperty(AMainPresenter.KEY_LAF_PROP);
	}
	
	
	private void setCurrentLookAndFeel(final String newLookAndFeel)
	{
		SumatraModel.getInstance().setUserProperty(AMainPresenter.KEY_LAF_PROP, newLookAndFeel);
	}
	
	
	@Override
	public void onRecordAndSave(final boolean active)
	{
	}
	
	
	@Override
	public void onRecord(final boolean active)
	{
	}
	
	
	@Override
	protected String getLayoutKey()
	{
		return KEY_LAYOUT_PROP;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public String getLastLayoutFile()
	{
		return LAST_LAYOUT_FILENAME;
	}
	
	
	@Override
	protected String getDefaultLayout()
	{
		return LAYOUT_DEFAULT;
	}
}
