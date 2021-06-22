/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view;

import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.IMainFrameObserver;
import edu.tigers.sumatra.aicenter.AICenterView;
import edu.tigers.sumatra.botcenter.view.BotCenterView;
import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.botparams.view.BotParamsView;
import edu.tigers.sumatra.config.ConfigEditorView;
import edu.tigers.sumatra.kick.view.BallKickIdentView;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.OffensiveActionTreeView;
import edu.tigers.sumatra.offensive.OffensiveInterceptionsView;
import edu.tigers.sumatra.offensive.OffensiveStatisticsView;
import edu.tigers.sumatra.offensive.OffensiveStrategyView;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.presenter.ball.BallAnalyserView;
import edu.tigers.sumatra.presenter.log.LogView;
import edu.tigers.sumatra.presenter.logfile.LogfileView;
import edu.tigers.sumatra.presenter.rcm.RcmView;
import edu.tigers.sumatra.presenter.referee.RefereeView;
import edu.tigers.sumatra.presenter.sim.SimulationView;
import edu.tigers.sumatra.presenter.testplays.TestPlaysControlView;
import edu.tigers.sumatra.presenter.timer.TimerView;
import edu.tigers.sumatra.replay.AiReplayPresenter;
import edu.tigers.sumatra.statistics.StatisticsView;
import edu.tigers.sumatra.support.SupportBehaviorsView;
import edu.tigers.sumatra.util.ShortcutsDialog;
import edu.tigers.sumatra.view.replay.ReplayLoadMenu;
import edu.tigers.sumatra.view.replay.ReplayLoadMenu.IReplayLoadMenuObserver;
import edu.tigers.sumatra.view.skills.SkillsView;
import edu.tigers.sumatra.view.toolbar.ToolBar;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.visualizer.VisualizerAiView;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;


/**
 * The Sumatra-main view with all available JComponents as dockable views :).
 */
public class MainFrame extends AMainFrame implements IReplayLoadMenuObserver
{
	private static final long serialVersionUID = -6858464942004450029L;

	private JMenu moduliMenu;
	private JMenu lookAndFeelMenu;
	private ReplayLoadMenu replayMenu;

	private transient ToolBar toolBar;


	public MainFrame()
	{
		setTitle("TIGERs Mannheim - Sumatra " + SumatraModel.getVersion());

		addView(new LogView(true));
		addView(new AICenterView());
		addView(new BotCenterView());
		addView(new ConfigEditorView());
		addView(new RcmView());
		addView(new RefereeView());
		addView(new TimerView());
		addView(new VisualizerAiView());
		addView(new BotOverviewView());
		addView(new StatisticsView());
		addView(new OffensiveStrategyView());
		addView(new OffensiveInterceptionsView());
		addView(new SimulationView());
		addView(new BallAnalyserView());
		addView(new AutoRefView());
		addView(new GameLogView());
		addView(new BallSpeedView());
		addView(new LogfileView());
		addView(new TestPlaysControlView());
		addView(new OffensiveStatisticsView());
		addView(new BotParamsView());
		addView(new OffensiveActionTreeView());
		addView(new BallKickIdentView());
		addView(new SupportBehaviorsView());
		addView(new SkillsView());

		updateViewMenu();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}


	/**
	 * Creates the ToolBar and some JMenus.
	 * Separate from the Constructor because the ToolBar requires the correct LookAndFeel to be set.
	 */
	public void initializeViews()
	{
		moduliMenu = new JMenu("Moduli");
		lookAndFeelMenu = new JMenu("Look & Feel");
		replayMenu = new ReplayLoadMenu();

		moduliMenu.setMnemonic(KeyEvent.VK_M);
		replayMenu.addObserver(this);
		replayMenu.setMnemonic(KeyEvent.VK_R);
		lookAndFeelMenu.setMnemonic(KeyEvent.VK_F);

		toolBar = new ToolBar();
		this.add(toolBar.getToolbar(), BorderLayout.NORTH);

		fillMenuBar();
	}


	/**
	 * @param names
	 */
	public void setMenuModuliItems(final List<String> names)
	{
		moduliMenu.removeAll();

		final ButtonGroup group = new ButtonGroup();

		for (final String name : names)
		{
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
			item.addActionListener(new LoadConfig(name));
			group.add(item);
			moduliMenu.add(item);
		}
	}


	/**
	 * @param name
	 */
	public void selectModuliItem(final String name)
	{
		// select RadioButton in moduliMenu
		for (int i = 0; i < moduliMenu.getItemCount(); i++)
		{
			final JMenuItem item = moduliMenu.getItem(i);
			if (item.getText().equals(name))
			{
				item.setSelected(true);
			}
		}
	}


	/**
	 * @param name
	 */
	public void selectLookAndFeelItem(final String name)
	{
		for (int i = 0; i < lookAndFeelMenu.getItemCount(); i++)
		{
			final JMenuItem item = lookAndFeelMenu.getItem(i);
			if (item.getText().equals(name))
			{
				item.setSelected(true);
			}
		}
	}


	/**
	 * @param lafName
	 */
	public void setLookAndFeel(final String lafName)
	{
		final JFrame frame = this;

		// update visible components
		final int state = getExtendedState();
		SwingUtilities.updateComponentTreeUI(frame);
		setExtendedState(state);

		// update menu
		for (int i = 0; i < lookAndFeelMenu.getItemCount(); i++)
		{
			final JMenuItem item = lookAndFeelMenu.getItem(i);
			if (item.getText().equals(lafName))
			{
				item.setSelected(true);
			}
		}

		// update all views (including non-visible)
		for (final ASumatraView view : getViews())
		{
			if (view.isInitialized())
			{
				SwingUtilities.updateComponentTreeUI(view.getComponent());
			}
		}
	}


	/**
	 * @return
	 */
	public ToolBar getToolbar()
	{
		return toolBar;
	}


	/**
	 * Creates the frame menu bar.
	 */
	private void fillMenuBar()
	{
		// JMenuBar on the macOS menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// File Menu
		JMenu sumatraMenu = new JMenu("Sumatra");
		sumatraMenu.setMnemonic(KeyEvent.VK_S);

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new Exit());
		exitMenuItem.setToolTipText("Exits the application");
		exitMenuItem.setMnemonic(KeyEvent.VK_E);

		final JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");
		shortcutMenuItem.addActionListener(new ShortcutActionListener());

		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutBox());
		aboutMenuItem.setToolTipText("Information about Sumatra");

		sumatraMenu.add(shortcutMenuItem);
		sumatraMenu.add(aboutMenuItem);
		sumatraMenu.add(exitMenuItem);

		// look and feel menu
		final ButtonGroup group = new ButtonGroup();
		final LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (final LookAndFeelInfo info : lafs)
		{
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(info.getName());
			item.addActionListener(new SetLookAndFeel(info));
			group.add(item);
			lookAndFeelMenu.add(item);
			if (info.getClassName().equals(UIManager.getSystemLookAndFeelClassName()))
			{
				item.setSelected(true);
			}
		}

		getJMenuBar().add(sumatraMenu);
		getJMenuBar().add(moduliMenu);
		super.addMenuItems();
		getJMenuBar().add(lookAndFeelMenu);
		getJMenuBar().add(replayMenu);
	}


	private class AboutBox implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IMainFrameObserver o : observers)
			{
				o.onAbout();
			}
		}
	}


	private static class LoadConfig implements ActionListener
	{
		private final String configName;


		/**
		 * @param c
		 */
		public LoadConfig(final String c)
		{
			configName = c;
		}


		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			new Thread(() -> SumatraModel.getInstance().startUp(configName), "ModuliStateChanged").start();
		}
	}


	private class SetLookAndFeel implements ActionListener
	{
		private final LookAndFeelInfo info;


		/**
		 * @param i
		 */
		public SetLookAndFeel(final LookAndFeelInfo i)
		{
			info = i;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IMainFrameObserver o : observers)
			{
				o.onSelectLookAndFeel(info);
			}
		}
	}

	private static class ShortcutActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			new ShortcutsDialog().setVisible(true);
		}
	}


	@Override
	public void onOpenReplay(final BerkeleyDb db)
	{
		new AiReplayPresenter().start(db, 0);
	}


	@Override
	public void onCompressReplay(final Path path)
	{
		startReplayCompressionThread(path);
	}
}
