/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.lookandfeel.ILookAndFeelStateObserver;
import edu.tigers.sumatra.lookandfeel.LookAndFeelStateAdapter;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.replay.AiReplayPresenter;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.view.AboutDialog;
import edu.tigers.sumatra.view.MainFrame;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


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
	private static final Logger log = LogManager.getLogger(MainPresenter.class.getName());

	private static final String LAST_LAYOUT_FILENAME = "last.ly";
	private static final String LAYOUT_DEFAULT = "default.ly";
	private static final String KEY_LAYOUT_PROP = MainPresenter.class.getName() + ".layout";
	private static final String KEY_LAF_PROP = MainPresenter.class.getName() + ".lookAndFeel";

	private final MainFrame mainFrame;

	private final RecordManagerObserver recordManagerObserver = new RecordManagerObserver();


	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 */
	public MainPresenter()
	{
		super(new MainFrame());
		log.trace("Create MainPresenter");

		onSelectLookAndFeel(getLookAndFeel());

		mainFrame = (MainFrame) getMainFrame();
		mainFrame.initializeViews();

		LookAndFeelStateAdapter.getInstance().addObserver(this);

		final ToolbarPresenter toolbarPresenter = new ToolbarPresenter(mainFrame.getToolbar());

		mainFrame.getToolbar().addObserver(toolbarPresenter);
		mainFrame.getToolbar().addObserver(this);

		loadConfig();
		refreshModuliItems();

		ModuliStateAdapter.getInstance().addObserver(this);

		mainFrame.activate();
		mainFrame.updateViewMenu();

		Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));

		log.trace("Created MainPresenter");
	}


	private void refreshModuliItems()
	{
		final List<String> filenames = new ArrayList<>();

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
		Collections.sort(filenames);

		mainFrame.setMenuModuliItems(filenames);
		mainFrame.selectModuliItem(SumatraModel.getInstance().getCurrentModuliConfig());
	}


	private LookAndFeelInfo getLookAndFeel()
	{
		final LookAndFeelInfo[] lafInfos = UIManager.getInstalledLookAndFeels();
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();

		String lookAndFeelName = userSettings.getProperty(KEY_LAF_PROP,
				UIManager.getSystemLookAndFeelClassName());

		for (final LookAndFeelInfo info : lafInfos)
		{
			if (info.getClassName().equals(lookAndFeelName))
			{
				return (info);
			}
		}

		// null -> System default
		return (null);
	}


	private void loadConfig()
	{
		// ## Init moduli config
		final String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
		mainFrame.selectModuliItem(moduliConfig);

		// ## Init Look-and-Feel
		final LookAndFeelInfo lafInfo = getLookAndFeel();
		onSelectLookAndFeel(lafInfo);
		if (lafInfo != null)
		{
			log.trace("Loading look and feel: " + lafInfo.getName());
			mainFrame.selectLookAndFeelItem(lafInfo.getName());
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
			default:
		}
	}


	private void stop()
	{
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_HALT);
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_START);
		GlobalShortcuts.unregisterAll(EShortcut.REFEREE_STOP);
		GlobalShortcuts.unregisterAll(EShortcut.AUTOREF_TOGGLE);

		deinitRecordManagerBinding();
	}


	private void start()
	{
		loadRefereeShortcuts();
		loadRefboxShortcuts();
		loadBotManagerShortcuts();
		loadAutorefShortcuts();
		initRecordManagerBinding();
	}


	private void loadAutorefShortcuts()
	{
		GlobalShortcuts.register(EShortcut.AUTOREF_TOGGLE, () -> {
			AutoRefModule autoref = SumatraModel.getInstance().getModule(AutoRefModule.class);
			EAutoRefMode nextMode = autoref.getMode().next();
			autoref.changeMode(nextMode);
		});
	}


	private void loadRefereeShortcuts()
	{
		try
		{
			final AReferee refBox = SumatraModel.getInstance().getModule(AReferee.class);

			GlobalShortcuts.register(EShortcut.REFEREE_HALT,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.HALT)));
			GlobalShortcuts.register(EShortcut.REFEREE_STOP,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.STOP)));
			GlobalShortcuts.register(EShortcut.REFEREE_START,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.NORMAL_START)));
		} catch (final ModuleNotFoundException err)
		{
			log.error("Referee Module not found", err);
		}
	}


	private void loadRefboxShortcuts()
	{
		try
		{
			final AReferee refBox = SumatraModel.getInstance().getModule(AReferee.class);

			GlobalShortcuts.register(EShortcut.REFBOX_HALT,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.HALT)));
			GlobalShortcuts.register(EShortcut.REFBOX_STOP,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.STOP)));
			GlobalShortcuts.register(EShortcut.REFBOX_START_NORMAL,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.NORMAL_START)));
			GlobalShortcuts.register(EShortcut.REFBOX_START_FORCE,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.FORCE_START)));
			GlobalShortcuts.register(EShortcut.REFBOX_KICKOFF_YELLOW,
					() -> refBox
							.sendGameControllerEvent(GcEventFactory.command(Command.PREPARE_KICKOFF_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_KICKOFF_BLUE,
					() -> refBox
							.sendGameControllerEvent(GcEventFactory.command(Command.PREPARE_KICKOFF_BLUE)));
			GlobalShortcuts.register(EShortcut.REFBOX_INDIRECT_YELLOW,
					() -> refBox
							.sendGameControllerEvent(GcEventFactory.command(Command.INDIRECT_FREE_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_INDIRECT_BLUE,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.INDIRECT_FREE_BLUE)));
			GlobalShortcuts.register(EShortcut.REFBOX_DIRECT_YELLOW,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.DIRECT_FREE_YELLOW)));
			GlobalShortcuts.register(EShortcut.REFBOX_DIRECT_BLUE,
					() -> refBox.sendGameControllerEvent(GcEventFactory.command(Command.DIRECT_FREE_BLUE)));
		} catch (final ModuleNotFoundException err)
		{
			log.error("Referee Module not found", err);
		}
	}


	private void loadBotManagerShortcuts()
	{
		if (SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{

			GlobalShortcuts.register(EShortcut.CHARGE_ALL_BOTS,
					() -> SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
							.ifPresent(TigersBotManager::chargeAll));
			GlobalShortcuts.register(EShortcut.DISCHARGE_ALL_BOTS,
					() -> SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
							.ifPresent(TigersBotManager::dischargeAll));
		}
	}


	private void initRecordManagerBinding()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			SumatraModel.getInstance().getModule(RecordManager.class)
					.addObserver(recordManagerObserver);
		}
	}


	private void deinitRecordManagerBinding()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			SumatraModel.getInstance().getModule(RecordManager.class)
					.removeObserver(recordManagerObserver);
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
	public void onEmergencyStop()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			SumatraModel.getInstance().getModule(AAgent.class)
					.changeMode(EAIControlState.EMERGENCY_MODE);
		}
		if (SumatraModel.getInstance().isModuleLoaded(ASkillSystem.class))
		{
			SumatraModel.getInstance().getModule(ASkillSystem.class)
					.emergencyStop();
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
		public void onViewReplay(final BerkeleyDb persistence, final long startTime)
		{
			new AiReplayPresenter().start(persistence, startTime);
		}
	}
}
