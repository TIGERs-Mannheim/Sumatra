/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveStrategyPanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = -314343167523031597L;

	private JRadioButton yellowRadioButton;
	private JRadioButton blueRadioButton;

	private TeamOffensiveStrategyPanel yellowPanel;
	private TeamOffensiveStrategyPanel bluePanel;


	public OffensiveStrategyPanel()
	{
		setLayout(new MigLayout("fill"));

		yellowRadioButton = new JRadioButton("Yellow Team");
		blueRadioButton = new JRadioButton("Blue Team");

		yellowPanel = new TeamOffensiveStrategyPanel();
		bluePanel = new TeamOffensiveStrategyPanel();

		yellowRadioButton.setSelected(true);
		yellowRadioButton.addActionListener(e -> {
			if (blueRadioButton.isSelected())
			{
				blueRadioButton.setSelected(false);
				remove(getComponentCount() - 1);
				add(yellowPanel);
				invalidate();
			}
			yellowRadioButton.setSelected(true);
		});
		blueRadioButton.addActionListener(e -> {
			if (yellowRadioButton.isSelected())
			{
				yellowRadioButton.setSelected(false);
				remove(getComponentCount() - 1);
				add(bluePanel);
				invalidate();
			}
			blueRadioButton.setSelected(true);
		});

		JPanel teamSelectionPanel = new JPanel(new MigLayout());
		teamSelectionPanel.add(yellowRadioButton);
		teamSelectionPanel.add(blueRadioButton);
		add(teamSelectionPanel, "wrap");
		add(yellowPanel);
	}


	/**
	 * @return TeamOffensiveStrategyPanel of the yellow team
	 */
	public TeamOffensiveStrategyPanel getYellowStrategyPanel()
	{
		return yellowPanel;
	}


	/**
	 * @return TeamOffensiveStrategyPanel of the blue tem
	 */
	public TeamOffensiveStrategyPanel getBlueStrategyPanel()
	{
		return bluePanel;
	}
}
