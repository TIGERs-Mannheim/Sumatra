/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.kick.view;

import lombok.Getter;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;


/**
 * Panel for ball kick identification
 */
@Getter
public class BallKickIdentPanel extends JPanel
{
	private final JTabbedPane tabs = new JTabbedPane();
	private final JCheckBox chkCollectKickIdents = new JCheckBox("Collect kick ident samples");


	public BallKickIdentPanel()
	{
		setLayout(new BorderLayout());

		JLabel howTo = new JLabel();
		howTo.setText("<html>"
				+ "1. Enable collection of kick ident samples on the left<br>"
				+ "2. Use StraightChipKickSamplerRole or KickSampleSkill to generate data<br>"
				+ "3. Carefully select the best data samples<br>"
				+ "4. Copy the identified parameters to the respective models<br>"
				+ "Note: Determine ball rolling acceleration before chip model sampling"
				+ "</html>");
		Font bigFont = new Font("Arial", Font.PLAIN, 14);
		howTo.setFont(bigFont);
		TitledBorder border = BorderFactory.createTitledBorder("How-To");
		border.setTitleFont(bigFont);
		howTo.setBorder(border);
		howTo.setPreferredSize(new Dimension(480, 120));

		chkCollectKickIdents.setFont(new Font("Arial", Font.BOLD, 14));

		JPanel top = new JPanel(new BorderLayout());
		top.add(chkCollectKickIdents, BorderLayout.WEST);
		top.add(howTo, BorderLayout.EAST);

		add(top, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
	}


	public JTabbedPane getTabs()
	{
		return tabs;
	}
}
