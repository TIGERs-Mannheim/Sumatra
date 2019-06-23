/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;

import edu.tigers.sumatra.view.botcenter.bots.ConsolePanel;
import edu.tigers.sumatra.view.botcenter.bots.ManualControlPanel;
import edu.tigers.sumatra.view.botcenter.bots.SkillsPanel;
import edu.tigers.sumatra.view.botcenter.bots.SystemMatchFeedbackPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.view.botcenter.bots.ConsolePanel;
import edu.tigers.sumatra.view.botcenter.bots.ManualControlPanel;
import edu.tigers.sumatra.view.botcenter.bots.SkillsPanel;
import edu.tigers.sumatra.view.botcenter.bots.SystemMatchFeedbackPanel;


/**
 * container for selecting bot and a tabbed pane with configs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotConfigOverviewPanel extends JPanel
{
	/**  */
	private static final long										serialVersionUID		= 1320083400713333902L;
	private final JComboBox<BotID>								cmbBots					= new JComboBox<>();
	
	private final ManualControlPanel manualControlPanel	= new ManualControlPanel();
	private final SkillsPanel skillsPanel				= new SkillsPanel();
	private final ConsolePanel consolePanel			= new ConsolePanel();
	private final SystemMatchFeedbackPanel systemFeedbackPanel	= new SystemMatchFeedbackPanel();
	private final BotConfigPanel									configPanel				= new BotConfigPanel();
	
	
	private final List<IBotConfigOverviewPanelObserver>	observers				= new CopyOnWriteArrayList<>();
	
	
	/**
	 * Constructor.
	 */
	public BotConfigOverviewPanel()
	{
		setLayout(new BorderLayout());
		JPanel selBotPanel = new JPanel();
		selBotPanel.add(new JLabel("Choose Bot: "));
		cmbBots.setPreferredSize(new Dimension(150, 25));
		cmbBots.addItemListener(new BotIdSelectedActionListener());
		cmbBots.addItem(BotID.noBot());
		selBotPanel.add(cmbBots);
		add(selBotPanel, BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Status", setupScrollPane(systemFeedbackPanel));
		tabbedPane.addTab("Manual Control", setupScrollPane(manualControlPanel));
		tabbedPane.addTab("Skills", setupScrollPane(skillsPanel));
		tabbedPane.addTab("Console", consolePanel);
		tabbedPane.addTab("Config", setupScrollPane(configPanel));
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	
	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}
	
	
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
	
	
	/**
	 * @return the cmbBots
	 */
	public JComboBox<BotID> getCmbBots()
	{
		return cmbBots;
	}
	
	
	/**
	 * @return the manualControlPanel
	 */
	public ManualControlPanel getManualControlPanel()
	{
		return manualControlPanel;
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
	 * Observer interface.
	 */
	@FunctionalInterface
	public interface IBotConfigOverviewPanelObserver
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
		
		
		private void notifyBotIdSelected(final BotID botId)
		{
			for (IBotConfigOverviewPanelObserver observer : observers)
			{
				observer.onBotIdSelected(botId);
			}
		}
	}
}
