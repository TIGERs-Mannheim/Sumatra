/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.RoundedCornerBorder;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class GameStatePanel extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= -3660600221977543004L;
	
	private JLabel					stateLabel			= new JLabel();
	private JTeamLabel			teamLabel			= new JTeamLabel();
	
	
	/**
	 * @param font
	 */
	public GameStatePanel(final Font font)
	{
		setupUI(font);
	}
	
	
	private void setupUI(final Font font)
	{
		teamLabel.setVisible(false);
		teamLabel.setFont(font);
		
		/*
		 * The team label is slightly larger than the regular state label due to the added border. This would normally
		 * cause the layout to shift vertically whenever the team label is displayed or set to invisible. We avoid this by
		 * setting the same border on the state label as invisible border. The invisible border takes on the size of the
		 * border that it delegates for without actually painting anything and serves as placeholder with the same size
		 * characteristics.
		 */
		Border teamLabelBorder = new RoundedCornerBorder(10, 5, Color.BLACK);
		stateLabel.setBorder(new InvisibleBorder(teamLabelBorder));
		stateLabel.setFont(font);
		stateLabel.setText("Unknown");
		
		setLayout(new FlowLayout());
		add(stateLabel);
		add(teamLabel);
	}
	
	
	/**
	 * @param state
	 */
	public void setState(final EGameStateNeutral state)
	{
		stateLabel.setText(getStateText(state));
		
		ETeamColor stateTeam = state.getTeamColor();
		if (stateTeam.isNonNeutral())
		{
			showTeamLabel(stateTeam);
		} else
		{
			hideTeamLabel();
		}
	}
	
	
	private void showTeamLabel(final ETeamColor team)
	{
		teamLabel.setVisible(true);
		teamLabel.setTeam(team);
	}
	
	
	private void hideTeamLabel()
	{
		teamLabel.setVisible(false);
	}
	
	
	private String getStateText(final EGameStateNeutral state)
	{
		switch (state)
		{
			case BALL_PLACEMENT_BLUE:
			case BALL_PLACEMENT_YELLOW:
				return "Ball Placement by ";
			case BREAK:
				return "Break";
			case DIRECT_KICK_BLUE:
			case DIRECT_KICK_YELLOW:
				return "Direct Kick for ";
			case HALTED:
				return "Game halted";
			case INDIRECT_KICK_BLUE:
			case INDIRECT_KICK_YELLOW:
				return "Indirect Kick for ";
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
				return "Prep. Kickoff for ";
			case KICKOFF_BLUE:
			case KICKOFF_YELLOW:
				return "Kickoff for ";
			case PENALTY_BLUE:
			case PENALTY_YELLOW:
				return "Penalty Kick for ";
			case POST_GAME:
				return "Game over";
			case PREPARE_PENALTY_BLUE:
			case PREPARE_PENALTY_YELLOW:
				return "Prep. Penalty Kick ";
			case RUNNING:
				return "Game running";
			case STOPPED:
				return "Game stopped";
			case TIMEOUT_BLUE:
			case TIMEOUT_YELLOW:
				return "Timeout for ";
			case UNKNOWN:
				return "Game state undetermined";
			default:
				return state.toString();
		}
	}
	
	private static class InvisibleBorder implements Border
	{
		
		private final Border	delegate;
		
		
		public InvisibleBorder(final Border delegate)
		{
			this.delegate = delegate;
		}
		
		
		@Override
		public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width,
				final int height)
		{
		}
		
		
		@Override
		public Insets getBorderInsets(final Component c)
		{
			return delegate.getBorderInsets(c);
		}
		
		
		@Override
		public boolean isBorderOpaque()
		{
			return false;
		}
		
	}
}
