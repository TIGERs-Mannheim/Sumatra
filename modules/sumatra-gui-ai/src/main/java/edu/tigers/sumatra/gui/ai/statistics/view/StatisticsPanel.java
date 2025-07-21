/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.statistics.view;

import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.statistics.stats.EMatchStatistics;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.Percentage;
import edu.tigers.sumatra.ai.metis.statistics.stats.StatisticData;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Main Panel for Game statistics from tactical Field
 */
public class StatisticsPanel extends JPanel
{
	private final JLabel lblBallPossession = new JLabel();
	private final StatisticsTable statTable = new StatisticsTable();
	private final PossessionBar ballPossessionBar = new PossessionBar();

	private transient MatchStats stats = null;
	@Getter
	private ETeamColor selectedTeamColor = ETeamColor.YELLOW;


	public StatisticsPanel()
	{
		setLayout(new BorderLayout());

		JPanel componentPanel = new JPanel();
		componentPanel.setLayout(new MigLayout());

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
		componentPanel.add(teamPanel, "wrap, dock north");

		componentPanel.add(statTable, "dock center, wrap");

		lblBallPossession.setFont(lblBallPossession.getFont().deriveFont(Font.BOLD));

		componentPanel.add(ballPossessionBar, "dock south, wrap");
		componentPanel.add(lblBallPossession, "dock south, wrap");

		BetterScrollPane scrollPane = new BetterScrollPane(componentPanel);
		add(scrollPane, BorderLayout.CENTER);
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
			update();
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
			ballPossessionBar.setTeamShareBoth(bs.get(EBallPossession.WE).getPercent(),
					bs.get(EBallPossession.THEY).getPercent());
		} else
		{
			ballPossessionBar.setTeamShareBoth(bs.get(EBallPossession.THEY).getPercent(),
					bs.get(EBallPossession.WE).getPercent());
		}
	}


	private void updateStatisticsTable()
	{
		Map<String, StatisticData> statistics = new LinkedHashMap<>();

		for (EMatchStatistics statistic : stats.getStatistics().keySet())
		{
			statistics.put(statistic.getDescriptor(), stats.getStatistics().get(statistic));
		}

		Set<Integer> allBots = stats.getAllBots();
		statTable.setData(statistics, allBots);
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
