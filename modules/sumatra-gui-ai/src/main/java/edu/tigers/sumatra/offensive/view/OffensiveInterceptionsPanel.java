/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveInterceptionsPanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = -314343167523031597L;

	private static final String MIG_LAYOUT_DEFAULT = "dock center, span 4";

	private final JRadioButton yellowRadioButton;
	private final JRadioButton blueRadioButton;

	private final TeamOffensiveInterceptionsPanel yellowPanel;
	private final TeamOffensiveInterceptionsPanel bluePanel;


	/**
	 * Offensive Interception Panel
	 */
	public OffensiveInterceptionsPanel()
	{
		setLayout(new MigLayout());

		yellowRadioButton = new JRadioButton("Yellow Team");
		blueRadioButton = new JRadioButton("Blue Team");

		yellowPanel = new TeamOffensiveInterceptionsPanel();
		bluePanel = new TeamOffensiveInterceptionsPanel();

		yellowRadioButton.setSelected(true);
		yellowRadioButton.addActionListener(e -> {
			if (blueRadioButton.isSelected())
			{
				blueRadioButton.setSelected(false);
				remove(2);
				add(yellowPanel, MIG_LAYOUT_DEFAULT);
				revalidate();
				repaint();
			}
			yellowRadioButton.setSelected(true);
		});
		blueRadioButton.addActionListener(e -> {
			if (yellowRadioButton.isSelected())
			{
				yellowRadioButton.setSelected(false);
				remove(2);
				add(bluePanel, MIG_LAYOUT_DEFAULT);
				revalidate();
				repaint();
			}
			blueRadioButton.setSelected(true);
		});

		setBorder(BorderFactory.createTitledBorder("Bot Interceptions"));
		add(yellowRadioButton, "wrap");
		add(blueRadioButton, "wrap");
		add(yellowPanel, MIG_LAYOUT_DEFAULT);
	}


	/**
	 * @return TeamOffensiveStrategyPanel of the yellow team
	 */
	public TeamOffensiveInterceptionsPanel getYellowStrategyPanel()
	{
		return yellowPanel;
	}


	/**
	 * @return TeamOffensiveStrategyPanel of the blue tem
	 */
	public TeamOffensiveInterceptionsPanel getBlueStrategyPanel()
	{
		return bluePanel;
	}
}
