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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
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
	private static final long	serialVersionUID			= -314343167523031597L;
	
	private JTextField			ballPossessionBoth		= null;
	private JTextField			ballPossessionOpponent	= null;
	private JTextField			ballPossessionTigers		= null;
	private JTextField			ballPossessionNoOne		= null;
	private JTextField			tackleWon					= null;
	private JTextField			tackleLost					= null;
	private JTextField			possibleGoalsTigers		= null;
	private JTextField			possibleGoalsOpponents	= null;
	
	private DecimalFormat		df								= new DecimalFormat("###.#%");
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public StatisticsPanel()
	{
		setLayout(new MigLayout("wrap"));
		
		ballPossessionBoth = new JTextField();
		ballPossessionBoth.setEditable(false);
		ballPossessionBoth.setBackground(Color.WHITE);
		
		ballPossessionTigers = new JTextField();
		ballPossessionTigers.setEditable(false);
		ballPossessionTigers.setBackground(Color.WHITE);
		
		ballPossessionOpponent = new JTextField();
		ballPossessionOpponent.setEditable(false);
		ballPossessionOpponent.setBackground(Color.WHITE);
		
		ballPossessionNoOne = new JTextField();
		ballPossessionNoOne.setEditable(false);
		ballPossessionNoOne.setBackground(Color.WHITE);
		
		tackleWon = new JTextField();
		tackleWon.setEditable(false);
		tackleWon.setBackground(Color.WHITE);
		
		tackleLost = new JTextField();
		tackleLost.setEditable(false);
		tackleLost.setBackground(Color.WHITE);
		
		possibleGoalsTigers = new JTextField();
		possibleGoalsTigers.setEditable(false);
		possibleGoalsTigers.setBackground(Color.WHITE);
		
		possibleGoalsOpponents = new JTextField();
		possibleGoalsOpponents.setEditable(false);
		possibleGoalsOpponents.setBackground(Color.WHITE);
		
		final JPanel ballPossessionPanel = new JPanel(new MigLayout("fill, inset 0",
				"[80,fill]10[80,fill]10[80,fill]10[80,fill]10[80,fill]"));
		ballPossessionPanel.add(new JLabel("Both:"));
		ballPossessionPanel.add(ballPossessionBoth);
		ballPossessionPanel.add(new JLabel("Tigers:"));
		ballPossessionPanel.add(ballPossessionTigers);
		ballPossessionPanel.add(new JLabel("Opponents:"));
		ballPossessionPanel.add(ballPossessionOpponent);
		ballPossessionPanel.add(new JLabel("No one:"));
		ballPossessionPanel.add(ballPossessionNoOne);
		
		final JPanel tacklePanel = new JPanel(new MigLayout("fill, inset 0", "[80,fill]10[80,fill]10[80,fill]"));
		tacklePanel.add(new JLabel("Tackle Won:"));
		tacklePanel.add(tackleWon);
		tacklePanel.add(new JLabel("Tackle Lost:"));
		tacklePanel.add(tackleLost);
		
		final JPanel possibelGoalsPanel = new JPanel(new MigLayout("fill, inset 0", "[80,fill]10[80,fill]10[80,fill]"));
		possibelGoalsPanel.add(new JLabel("Possible Goals "));
		possibelGoalsPanel.add(new JLabel("Tigers:"));
		possibelGoalsPanel.add(possibleGoalsTigers);
		possibelGoalsPanel.add(new JLabel("Opponents:"));
		possibelGoalsPanel.add(possibleGoalsOpponents);
		
		add(ballPossessionPanel);
		add(tacklePanel);
		add(possibelGoalsPanel);
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
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.BOTH))
				{
					ballPossessionBoth.setText(df.format(stats.getBallPossessionGeneral().get(EBallPossession.BOTH)
							.getPercent()));
				}
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.WE))
				{
					ballPossessionTigers.setText(df.format(stats.getBallPossessionGeneral().get(EBallPossession.WE)
							.getPercent()));
				}
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.THEY))
				{
					ballPossessionOpponent.setText(df.format(stats.getBallPossessionGeneral().get(EBallPossession.THEY)
							.getPercent()));
				}
				if (stats.getBallPossessionGeneral().containsKey(EBallPossession.NO_ONE))
				{
					ballPossessionNoOne.setText(df.format(stats.getBallPossessionGeneral().get(EBallPossession.NO_ONE)
							.getPercent()));
				}
				
				tackleLost.setText("" + stats.getTackleGeneralLost().getCurrent());
				tackleWon.setText("" + stats.getTackleGeneralWon().getCurrent());
				
				possibleGoalsTigers.setText("" + stats.getPossibleTigersGoals());
				possibleGoalsOpponents.setText("" + stats.getPossibleOpponentsGoals());
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
