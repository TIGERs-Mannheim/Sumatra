/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStatisticsPanel extends JPanel implements ISumatraView
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long					serialVersionUID	= -314343167523031597L;
	
	private JRadioButton							yellowRadioButton	= null;
	private JRadioButton							blueRadioButton	= null;
	private JRadioButton							displayCurrentFrameButton = null;

	private TeamOffensiveStatisticsPanel	yellowPanel			= null;
	private TeamOffensiveStatisticsPanel	bluePanel			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Offensive Strategy Panel
	 */
	public OffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());
		
		yellowRadioButton = new JRadioButton("Yellow Team");
		blueRadioButton = new JRadioButton("Blue Team");
		displayCurrentFrameButton = new JRadioButton("Show current Frame only");
		displayCurrentFrameButton.addActionListener(e -> {
			bluePanel.setCurrentFrameOnly(displayCurrentFrameButton.isSelected());
			yellowPanel.setCurrentFrameOnly(displayCurrentFrameButton.isSelected());
        });


		yellowPanel = new TeamOffensiveStatisticsPanel();
		bluePanel = new TeamOffensiveStatisticsPanel();
		
		yellowRadioButton.setSelected(true);
		yellowRadioButton.addActionListener(e -> {
			if (blueRadioButton.isSelected())
			{
				blueRadioButton.setSelected(false);
				remove(3);
				add(yellowPanel, "span");
				revalidate();
				repaint();
			}
			yellowRadioButton.setSelected(true);
		});
		blueRadioButton.addActionListener(e -> {
			if (yellowRadioButton.isSelected())
			{
				yellowRadioButton.setSelected(false);
				remove(3);
				add(bluePanel, "span");
				revalidate();
				repaint();
			}
			blueRadioButton.setSelected(true);
		});
		
		add(yellowRadioButton);
		add(blueRadioButton);
		add(displayCurrentFrameButton, "wrap");
		add(yellowPanel, "span");
	}
	
	
	/**
	 * @return TeamOffensiveStrategyPanel of the yellow team
	 */
	public TeamOffensiveStatisticsPanel getYellowStrategyPanel()
	{
		return yellowPanel;
	}
	
	
	/**
	 * @return TeamOffensiveStrategyPanel of the blue tem
	 */
	public TeamOffensiveStatisticsPanel getBlueStrategyPanel()
	{
		return bluePanel;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>();
	}
}
