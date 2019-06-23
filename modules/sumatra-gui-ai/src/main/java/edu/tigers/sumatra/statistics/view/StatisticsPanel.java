/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.MatchStats.EMatchStatistics;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Main Panel for Game statistics from tactical Field
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsPanel extends JPanel implements ISumatraView
{
	private final JLabel lblBallPossession;
	private StatisticsTable statTable;
	private MatchStats stats = null;
	private ETeamColor selectedTeamColor = ETeamColor.YELLOW;
	
	
	/**
	 * Default
	 */
	public StatisticsPanel()
	{
		setLayout(new MigLayout("wrap"));
		
		JRadioButton rBtnYellow = new JRadioButton(ETeamColor.YELLOW.name());
		rBtnYellow.setActionCommand(ETeamColor.YELLOW.name());
		rBtnYellow.addActionListener(new TeamActionListener());
		JRadioButton rBtnBlue = new JRadioButton(ETeamColor.BLUE.name());
		rBtnBlue.setActionCommand(ETeamColor.BLUE.name());
		rBtnBlue.addActionListener(new TeamActionListener());
		ButtonGroup teamButtonGroup = new ButtonGroup();
		teamButtonGroup.add(rBtnYellow);
		teamButtonGroup.add(rBtnBlue);
		rBtnYellow.setSelected(true);
		
		JPanel teamPanel = new JPanel();
		teamPanel.add(rBtnYellow);
		teamPanel.add(rBtnBlue);
		add(teamPanel);
		
		lblBallPossession = new JLabel();
		lblBallPossession.setFont(lblBallPossession.getFont().deriveFont(Font.BOLD));
		
		add(lblBallPossession);
		
		statTable = new StatisticsTable();
		
		add(statTable);
	}
	
	
	/**
	 * Reset the panel
	 */
	public void reset()
	{
		remove(statTable);
		statTable = new StatisticsTable();
		add(statTable);
		repaint();
	}
	
	
	/**
	 * @param lastVisualizationFrame
	 */
	public void onNewVisualizationFrame(final VisualizationFrame lastVisualizationFrame)
	{
		stats = lastVisualizationFrame.getMatchStats();
		if (stats != null)
		{
			SwingUtilities.invokeLater(this::update);
		}
	}
	
	
	private void update()
	{
		updateBallPossession();
		updateStatisticsTable();
	}
	
	
	private void updateBallPossession()
	{
		double we = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.WE).getPercent();
		double they = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.THEY).getPercent();
		double both = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.BOTH).getPercent();
		lblBallPossession
				.setText(String.format("We %3.1f%% - %3.1f%% They, Both: %3.1f%%", we, they, both));
	}
	
	
	private void updateStatisticsTable()
	{
		Map<String, StatisticData> statistics = new LinkedHashMap<>();
		
		for (EMatchStatistics statistic : stats.getStatistics().keySet())
		{
			statistics.put(statistic.getDescriptor(), stats.getStatistics().get(statistic));
		}
		
		Set<BotID> allBots = stats.getAllBots();
		statTable.updateTableEntries(statistics, allBots);
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return Collections.emptyList();
	}
	
	
	public ETeamColor getSelectedTeamColor()
	{
		return selectedTeamColor;
	}
	
	private class TeamActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			selectedTeamColor = ETeamColor.valueOf(actionEvent.getActionCommand());
			reset();
		}
	}
}
