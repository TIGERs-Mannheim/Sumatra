package edu.tigers.sumatra.presenter;

import edu.tigers.autoref.presenter.AutoRefPresenter;
import edu.tigers.autoref.presenter.BallSpeedPresenter;
import edu.tigers.autoref.presenter.GameLogPresenter;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.AModuliMainPresenter;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.config.ConfigEditorPresenter;
import edu.tigers.sumatra.gui.ai.aicenter.presenter.AICenterPresenter;
import edu.tigers.sumatra.gui.ai.botoverview.presenter.BotOverviewPresenter;
import edu.tigers.sumatra.gui.ai.offensive.interceptions.presenter.OffensiveInterceptionsPresenter;
import edu.tigers.sumatra.gui.ai.offensive.statistics.presenter.OffensiveStatisticsPresenter;
import edu.tigers.sumatra.gui.ai.offensive.strategy.presenter.OffensiveStrategyPresenter;
import edu.tigers.sumatra.gui.ai.statistics.presenter.StatisticsPresenter;
import edu.tigers.sumatra.gui.ai.support.presenter.SupportBehaviorsPresenter;
import edu.tigers.sumatra.gui.ai.visualizer.presenter.VisualizerAiPresenter;
import edu.tigers.sumatra.gui.ballanalyzer.presenter.VisionAnalyserPresenter;
import edu.tigers.sumatra.gui.botcenter.presenter.BotCenterPresenter;
import edu.tigers.sumatra.gui.botparams.presenter.BotParamsPresenter;
import edu.tigers.sumatra.gui.kick.presenter.BallKickIdentPresenter;
import edu.tigers.sumatra.gui.log.presenter.LogPresenter;
import edu.tigers.sumatra.gui.logfile.presenter.LogfilePresenter;
import edu.tigers.sumatra.gui.rcm.presenter.RCMPresenter;
import edu.tigers.sumatra.gui.referee.presenter.RefereePresenter;
import edu.tigers.sumatra.gui.replay.view.ReplayLoadMenu.IReplayLoadMenuObserver;
import edu.tigers.sumatra.gui.sim.presenter.SimulationPresenter;
import edu.tigers.sumatra.gui.skills.presenter.SkillsPresenter;
import edu.tigers.sumatra.gui.timer.presenter.TimerPresenter;
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
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.SumatraView;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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
		String defaultLaf = SumatraModel.getInstance()
				.getUserProperty(KEY_LAF_PROP, UIManager.getSystemLookAndFeelClassName());
		applyLookAndFeel(defaultLaf);

		super(new MainFrame(), createViews(), "main");
		log.trace("Create MainPresenter");
		toolbarPresenter = new ToolbarPresenter(getMainFrame().getToolbar());

		getMainFrame().getMenuReplay().addObserver(this);
		getMainFrame().getMenuItemSumatraShortcut().addActionListener(_ -> new ShortcutsDialog(getMainFrame()));
		getMainFrame().getMenuItemSumatraAbout().addActionListener(_ -> new AboutDialog());
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


	private static List<SumatraView> createViews()
	{
		return List.of(
				new SumatraView(ESumatraViewType.LOG, () -> new LogPresenter(true)),
				new SumatraView(ESumatraViewType.AI_CENTER, AICenterPresenter::new),
				new SumatraView(ESumatraViewType.BOT_CENTER, BotCenterPresenter::new),
				new SumatraView(ESumatraViewType.CONFIG_EDITOR, ConfigEditorPresenter::new),
				new SumatraView(ESumatraViewType.RCM, RCMPresenter::new),
				new SumatraView(ESumatraViewType.REFEREE, RefereePresenter::new),
				new SumatraView(ESumatraViewType.TIMER, TimerPresenter::new),
				new SumatraView(ESumatraViewType.VISUALIZER, VisualizerAiPresenter::new),
				new SumatraView(ESumatraViewType.BOT_OVERVIEW, BotOverviewPresenter::new),
				new SumatraView(ESumatraViewType.STATISTICS, StatisticsPresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_STRATEGY, OffensiveStrategyPresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_INTERCEPTIONS, OffensiveInterceptionsPresenter::new),
				new SumatraView(ESumatraViewType.SIMULATION, SimulationPresenter::new),
				new SumatraView(ESumatraViewType.VISION_ANALYSER, VisionAnalyserPresenter::new),
				new SumatraView(ESumatraViewType.AUTOREFEREE, AutoRefPresenter::new),
				new SumatraView(ESumatraViewType.AUTOREFEREE_GAME_LOG, GameLogPresenter::new),
				new SumatraView(ESumatraViewType.BALL_SPEED, BallSpeedPresenter::new),
				new SumatraView(ESumatraViewType.LOGFILE, LogfilePresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_STATISTICS, OffensiveStatisticsPresenter::new),
				new SumatraView(ESumatraViewType.BOT_PARAMS, BotParamsPresenter::new),
				new SumatraView(ESumatraViewType.BALL_KICK_IDENT, BallKickIdentPresenter::new),
				new SumatraView(ESumatraViewType.SUPPORT_BEHAVIORS, SupportBehaviorsPresenter::new),
				new SumatraView(ESumatraViewType.SKILLS, SkillsPresenter::new)
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
			menuItems.forEach((name, item) -> item.addActionListener(_ -> SumatraModel.getInstance().startUpAsync(name)));
			getMainFrame().setModuliItemSelected(SumatraModel.getInstance().getCurrentModuliConfig());
		} catch (IOException e)
		{
			log.error("Could not read config files from path: {}", configPath, e);
		}
	}


	private void refreshLookAndFeelMenu()
	{
		var lafInfos = Arrays.stream(UIManager.getInstalledLookAndFeels())
				.sorted(Comparator.comparing(LookAndFeelInfo::getName))
				.toList();

		var menuItems = getMainFrame().setLookAndFeelItems(lafInfos);
		menuItems.forEach((info, item) -> item.addActionListener(_ -> setCurrentLookAndFeel(info)));
	}


	private static void applyLookAndFeel(String lookAndFeelClassName)
	{
		log.trace("Loading look and feel: {}", lookAndFeelClassName);

		try
		{
			UIManager.setLookAndFeel(lookAndFeelClassName);
		} catch (final ClassNotFoundException
		               | InstantiationException
		               | IllegalAccessException
		               | UnsupportedLookAndFeelException err)
		{
			log.error("Could not select look and feel.", err);
		}
	}


	private void setCurrentLookAndFeel(final LookAndFeelInfo info)
	{
		applyLookAndFeel(info.getClassName());

		// update visible components
		final int state = getMainFrame().getExtendedState();
		SwingUtilities.updateComponentTreeUI(getMainFrame());
		getMainFrame().setExtendedState(state);
		getMainFrame().setLookAndFeelItemSelected(info.getName());
		SumatraModel.getInstance().setUserProperty(KEY_LAF_PROP, info.getClassName());
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
