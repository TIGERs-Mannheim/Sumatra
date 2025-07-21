/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.strategy.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveStrategyPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private final JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
	private final JRadioButton blueRadioButton = new JRadioButton("Blue Team");

	@Getter
	private final TeamOffensiveStrategyPanel yellowPanel = new TeamOffensiveStrategyPanel();
	@Getter
	private final TeamOffensiveStrategyPanel bluePanel = new TeamOffensiveStrategyPanel();


	public OffensiveStrategyPanel()
	{
		setLayout(new MigLayout("fill"));

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
}
