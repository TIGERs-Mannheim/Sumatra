/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: Sumatra
 * Date: Jul 27, 2010
 * Authors:
 * Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */

package edu.tigers.sumatra.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.ball.BallAnalyserView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.BotCenterView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.RcmView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.referee.RefereeView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.sim.SimulationView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.timer.TimerView;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayLoadMenu;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayLoadMenu.IReplayLoadMenuObserver;
import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.IMainFrameObserver;
import edu.tigers.sumatra.ReplayPresenter;
import edu.tigers.sumatra.aicenter.AICenterView;
import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.config.ConfigEditorView;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.OffensiveStrategyView;
import edu.tigers.sumatra.persistance.IRecordPersistence;
import edu.tigers.sumatra.statistics.StatisticsView;
import edu.tigers.sumatra.util.ShortcutsDialog;
import edu.tigers.sumatra.view.toolbar.ToolBar;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.visualizer.VisualizerAiView;


/**
 * The Sumatra-main view with all available JComponents as dockable views :).
 */
public class MainFrame extends AMainFrame implements IReplayLoadMenuObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(MainFrame.class.getName());
	private static final long		serialVersionUID	= -6858464942004450029L;
	
	private final JMenu				moduliMenu			= new JMenu("Moduli");
	private final JMenu				lookAndFeelMenu	= new JMenu("Look & Feel");
	private final ReplayLoadMenu	replayMenu			= new ReplayLoadMenu();
	
	private final ToolBar			toolBar				= new ToolBar();
	
	
	/**
	 * Constructor of GuiView.
	 */
	public MainFrame()
	{
		log.trace("Create mainframe");
		this.add(toolBar.getToolbar(), BorderLayout.NORTH);
		setTitle("Tigers Mannheim - Sumatra V " + SumatraModel.getVersion());
		
		replayMenu.addObserver(this);
		
		addView(new LogView(true));
		addView(new AICenterView(ETeamColor.YELLOW));
		addView(new AICenterView(ETeamColor.BLUE));
		addView(new BotCenterView());
		addView(new ConfigEditorView());
		addView(new RcmView());
		addView(new RefereeView());
		addView(new TimerView());
		addView(new VisualizerAiView());
		addView(new BotOverviewView());
		addView(new StatisticsView(ETeamColor.YELLOW));
		addView(new StatisticsView(ETeamColor.BLUE));
		addView(new OffensiveStrategyView());
		addView(new SimulationView());
		addView(new BallAnalyserView());
		addView(new AutoRefView());
		addView(new GameLogView());
		addView(new BallSpeedView());
		
		updateViewMenu();
		
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
	 * @param enabled
	 */
	public void setModuliMenuEnabled(final boolean enabled)
	{
		for (int i = 0; i < moduliMenu.getItemCount(); i++)
		{
			moduliMenu.getItem(i).setEnabled(enabled);
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
		// File Menu
		JMenu sumatraMenu = new JMenu("Sumatra");
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new Exit());
		exitMenuItem.setToolTipText("Exits the application");
		
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
		private final String	configName;
		
		
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
			SumatraModel.getInstance().setCurrentModuliConfig(configName);
		}
	}
	
	
	private class SetLookAndFeel implements ActionListener
	{
		private final LookAndFeelInfo	info;
		
		
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
	public void onLoadPersistance(final IRecordPersistence p)
	{
		new ReplayPresenter().start(p);
	}
}
