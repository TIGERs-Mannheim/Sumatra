/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.timer.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.io.Serial;


/**
 * Timer main panel
 */
public class TimerPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -4840668605222003132L;

	@Getter
	private final TimerChartPanel chartPanel = new TimerChartPanel();


	public TimerPanel()
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));

		chartPanel.setVisible(false);

		add(chartPanel, "grow");
	}
}
