/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.lookandfeel.ILookAndFeelStateObserver;
import edu.tigers.sumatra.lookandfeel.LookAndFeelStateAdapter;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABerkeleyPersistence;
import edu.tigers.sumatra.persistence.ARecordManager;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
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
	private static final Logger log = Logger.getLogger(MainPresenter.class.getName());
	
	private static final String LAST_LAYOUT_FILENAME = "last.ly";
	private static final String LAYOUT_DEFAULT = "default.ly";
	private static final String KEY_LAYOUT_PROP = MainPresenter.class.getName() + ".layout";
	private static final String KEY_LAF_PROP = MainPresenter.class.getName() + ".lookAndFeel";
	
	private final MainFrame mainFrame;
	
	private ASkillSystem skillSystem = null;
	private AAgent agent = null;
	private RecordManagerObserver recordManagerObserver;
	
	
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
		
		final ToolbarPresenter toolbarPresenter = new ToolbarPresenter(mainFrame.getToolbar());
		
		mainFrame.getToolbar().addObserver(toolbarPresenter);
		mainFrame.getToolbar().addObserver(this);
		
		loadConfig();
		refreshModuliItems();
		
		GlobalShortcuts.register(EShortcut.MATCH_LAYOUT, () -> onLoadLayout("match.ly"));
		GlobalShortcuts.register(EShortcut.TIMEOUT_LAYOUT, () -> onLoadLayout("timeout.ly"));
		GlobalShortcuts.register(EShortcut.DEFAULT_LAYOUT, () -> onLoadLayout("default.ly"));
		
		ModuliStateAdapter.getInstance().addObserver(this);
		
		mainFrame.activate();
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));
		
		log.trace("Created MainPresenter");
	}
	
	
	private void refreshModuliItems()
	{
		final ArrayList<String> filenames = new ArrayList<>();
		
		// --- read all config-files from config-folder ---
		final File dir = new File(SumatraModel.MODULI_CONFIG_PATH);
		final File[] fileList = dir.listFiles();
		if (fileList != null)
		{
			for (final File file : fileList)
			{
				if ((file != null) && !file.isHidden() && !file.getName().startsWith("."))
				{
					filenames.add(file.getName());
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
				start();
				break;
			case NOT_LOADED:
			case RESOLVED:
				stop();
				break;
		}
	}
	
	
	private void stop()
	{
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_HALT);
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_START);
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_STOP);
		
		mainFrame.setModuliMenuEnabled(true);
		deinitRecordManagerBinding();
	}
	
	
	private void start()
	{
		loadSkillsystem();
		loadAgents();
		loadRefereeShortcuts();
		loadRefboxShortcuts();
		loadBotManagerShortcuts();
		mainFrame.setModuliMenuEnabled(false);
		initRecordManagerBinding();
	}
	
	
	private void loadAgents()
	{
		try
		{
			agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
	}
	
	
	private void loadSkillsystem()
	{
		try
		{
			skillSystem = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get skillsystem module", err);
		}
	}
	
	
	private void loadRefereeShortcuts()
	{
		try
		{
			final AReferee refBox = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			
			GlobalShortcuts.register(EShortcut.REFEREE_HALT,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.HALT)));
			GlobalShortcuts.register(EShortcut.REFEREE_STOP,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.STOP)));
			GlobalShortcuts.register(EShortcut.REFEREE_START,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.NORMAL_START)));
		} catch (final ModuleNotFoundException err)
		{
			log.error("RefBox Module not found", err);
		}
	}
	
	
	private void loadRefboxShortcuts()
	{
		try
		{
			final AReferee refBox = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			
			GlobalShortcuts.register(EShortcut.REFBOX_HALT,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.HALT)));
			GlobalShortcuts.register(EShortcut.REFBOX_STOP,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.STOP)));
			GlobalShortcuts.register(EShortcut.REFBOX_START_NORMAL,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.NORMAL_START)));
			GlobalShortcuts.register(EShortcut.REFBOX_START_FORCE,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.FORCE_START)));
			GlobalShortcuts.register(EShortcut.REFBOX_KICKOFF_YELLOW,
					() -> refBox
							.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.PREPARE_KICKOFF_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_KICKOFF_BLUE,
					() -> refBox
							.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.PREPARE_KICKOFF_BLUE)));
			GlobalShortcuts.register(EShortcut.REFBOX_INDIRECT_YELLOW,
					() -> refBox
							.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.INDIRECT_FREE_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_INDIRECT_BLUE,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.INDIRECT_FREE_BLUE)));
			GlobalShortcuts.register(EShortcut.REFBOX_DIRECT_YELLOW,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.DIRECT_FREE_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_DIRECT_BLUE,
					() -> refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(Command.DIRECT_FREE_BLUE)));
		} catch (final ModuleNotFoundException err)
		{
			log.error("RefBox Module not found", err);
		}
	}
	
	
	private void loadBotManagerShortcuts()
	{
		try
		{
			final ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			GlobalShortcuts.register(EShortcut.CHARGE_ALL_BOTS, () -> {
				if (botManager != null)
				{
					botManager.chargeAll();
				}
			});
			
			GlobalShortcuts.register(EShortcut.DISCHARGE_ALL_BOTS, () -> {
				if (botManager != null)
				{
					botManager.dischargeAll();
				}
			});
		} catch (final ModuleNotFoundException err)
		{
			log.error("botmanager Module not found", err);
		}
	}
	
	
	private void initRecordManagerBinding()
	{
		try
		{
			ARecordManager recordManager = (ARecordManager) SumatraModel.getInstance().getModule(ARecordManager.MODULE_ID);
			recordManagerObserver = new RecordManagerObserver();
			recordManager.addObserver(recordManagerObserver);
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
	}
	
	
	private void deinitRecordManagerBinding()
	{
		try
		{
			ARecordManager recordManager = (ARecordManager) SumatraModel.getInstance().getModule(ARecordManager.MODULE_ID);
			recordManager.removeObserver(recordManagerObserver);
			recordManagerObserver = null;
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
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
		SumatraModel.getInstance().saveUserProperties();
	}
	
	
	@Override
	public void onSelectLookAndFeel(final LookAndFeelInfo info)
	{
		try
		{
			String currentLafName;
			if (info == null)
			{
				currentLafName = UIManager.getSystemLookAndFeelClassName();
			} else
			{
				currentLafName = info.getClassName();
			}
			
			setCurrentLookAndFeel(currentLafName);
			UIManager.setLookAndFeel(currentLafName);
		} catch (final ClassNotFoundException
				| InstantiationException
				| IllegalAccessException
				| UnsupportedLookAndFeelException err)
		{
			log.error("Could not select look and feel.", err);
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
		if (agent != null)
		{
			agent.changeMode(EAIControlState.EMERGENCY_MODE);
		}
		if (skillSystem != null)
		{
			skillSystem.emergencyStop();
		}
	}
	
	private static class RecordManagerObserver implements IRecordObserver
	{
		@Override
		public void onStartStopRecord(final boolean recording)
		{
			// nothing to do here
		}
		
		
		@Override
		public void onViewReplay(final ABerkeleyPersistence persistence, final long startTime)
		{
			new ReplayPresenter().start(persistence, startTime);
		}
	}
}
