/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.statistics.PenaltyStats;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


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
	private static final long	serialVersionUID					= -314343167523031597L;
	
	
	private JLabel					ballPossessionBothNoOne			= null;
	private JLabel					ballPossessionTigersOpponents	= null;
	private JLabel					tackleWonLost						= null;
	private JLabel					possibleGoals						= null;
	
	private DecimalFormat		df										= new DecimalFormat("###.#%");
	
	private JTable					botStatTable;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param teamColor
	 */
	public StatisticsPanel(final ETeamColor teamColor)
	{
		setLayout(new MigLayout("wrap"));
		
		JTextField colorIndicator = new JTextField(50);
		colorIndicator.setEditable(false);
		if (teamColor == ETeamColor.BLUE)
		{
			colorIndicator.setBackground(Color.BLUE);
		} else
		{
			colorIndicator.setBackground(Color.YELLOW);
		}
		
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
		
		botStatTable = new JTable(7, 6);
		botStatTable.setEnabled(false);
		
		
		add(colorIndicator);
		add(ballPossessionPanel);
		add(tacklePanel);
		add(possibelGoalsPanel);
		add(botStatTable);
		
		botStatTable.setValueAt("BotID", 0, 0);
		botStatTable.setValueAt("GoalsPossible", 0, 1);
		botStatTable.setValueAt("WonTackles", 0, 2);
		botStatTable.setValueAt("LostTackles", 0, 3);
		botStatTable.setValueAt("BallPossession", 0, 4);
		botStatTable.setValueAt("PenaltyScore", 0, 5);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param lastAIInfoframe
	 */
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Statistics stats = lastAIInfoframe.getTacticalField().getStatistics();
				
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
				
				String tackles = new StringBuilder().append(stats.getTackleGeneralLost().getCurrent()).append(" - ")
						.append(stats.getTackleGeneralWon().getCurrent()).toString();
				tackleWonLost.setText(tackles);
				
				String pGoals = new StringBuilder().append(stats.getPossibleTigersGoals()).append(" - ")
						.append(stats.getPossibleOpponentsGoals()).toString();
				possibleGoals.setText(pGoals);
				
				IBotIDMap<TrackedTigerBot> tigersAvail = lastAIInfoframe.getWorldFrame().getTigerBotsAvailable();
				int row = 1;
				for (BotID bot : tigersAvail.keySet())
				{
					int hwId = lastAIInfoframe.getWorldFrame().getBot(bot).getBot().getHardwareId();
					botStatTable.setValueAt(hwId, row, 0);
					
					if (stats.getPossibleBotGoals().containsKey(bot))
					{
						int botGoals = stats.getPossibleBotGoals().get(bot).getCurrent();
						botStatTable.setValueAt(botGoals, row, 1);
					}
					if (stats.getTackleWon().containsKey(bot))
					{
						int tacklesWon = stats.getTackleWon().get(bot).getCurrent();
						botStatTable.setValueAt(tacklesWon, row, 2);
					}
					if (stats.getTackleLost().containsKey(bot))
					{
						int tacklesLost = stats.getTackleLost().get(bot).getCurrent();
						botStatTable.setValueAt(tacklesLost, row, 3);
					}
					
					if (stats.getBallPossessionTigers().containsKey(hwId))
					{
						double ballPossession = stats.getBallPossessionTigers().get(hwId).getPercent();
						botStatTable.setValueAt(df.format(ballPossession), row, 4);
					}
					
					for (PenaltyStats pStat : stats.getBestPenaltyShooterStats())
					{
						if (pStat.getBotID().equals(bot))
						{
							botStatTable.setValueAt(pStat.getSummedScore(), row, 5);
						}
					}
					
					row++;
				}
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
