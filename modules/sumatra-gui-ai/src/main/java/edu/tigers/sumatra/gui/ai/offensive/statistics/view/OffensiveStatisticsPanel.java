/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.statistics.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Dimension;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveStatisticsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private static final Dimension MAX = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private static final Dimension HIDDEN = new Dimension();

	private final JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
	private final JRadioButton blueRadioButton = new JRadioButton("Blue Team");
	private final JComboBox<String> passTypeFilter = new JComboBox<>(
			new String[] { "All passes", "Redirect", "Receive  / Dont care" });

	@Getter
	private final TeamOffensiveStatisticsPanel yellowPanel = new TeamOffensiveStatisticsPanel();
	@Getter
	private final TeamOffensiveStatisticsPanel bluePanel = new TeamOffensiveStatisticsPanel();


	public OffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());

		yellowRadioButton.setSelected(true);
		yellowRadioButton.addActionListener(e -> {
			if (blueRadioButton.isSelected())
			{
				blueRadioButton.setSelected(false);
				yellowPanel.setMaximumSize(MAX);
				bluePanel.setMaximumSize(HIDDEN);
				revalidate();
			}
			yellowRadioButton.setSelected(true);
		});
		blueRadioButton.addActionListener(e -> {
			if (yellowRadioButton.isSelected())
			{
				yellowRadioButton.setSelected(false);
				yellowPanel.setMaximumSize(HIDDEN);
				bluePanel.setMaximumSize(MAX);
				revalidate();
			}
			blueRadioButton.setSelected(true);
		});

		passTypeFilter.addActionListener(e -> {
			int selection = passTypeFilter.getSelectedIndex();
			bluePanel.setFilterRegular(false);
			bluePanel.setFilterRedirects(false);
			yellowPanel.setFilterRegular(false);
			yellowPanel.setFilterRedirects(false);
			switch (selection)
			{
				case 1:
					bluePanel.setFilterRedirects(true);
					yellowPanel.setFilterRedirects(true);
					break;
				case 2:
					bluePanel.setFilterRegular(true);
					yellowPanel.setFilterRegular(true);
					break;
				default:
			}

			revalidate();
			repaint();
		});

		add(yellowRadioButton);
		add(blueRadioButton);
		add(passTypeFilter, "wrap");
		add(yellowPanel, "span");
		add(bluePanel, "span");
		bluePanel.setMaximumSize(HIDDEN);
	}
}
