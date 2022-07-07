/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.IModuliStateObserver;
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
import edu.tigers.sumatra.view.AboutDialog;
import edu.tigers.sumatra.view.MainFrame;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import lombok.extern.log4j.Log4j2;

import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * The UI presenter for the main window of Sumatra.
 */
@Log4j2
public class MainPresenter extends AMainPresenter implements IModuliStateObserver,
		ILookAndFeelStateObserver, IToolbarObserver
{
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
		GlobalShortcuts.removeAllForComponent(mainFrame);

		deinitRecordManagerBinding();
	}


	private void start()
	{
		loadRefereeShortcuts();
		loadBotManagerShortcuts();
		loadAutorefShortcuts();
		initRecordManagerBinding();
	}


	private void loadAutorefShortcuts()
	{
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(autoRefModule ->
				GlobalShortcuts.add(
						"Toggle AutoRef",
						mainFrame,
						() -> autoRefModule.changeMode(autoRefModule.getMode().next()),
						KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)
				));
	}


	private void loadRefereeShortcuts()
	{
		SumatraModel.getInstance().getModuleOpt(AReferee.class).ifPresent(this::addRefereeShortcuts);
	}


	private void addRefereeShortcuts(AReferee referee)
	{
		GlobalShortcuts.add(
				"Send HALT",
				mainFrame,
				() -> referee.sendGameControllerEvent(GcEventFactory.command(Command.HALT)),
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)
		);
		GlobalShortcuts.add(
				"Send STOP",
				mainFrame,
				() -> referee.sendGameControllerEvent(GcEventFactory.command(Command.STOP)),
				KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
		);
		GlobalShortcuts.add(
				"Send FORCE_START",
				mainFrame,
				() -> referee.sendGameControllerEvent(GcEventFactory.command(Command.FORCE_START)),
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)
		);
	}


	private void addBotManagerShortcuts(TigersBotManager botManager)
	{
		GlobalShortcuts.add(
				"Charge all bots",
				mainFrame,
				botManager::chargeAll,
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)
		);
		GlobalShortcuts.add(
				"Discharge all bots",
				mainFrame,
				botManager::dischargeAll,
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)
		);
	}


	private void loadBotManagerShortcuts()
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class).ifPresent(this::addBotManagerShortcuts);
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
