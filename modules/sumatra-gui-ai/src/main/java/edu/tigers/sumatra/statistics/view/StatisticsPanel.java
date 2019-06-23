/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats.EMatchStatistics;
import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.statistics.Percentage;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
	
	private PossessionBar ballPossessionBar;
	/**
	 * Default
	 */
	public StatisticsPanel()
	{
		setLayout(new MigLayout());
		
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
		add(teamPanel, "wrap, dock north");
		

		
		statTable = new StatisticsTable();
		Panel tPanel = new Panel();
		tPanel.setLayout(new MigLayout());
		tPanel.add(statTable.getTableHeader(), "wrap, dock north");
		tPanel.add(statTable, "dock center, wrap");
		add(tPanel, "dock center, wrap");


		lblBallPossession = new JLabel();
		lblBallPossession.setFont(lblBallPossession.getFont().deriveFont(Font.BOLD));
		ballPossessionBar = new PossessionBar();

		add(ballPossessionBar, "dock south, wrap");
		add(lblBallPossession, "dock south, wrap");

	}
	
	
	/**
	 * Reset the panel
	 */
	public void reset()
	{
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
		Map<EBallPossession, Percentage> bs = stats.getBallPossessionGeneral();
		double we = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.WE).getPercent();
		double they = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.THEY).getPercent();
		double both = 100.0 * stats.getBallPossessionGeneral().get(EBallPossession.BOTH).getPercent();
		lblBallPossession
				.setText(String.format("We %3.1f%% - %3.1f%% They, Both: %3.1f%%", we, they, both));

		if (selectedTeamColor == ETeamColor.YELLOW)
		{
			ballPossessionBar.setTeamShareBoth(bs.get(EBallPossession.WE).getPercent(), bs.get(EBallPossession.THEY).getPercent());
		}
		else
		{
			ballPossessionBar.setTeamShareBoth(bs.get(EBallPossession.THEY).getPercent(), bs.get(EBallPossession.WE).getPercent());
		}
	}
	
	
	private void updateStatisticsTable()
	{
		Map<String, StatisticData> statistics = new LinkedHashMap<>();
		
		for (EMatchStatistics statistic : stats.getStatistics().keySet())
		{
			statistics.put(statistic.getDescriptor(), stats.getStatistics().get(statistic));
		}
		
		Set<BotID> allBots = stats.getAllBots();
		statTable.setData(statistics, allBots);
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
