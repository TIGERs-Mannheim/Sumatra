/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics.view;

import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.MatchStatistics.EAvailableStatistic;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.data.statistics.calculators.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Main Panel for Game statistics from tactical Field
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsPanel extends JPanel implements ISumatraView
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID					= -314343167523031597L;
	
	
	private JLabel						ballPossessionBothNoOne			= null;
	private JLabel						ballPossessionTigersOpponents	= null;
	private JLabel						tackleWonLost						= null;
	private JLabel						possibleGoals						= null;
	
	private final DecimalFormat	df										= new DecimalFormat("###.#%");
	
	private JCheckBox					showHardwareIDs;
	private StatisticsTable			statTable;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param teamColor
	 */
	public StatisticsPanel(final ETeamColor teamColor)
	{
		setLayout(new MigLayout("wrap"));
		
		ballPossessionBothNoOne = new JLabel();
		ballPossessionBothNoOne.setFont(ballPossessionBothNoOne.getFont().deriveFont(Font.BOLD));
		ballPossessionTigersOpponents = new JLabel();
		ballPossessionTigersOpponents.setFont(ballPossessionTigersOpponents.getFont().deriveFont(Font.BOLD));
		tackleWonLost = new JLabel();
		tackleWonLost.setFont(tackleWonLost.getFont().deriveFont(Font.BOLD));
		possibleGoals = new JLabel();
		possibleGoals.setFont(possibleGoals.getFont().deriveFont(Font.BOLD));
		
		final JPanel ballPossessionPanel = new JPanel(new MigLayout("fill, inset 0",
				"[80,fill]10[80,fill]10[80,fill]10[80,fill]10[80,fill]"));
		ballPossessionPanel.add(new JLabel("BallPossesion:"), "wrap");
		ballPossessionPanel.add(new JLabel("Tigers - Opponents"));
		ballPossessionPanel.add(ballPossessionTigersOpponents);
		ballPossessionPanel.add(new JLabel("Both - No one"));
		ballPossessionPanel.add(ballPossessionBothNoOne);
		
		final JPanel tacklePanel = new JPanel(new MigLayout("fill, inset 0", "[80,fill]10[80,fill]10[80,fill]"));
		tacklePanel.add(new JLabel("Tackles: Won - Lost"));
		tacklePanel.add(tackleWonLost);
		
		final JPanel possibelGoalsPanel = new JPanel(new MigLayout("fill, inset 0",
				"[80,fill]10[80,fill]10[80,fill]"));
		possibelGoalsPanel.add(new JLabel("Possible Goals: Tigers - Opponents"));
		possibelGoalsPanel.add(possibleGoals);
		
		add(ballPossessionPanel);
		add(tacklePanel);
		add(possibelGoalsPanel);
		
		showHardwareIDs = new JCheckBox("Show HardwareIDs");
		add(showHardwareIDs);
		
		statTable = new StatisticsTable();
		
		add(statTable);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param lastVisualizationFrame
	 */
	public void onNewVisualizationFrame(final VisualizationFrame lastVisualizationFrame)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			private MatchStatistics	stats	= null;
			
			
			// private GameEvents gameEvents = null;
			
			
			@Override
			public void run()
			{
				stats = lastVisualizationFrame.getMatchStatistics();
				
				// gameEvents = lastVisualizationFrame.getGameEvents();
				
				if (stats != null)
				{
					updateBallPossession();
					
					updateTackles();
					
					updateGoalsPossibilities();
					
					updateStatisticsTable();
				}
			}
			
			
			private void updateBallPossession()
			{
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.WE))
				{
					String tigersPos = df.format(stats.getBallPossessionGeneral().get(EBallPossession.WE)
							.getPercent());
					String oppPos = df.format(stats.getBallPossessionGeneral().get(EBallPossession.THEY)
							.getPercent());
					ballPossessionTigersOpponents.setText(new StringBuilder().append(tigersPos).append(" - ").append(oppPos)
							.toString());
				}
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.BOTH))
				{
					
					String tigersPos = df.format(stats.getBallPossessionGeneral().get(EBallPossession.BOTH)
							.getPercent());
					String oppPos = df.format(stats.getBallPossessionGeneral().get(EBallPossession.NO_ONE)
							.getPercent());
					ballPossessionBothNoOne.setText(new StringBuilder().append(tigersPos).append(" - ").append(oppPos)
							.toString());
				}
			}
			
			
			private void updateTackles()
			{
				String tackles = new StringBuilder().append(stats.getTackleGeneralLost().getCurrent()).append(" - ")
						.append(stats.getTackleGeneralWon().getCurrent()).toString();
				tackleWonLost.setText(tackles);
			}
			
			
			private void updateGoalsPossibilities()
			{
				String pGoals = new StringBuilder().append(stats.getPossibleTigersGoals()).append(" - ")
						.append(stats.getPossibleOpponentsGoals()).toString();
				possibleGoals.setText(pGoals);
			}
			
			
			private void updateStatisticsTable()
			{
				IBotIDMap<ITrackedBot> tigersAvail = lastVisualizationFrame.getWorldFrame().getTigerBotsAvailable();
				
				processShowingOfHardwareIDs(tigersAvail.keySet());
				
				Map<String, StatisticData> statistics = new HashMap<String, StatisticData>();
				
				for (EAvailableStatistic statistic : stats.getStatistics().keySet())
				{
					statistics.put(statistic.getDescriptor(), stats.getStatistics().get(statistic));
				}
				
				statTable.updateTableEntries(statistics, tigersAvail.keySet());
			}
			
			
			private void processShowingOfHardwareIDs(final Set<BotID> availableBots)
			{
				statTable.setHardwareIDShown(showHardwareIDs.isSelected());
				
				Map<BotID, Integer> hardwareIDs = new HashMap<>();
				
				for (BotID tempBot : availableBots)
				{
					int hardwareID = lastVisualizationFrame.getWorldFrame().getTiger(tempBot).getBot().getHardwareId();
					
					hardwareIDs.put(tempBot, hardwareID);
				}
				
				statTable.updateHardwareIDs(hardwareIDs);
			}
		});
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final List<JMenu> menus = new ArrayList<JMenu>();
		return menus;
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
