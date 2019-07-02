/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import javax.swing.JPanel;

import org.apache.log4j.Priority;

import edu.tigers.sumatra.view.TextPane;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Panel for logs
 */
public class LogPanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = 1L;

	private final TextPane textPane;
	private final FilterPanel filterPanel;


	public LogPanel(final int maxCapacity, final Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));

		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel(initialLevel);

		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");


		add(display, "grow");
	}


	public TextPane getTextPane()
	{
		return textPane;
	}


	public FilterPanel getFilterPanel()
	{
		return filterPanel;
	}


	public SlidePanel getSlidePanel()
	{
		return filterPanel.getSlidePanel();
	}
}
