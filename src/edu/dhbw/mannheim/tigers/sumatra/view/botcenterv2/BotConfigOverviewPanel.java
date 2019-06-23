/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.CommandPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.MovePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv3.SystemMatchFeedbackPanel;


/**
 * container for selecting bot and a tabbed pane with configs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotConfigOverviewPanel extends JPanel
{
	/**  */
	private static final long										serialVersionUID			= 1320083400713333902L;
	private final JComboBox<BotID>								cmbBots						= new JComboBox<>();
	
	private final BcBotNetStatsPanel								bcBotNetStatsPanel		= new BcBotNetStatsPanel();
	private final BcBotPingPanel									bcBotPingPanel				= new BcBotPingPanel();
	private final BcBotKickerPanel								bcBotKickerPanel			= new BcBotKickerPanel();
	private final BcBotControllerCfgPanel						bcBotControllerCfgPanel	= new BcBotControllerCfgPanel();
	private final MovePanel											movePanel					= new MovePanel();
	private final SkillsPanel										skillsPanel					= new SkillsPanel();
	private final ConsolePanel										consolePanel				= new ConsolePanel();
	private final CommandPanel										commandPanel				= new CommandPanel();
	private final SystemMatchFeedbackPanel						systemFeedbackPanel		= new SystemMatchFeedbackPanel();
	private final BotConfigPanel									configPanel					= new BotConfigPanel();
	
	
	private final List<IBotConfigOverviewPanelObserver>	observers					= new CopyOnWriteArrayList<IBotConfigOverviewPanelObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBotConfigOverviewPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBotConfigOverviewPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyBotIdSelected(final BotID botId)
	{
		for (IBotConfigOverviewPanelObserver observer : observers)
		{
			observer.onBotIdSelected(botId);
		}
	}
	
	
	/**
	 * 
	 */
	public BotConfigOverviewPanel()
	{
		setLayout(new BorderLayout());
		JPanel selBotPanel = new JPanel();
		selBotPanel.add(new JLabel("Choose Bot: "));
		cmbBots.setPreferredSize(new Dimension(150, 25));
		cmbBots.addItemListener(new BotIdSelectedActionListener());
		cmbBots.addItem(BotID.createBotId());
		selBotPanel.add(cmbBots);
		add(selBotPanel, BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("network", setupScrollPane(bcBotNetStatsPanel));
		tabbedPane.addTab("status", setupScrollPane(systemFeedbackPanel));
		tabbedPane.addTab("kicker", setupScrollPane(bcBotKickerPanel));
		tabbedPane.addTab("move", setupScrollPane(movePanel));
		tabbedPane.addTab("skills", setupScrollPane(skillsPanel));
		tabbedPane.addTab("controller", setupScrollPane(bcBotControllerCfgPanel));
		tabbedPane.addTab("console", consolePanel);
		tabbedPane.addTab("ping", setupScrollPane(bcBotPingPanel));
		tabbedPane.addTab("config", setupScrollPane(configPanel));
		// tabbedPane.addTab("command", setupScrollPane(commandPanel));
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	
	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}
	
	
	/**
	 * @return the cmbBots
	 */
	public JComboBox<BotID> getCmbBots()
	{
		return cmbBots;
	}
	
	
	/**
	 * @return the bcBotNetStats
	 */
	public BcBotNetStatsPanel getBcBotNetStatsPanel()
	{
		return bcBotNetStatsPanel;
	}
	
	
	/**
	 * @return the bcBotPingPanel
	 */
	public BcBotPingPanel getBcBotPingPanel()
	{
		return bcBotPingPanel;
	}
	
	
	/**
	 * @return the bcBotKickerPanel
	 */
	public BcBotKickerPanel getBcBotKickerPanel()
	{
		return bcBotKickerPanel;
	}
	
	
	/**
	 * @return the bcBotControllerCfgPanel
	 */
	public BcBotControllerCfgPanel getBcBotControllerCfgPanel()
	{
		return bcBotControllerCfgPanel;
	}
	
	
	/**
	 * @return the movePanel
	 */
	public MovePanel getMovePanel()
	{
		return movePanel;
	}
	
	
	/**
	 * @return the skillsPanel
	 */
	public SkillsPanel getSkillsPanel()
	{
		return skillsPanel;
	}
	
	
	/**
	 * @return the consolePanel
	 */
	public ConsolePanel getConsolePanel()
	{
		return consolePanel;
	}
	
	
	/**
	 * @return the commandPanel
	 */
	public CommandPanel getCommandPanel()
	{
		return commandPanel;
	}
	
	
	/**
	 * @return the systemStatusPanel
	 */
	public final SystemMatchFeedbackPanel getSystemStatusPanel()
	{
		return systemFeedbackPanel;
	}
	
	
	/**
	 * @return
	 */
	public final BotConfigPanel getConfigPanel()
	{
		return configPanel;
	}
	
	
	/**
	 */
	public static interface IBotConfigOverviewPanelObserver
	{
		/**
		 * @param botId
		 */
		void onBotIdSelected(BotID botId);
	}
	
	private class BotIdSelectedActionListener implements ItemListener
	{
		@Override
		public void itemStateChanged(final ItemEvent e)
		{
			BotID botId = (BotID) e.getItem();
			if (botId != null)
			{
				notifyBotIdSelected(botId);
			}
		}
	}
}
