package edu.tigers.sumatra.gui.visualizer.view.toolbar;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;


@Log4j2
public class VisualizerToolbar extends JToolBar
{
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

	private boolean initialized = false;


	public VisualizerToolbar()
	{
		setFloatable(false);

		updateIcons();

		// tooltips
		darkMode.setToolTipText("Toggle dark mode");
		borderOffset.setToolTipText("Add offset");

		showShortcuts.setToolTipText("Show shortcuts");
		captureSettings.setToolTipText("Capture settings");

		turnCounterClockwise.setToolTipText("Rotate field 90° counter clockwise");
		turnClockwise.setToolTipText("Rotate field 90° clockwise");
		resetField.setToolTipText("Reset field view");

		recordVideoFull.setToolTipText("Record full field video");
		recordVideoSelection.setToolTipText("Record selection video");

		takeScreenshotFull.setToolTipText("Screenshot full field");
		takeScreenshotSelection.setToolTipText("Screenshot selection");

		shapeSelection.setToolTipText("Toggle shape selection tree");

		// actions
		captureSettings.addActionListener(e -> captureSettingsDialog.setVisible(true));
		showShortcuts.addActionListener(e -> new VisualizerShortcutsDialog());

		// add
		add(darkMode);
		add(borderOffset);

		addSeparator();

		add(showShortcuts);
		add(captureSettings);

		addSeparator();

		add(turnCounterClockwise);
		add(turnClockwise);
		add(resetField);

		addSeparator();

		add(recordVideoFull);
		add(recordVideoSelection);
		add(takeScreenshotFull);
		add(takeScreenshotSelection);

		addSeparator();

		add(shapeSelection);

		initialized = true;
	}


	private void updateLookAndFeelForDialog()
	{
		SwingUtilities.updateComponentTreeUI(captureSettingsDialog);
	}


	@Override
	public void updateUI()
	{
		super.updateUI();
		updateIcons();
		if (captureSettingsDialog != null)
		{
			updateLookAndFeelForDialog();
		}
	}


	private void updateIcons()
	{
		if (!initialized)
		{
			return;
		}

		Color c = UIManager.getColor("Label.foreground");

		darkMode.setIcon(IconFontSwing.buildIcon(FontAwesome.ADJUST, 16, c));

		borderOffset.setIcon(IconFontSwing.buildIcon(FontAwesome.ARROWS_V, 16, c));

		showShortcuts.setIcon(IconFontSwing.buildIcon(FontAwesome.KEYBOARD_O, 16, c));

		captureSettings.setIcon(
				IconFontSwing.buildIcon(FontAwesome.COG, 16, c)
		);

		// counter clockwise
		turnCounterClockwise.setIcon(
				IconFontSwing.buildIcon(FontAwesome.UNDO, 16, c)
		);

		// clockwise
		turnClockwise.setIcon(
				IconFontSwing.buildIcon(FontAwesome.REPEAT, 16, c)
		);

		resetField.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 16, c));

		recordVideoFull.setIcon(IconFontSwing.buildIcon(FontAwesome.VIDEO_CAMERA, 16, c));
		recordVideoSelection.setIcon(IconFontSwing.buildIcon(FontAwesome.VIDEO_CAMERA, 12, c));

		takeScreenshotFull.setIcon(IconFontSwing.buildIcon(FontAwesome.CAMERA, 16, c));
		takeScreenshotSelection.setIcon(IconFontSwing.buildIcon(FontAwesome.CAMERA, 12, c));

		shapeSelection.setIcon(IconFontSwing.buildIcon(FontAwesome.OBJECT_GROUP, 16, c));
	}
}