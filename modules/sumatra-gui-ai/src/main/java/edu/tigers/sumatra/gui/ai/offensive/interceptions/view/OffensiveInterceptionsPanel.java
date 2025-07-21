/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.interceptions.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveInterceptionsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private static final String MIG_LAYOUT_DEFAULT = "dock center, span 4";

	private final JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
	private final JRadioButton blueRadioButton = new JRadioButton("Blue Team");

	@Getter
	private final TeamOffensiveInterceptionsPanel yellowPanel = new TeamOffensiveInterceptionsPanel();
	@Getter
	private final TeamOffensiveInterceptionsPanel bluePanel = new TeamOffensiveInterceptionsPanel();


	/**
	 * Offensive Interception Panel
	 */
	public OffensiveInterceptionsPanel()
	{
		setLayout(new MigLayout());

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
}
