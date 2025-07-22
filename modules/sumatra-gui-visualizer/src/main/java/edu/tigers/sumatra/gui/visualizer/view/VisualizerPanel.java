/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.visualizer.view;

import edu.tigers.sumatra.gui.visualizer.view.field.FieldPanel;
import edu.tigers.sumatra.gui.visualizer.view.options.ShapeSelectionPanel;
import edu.tigers.sumatra.gui.visualizer.view.toolbar.VisualizerToolbar;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
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
	@Getter
	private final JSplitPane splitPane = new JSplitPane();


	public VisualizerPanel()
	{
		setLayout(new BorderLayout());

		add(toolbar, BorderLayout.PAGE_START);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(fieldPanel);
		splitPane.setRightComponent(shapeSelectionPanel);
		add(splitPane, BorderLayout.CENTER);

		splitPane.setResizeWeight(1);
	}
}
