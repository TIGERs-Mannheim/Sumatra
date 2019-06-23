/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class CommandPanel extends JPanel {

	private ACommandDetailsPanel detailsPanel = null;

	/**
	 * Creates a new CommandPanel
	 */
	public CommandPanel() {

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Details"));

		JPanel buttonPanel = new JPanel(new FlowLayout());

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new SaveAction());
		buttonPanel.add(btnSave);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Reload the command panel
	 * 
	 * @param detailsPanel
	 */
	public void setCommandDetailsPanel(ACommandDetailsPanel detailsPanel) {

		if (this.detailsPanel != null) {
			remove(this.detailsPanel);
		}

		this.detailsPanel = detailsPanel;
		add(detailsPanel);

		revalidate();
		repaint();
	}

	/**
	 * Removes the current details panel.
	 */
	public void clearCommandDetailsPanel() {

		if (this.detailsPanel != null) {
			remove(this.detailsPanel);
		}

		revalidate();
		repaint();
	}

	class SaveAction implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {

			if (detailsPanel == null) {
				return;
			}

			detailsPanel.onSave();
		}
	}

}
