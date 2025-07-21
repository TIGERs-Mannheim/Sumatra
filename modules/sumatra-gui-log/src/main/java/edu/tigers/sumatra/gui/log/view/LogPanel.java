/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.log.view;

import edu.tigers.sumatra.view.TextPane;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.Level;

import javax.swing.JPanel;
import java.io.Serial;


/**
 * Panel for logs
 */
public class LogPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 1L;

	@Getter
	private final TextPane textPane;
	@Getter
	private final FilterPanel filterPanel;


	public LogPanel(final int maxCapacity, final Level initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));

		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel(initialLevel);

		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");


		add(display, "grow");
	}
}
