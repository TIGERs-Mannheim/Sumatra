/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.toolbar;

import edu.tigers.sumatra.util.ImageScaler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


@Log4j2
public class VisualizerToolbar extends JToolBar
{
	@Getter
	private final JToggleButton fancyDrawing = new JToggleButton();
	@Getter
	private final JToggleButton darkMode = new JToggleButton();
	@Getter
	private final JToggleButton borderOffset = new JToggleButton();

	@Getter
	private final JButton showShortcuts = new JButton();
	@Getter
	private final JButton captureSettings = new JButton();

	@Getter
	private final JButton turnCounterClockwise = new JButton();
	@Getter
	private final JButton turnClockwise = new JButton();
	@Getter
	private final JButton resetField = new JButton();

	@Getter
	private final JToggleButton recordVideoFull = new JToggleButton();
	@Getter
	private final JToggleButton recordVideoSelection = new JToggleButton();
	@Getter
	private final JButton takeScreenshotFull = new JButton();
	@Getter
	private final JButton takeScreenshotSelection = new JButton();

	@Getter
	private final JToggleButton shapeSelection = new JToggleButton();

	@Getter
	private final CaptureSettingsDialog captureSettingsDialog = new CaptureSettingsDialog();


	public VisualizerToolbar()
	{
		setFloatable(false);

		fancyDrawing.setIcon(ImageScaler.scaleSmallButtonImageIcon(
				"/icons8-cool-expression-emoji-wearing-sunshades-shared-online-48.png"));
		fancyDrawing.setToolTipText("Enable fancy drawing (antialiasing and interpolation)");
		add(fancyDrawing);

		darkMode.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-dark-67.png"));
		darkMode.setToolTipText("Enable dark mode");
		add(darkMode);

		borderOffset.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-horizontal-line-64.png"));
		borderOffset.setToolTipText("Add an offset");
		add(borderOffset);

		addSeparator();

		showShortcuts.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-shortcut-50.png"));
		showShortcuts.setToolTipText("Show shortcuts");
		showShortcuts.addActionListener(e -> new VisualizerShortcutsDialog());
		add(showShortcuts);

		captureSettings.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-settings-64.png"));
		captureSettings.setToolTipText("Capture settings");
		captureSettings.addActionListener(e -> captureSettingsDialog.setVisible(true));
		add(captureSettings);

		addSeparator();

		turnCounterClockwise.setIcon(ImageScaler.scaleSmallButtonImageIcon("/turn-up-solid.png"));
		turnCounterClockwise.setToolTipText("Turn field 90 degrees counter clockwise");
		add(turnCounterClockwise);

		turnClockwise.setIcon(ImageScaler.scaleSmallButtonImageIcon("/turn-down-solid.png"));
		turnClockwise.setToolTipText("Turn field 90 degrees clockwise");
		add(turnClockwise);

		resetField.setIcon(ImageScaler.scaleSmallButtonImageIcon("/chalkboard-solid.png"));
		resetField.setToolTipText("Reset field view");
		add(resetField);

		addSeparator();

		recordVideoFull.setIcon(ImageScaler.scaleSmallButtonImageIcon("/record-display-solid.png"));
		recordVideoFull.setSelectedIcon(ImageScaler.scaleSmallButtonImageIcon("/recordActive.gif"));
		recordVideoFull.setToolTipText("Record a video of the visualizer (full field)");
		add(recordVideoFull);

		recordVideoSelection.setIcon(ImageScaler.scaleSmallButtonImageIcon("/record-camera-solid.png"));
		recordVideoSelection.setSelectedIcon(ImageScaler.scaleSmallButtonImageIcon("/recordActive.gif"));
		recordVideoSelection.setToolTipText("Record a video of the visualizer (current selection)");
		add(recordVideoSelection);

		takeScreenshotFull.setIcon(ImageScaler.scaleSmallButtonImageIcon("/display-solid.png"));
		takeScreenshotFull.setToolTipText("Take screenshot (full field)");
		add(takeScreenshotFull);

		takeScreenshotSelection.setIcon(ImageScaler.scaleSmallButtonImageIcon("/camera-solid.png"));
		takeScreenshotSelection.setToolTipText("Take screenshot (current selection)");
		add(takeScreenshotSelection);

		addSeparator();

		shapeSelection.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-shape-64.png"));
		shapeSelection.setToolTipText("Show/hide the shape selection tree (ctrl + .)");
		add(shapeSelection);
	}
}
