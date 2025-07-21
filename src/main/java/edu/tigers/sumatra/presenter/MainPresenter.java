/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter;

import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.AModuliMainPresenter;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.config.ConfigEditorView;
import edu.tigers.sumatra.gui.ai.aicenter.AICenterView;
import edu.tigers.sumatra.gui.ai.botoverview.BotOverviewView;
import edu.tigers.sumatra.gui.ai.offensive.interceptions.OffensiveInterceptionsView;
import edu.tigers.sumatra.gui.ai.offensive.statistics.OffensiveStatisticsView;
import edu.tigers.sumatra.gui.ai.offensive.strategy.OffensiveStrategyView;
import edu.tigers.sumatra.gui.ai.statistics.StatisticsView;
import edu.tigers.sumatra.gui.ai.support.SupportBehaviorsView;
import edu.tigers.sumatra.gui.ai.visualizer.VisualizerAiView;
import edu.tigers.sumatra.gui.ballanalyzer.BallAnalyserView;
import edu.tigers.sumatra.gui.botcenter.BotCenterView;
import edu.tigers.sumatra.gui.botparams.BotParamsView;
import edu.tigers.sumatra.gui.kick.BallKickIdentView;
import edu.tigers.sumatra.gui.log.LogView;
import edu.tigers.sumatra.gui.logfile.LogfileView;
import edu.tigers.sumatra.gui.rcm.RcmView;
import edu.tigers.sumatra.gui.referee.RefereeView;
import edu.tigers.sumatra.gui.replay.view.ReplayLoadMenu.IReplayLoadMenuObserver;
import edu.tigers.sumatra.gui.sim.SimulationView;
import edu.tigers.sumatra.gui.skills.SkillsView;
import edu.tigers.sumatra.gui.timer.TimerView;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.ShortcutsDialog;
import edu.tigers.sumatra.view.AboutDialog;
import edu.tigers.sumatra.view.MainFrame;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import edu.tigers.sumatra.views.ASumatraView;
import lombok.extern.log4j.Log4j2;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * The UI presenter for the main window of Sumatra.
 */
@Log4j2
public class MainPresenter extends AModuliMainPresenter<MainFrame>
		implements IToolbarObserver, IReplayLoadMenuObserver
{
	private static final String KEY_LAF_PROP = MainPresenter.class.getName() + ".lookAndFeel";

	private final ToolbarPresenter toolbarPresenter;


	public MainPresenter()
	{
		super(new MainFrame(), createViews(), "main");
		log.trace("Create MainPresenter");
		toolbarPresenter = new ToolbarPresenter(getMainFrame().getToolbar());

		getMainFrame().getMenuReplay().addObserver(this);
		getMainFrame().getMenuItemSumatraShortcut().addActionListener(e -> new ShortcutsDialog(getMainFrame()));
		getMainFrame().getMenuItemSumatraAbout().addActionListener(e -> new AboutDialog());
		getMainFrame().getToolbar().addObserver(this);

		SwingUtilities.invokeLater(() -> {
			refreshModuliMenu();
			refreshLookAndFeelMenu();
		});

		loadRefereeShortcuts();
		loadBotManagerShortcuts();
		loadAutorefShortcuts();
		init();

		log.trace("Created MainPresenter");
	}


	private static List<ASumatraView> createViews()
	{
		return List.of(
				new LogView(true),
				new AICenterView(),
				new BotCenterView(),
				new ConfigEditorView(),
				new RcmView(),
				new RefereeView(),
				new TimerView(),
				new VisualizerAiView(),
				new BotOverviewView(),
				new StatisticsView(),
				new OffensiveStrategyView(),
				new OffensiveInterceptionsView(),
				new SimulationView(),
				new BallAnalyserView(),
				new AutoRefView(),
				new GameLogView(),
				new BallSpeedView(),
				new LogfileView(),
				new OffensiveStatisticsView(),
				new BotParamsView(),
				new BallKickIdentView(),
				new SupportBehaviorsView(),
				new SkillsView()
		);
	}


	private void refreshModuliMenu()
	{
		Path configPath = SumatraModel.getInstance().getConfigModuliPath();
		try (var files = Files.list(configPath))
		{
			List<String> filenames = files
					.map(Path::getFileName)
					.filter(path -> !path.startsWith("."))
					.map(Path::toString)
					.sorted()
					.toList();
			var menuItems = getMainFrame().setModuliItems(filenames);
			menuItems.forEach((name, item) -> item.addActionListener(e -> SumatraModel.getInstance().startUpAsync(name)));
			getMainFrame().setModuliItemSelected(SumatraModel.getInstance().getCurrentModuliConfig());
		} catch (IOException e)
		{
			log.error("Could not read config files from path: {}", configPath, e);
		}
	}


	private void refreshLookAndFeelMenu()
	{
		var lafInfos = Arrays.stream(UIManager.getInstalledLookAndFeels())
				.collect(Collectors.toMap(LookAndFeelInfo::getClassName, Function.identity()));

		var menuItems = getMainFrame().setLookAndFeelItems(lafInfos.values());
		menuItems.forEach((info, item) -> item.addActionListener(e -> setCurrentLookAndFeel(info)));

		String userLafClassName = SumatraModel.getInstance()
				.getUserProperty(KEY_LAF_PROP, UIManager.getSystemLookAndFeelClassName());
		LookAndFeelInfo info = lafInfos.getOrDefault(
				userLafClassName, lafInfos.get(UIManager.getSystemLookAndFeelClassName()));

		setCurrentLookAndFeel(info);
	}


	private void setCurrentLookAndFeel(final LookAndFeelInfo info)
	{
		log.trace("Loading look and feel: {}", info.getName());
		try
		{
			UIManager.setLookAndFeel(info.getClassName());

			// update visible components
			final int state = getMainFrame().getExtendedState();
			SwingUtilities.updateComponentTreeUI(getMainFrame());
			getMainFrame().setExtendedState(state);
			getMainFrame().setLookAndFeelItemSelected(info.getName());
			SumatraModel.getInstance().setUserProperty(KEY_LAF_PROP, info.getClassName());
		} catch (final ClassNotFoundException
		               | InstantiationException
		               | IllegalAccessException
		               | UnsupportedLookAndFeelException err)
		{
			log.error("Could not select look and feel.", err);
		}
	}


	@Override
	protected void onModuliStarted()
	{
		super.onModuliStarted();
		toolbarPresenter.onModuliStarted();
	}


	@Override
	protected void onModuliStopped()
	{
		super.onModuliStopped();
		toolbarPresenter.onModuliStopped();
	}


	private void loadAutorefShortcuts()
	{
		GlobalShortcuts.add(
				"Toggle AutoRef",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
						.ifPresent(autoRefModule -> autoRefModule.changeMode(autoRefModule.getMode().next())),
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)
		);
	}


	private void loadRefereeShortcuts()
	{
		GlobalShortcuts.add(
				"Send HALT",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(AReferee.class)
						.ifPresent(referee -> referee.sendGameControllerEvent(GcEventFactory.command(Command.HALT))),
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)
		);
		GlobalShortcuts.add(
				"Send STOP",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(AReferee.class)
						.ifPresent(referee -> referee.sendGameControllerEvent(GcEventFactory.command(Command.STOP))),
				KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
		);
		GlobalShortcuts.add(
				"Send FORCE_START",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(AReferee.class)
						.ifPresent(referee -> referee.sendGameControllerEvent(GcEventFactory.command(Command.FORCE_START))),
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)
		);
	}


	private void loadBotManagerShortcuts()
	{
		GlobalShortcuts.add(
				"Charge all bots",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
						.ifPresent(TigersBotManager::chargeAll),
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)
		);
		GlobalShortcuts.add(
				"Discharge all bots",
				getMainFrame(),
				() -> SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
						.ifPresent(TigersBotManager::dischargeAll),
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)
		);
	}


	@Override
	public void onEmergencyStop()
	{
		SumatraModel.getInstance().getModuleOpt(AAgent.class)
				.ifPresent(agent -> agent.changeMode(EAIControlState.EMERGENCY_MODE));

		SumatraModel.getInstance().getModuleOpt(ASkillSystem.class).ifPresent(ASkillSystem::emergencyStop);
	}


	@Override
	public void onToggleRecord()
	{
		SumatraModel.getInstance().getModuleOpt(RecordManager.class).ifPresent(RecordManager::toggleRecording);
	}


	@Override
	public void onOpenReplay(final PersistenceDb db)
	{
		new ReplayMainPresenter().start(db, 0);
	}


	private void compressReplay(Path path)
	{
		try
		{
			PersistenceDb db = new PersistenceDb(path);
			db.close();
			db.compress();
		} catch (IOException e)
		{
			log.error("Could not create ZIP file: {}", path, e);
		}
	}


	@Override
	public void onCompressReplay(final Path path)
	{
		CompletableFuture.runAsync(() -> compressReplay(path));
	}
}
