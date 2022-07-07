/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.visualizer.field.FieldPanel;
import edu.tigers.sumatra.visualizer.options.ShapeSelectionPanel;
import edu.tigers.sumatra.visualizer.toolbar.VisualizerToolbar;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.Serial;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 */
@Log4j2
public class VisualizerPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 2686191777355388548L;

	@Getter
	private final FieldPanel fieldPanel = new FieldPanel();
	@Getter
	private final VisualizerToolbar toolbar = new VisualizerToolbar();
	@Getter
	private final ShapeSelectionPanel shapeSelectionPanel = new ShapeSelectionPanel();


	public VisualizerPanel()
	{
		setLayout(new BorderLayout());

		add(toolbar, BorderLayout.PAGE_START);
		add(fieldPanel, BorderLayout.CENTER);
		add(shapeSelectionPanel, BorderLayout.EAST);
	}
}
