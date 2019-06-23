/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.view.testplays.panels;

import java.awt.*;

import javax.swing.*;

import edu.tigers.sumatra.testplays.commands.ACommand;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public abstract class ACommandDetailsPanel extends JPanel {

	private GridBagConstraints gbc = new GridBagConstraints();

	protected ACommandDetailsPanel(ACommand.CommandType commandType) {

		setLayout(new GridBagLayout());

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;

		final JLabel commandTypeValue = new JLabel(commandType.name());
		addLine(new JLabel("Command type:"), commandTypeValue);
	}

	protected GridBagConstraints getGridBagConstraints() {

		return gbc;
	}

	private void addLine() {
		gbc.gridy++;
		gbc.gridx = 1;
	}

	protected void addLine(Component comp1, Component comp2) {

		addLine(comp1, gbc, comp2, gbc);
	}

	protected void addLine(Component comp1, GridBagConstraints gbc1, Component comp2) {

		addLine(comp1, gbc1, comp2, gbc);
	}

	protected void addLine(Component comp1, Component comp2, GridBagConstraints gbc2) {

		addLine(comp1, gbc, comp2, gbc2);
	}

	protected void addLine(Component comp1, GridBagConstraints gbc1, Component comp2, GridBagConstraints gbc2) {

		addLine();

		gbc1.gridy = gbc.gridy;
		gbc1.gridx = 0;
		add(comp1, gbc);

		gbc2.gridy = gbc.gridy;
		gbc2.gridx = 1;
		add(comp2, gbc);
	}

	protected void addFillPanel() {

		GridBagConstraints constraints = getGridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 0.5;

		addLine(new JPanel(), new JPanel(), constraints);
	}

	abstract void onSave();

}
