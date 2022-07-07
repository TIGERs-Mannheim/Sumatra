/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveStatisticsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private final JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
	private final JRadioButton blueRadioButton = new JRadioButton("Blue Team");
	private final JRadioButton displayCurrentFrameButton = new JRadioButton("Show current Frame only");

	@Getter
	private final TeamOffensiveStatisticsPanel yellowPanel = new TeamOffensiveStatisticsPanel();
	@Getter
	private final TeamOffensiveStatisticsPanel bluePanel = new TeamOffensiveStatisticsPanel();


	public OffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());

		displayCurrentFrameButton.addActionListener(e -> {
			bluePanel.setCurrentFrameOnly(displayCurrentFrameButton.isSelected());
			yellowPanel.setCurrentFrameOnly(displayCurrentFrameButton.isSelected());
		});

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
}
