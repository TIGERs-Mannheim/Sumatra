/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Visualizes all available robots.
 */
public class VisualizerOptionsMenu extends JMenuBar
{
	static final String SOURCE_PREFIX = "SOURCE_";
	static final String CATEGORY_PREFIX = "CATEGORY_";
	private final List<IOptionsPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final Map<String, JMenu> parentMenus = new HashMap<>();
	private final List<JCheckBoxMenuItem> checkBoxes = new ArrayList<>();
	private final Set<IShapeLayer> knownShapeLayers = new HashSet<>();
	private final CheckboxListener checkboxListener;
	private final List<String> fixedMenuEntries = new ArrayList<>();

	private final JMenu pSources;
	private final Map<String, JMenu> pSourcesSub;

	private JTextField wTextField = new JTextField("1024");
	private JMenuItem btnScreenshot = new JMenuItem("Take screenshot");
	private JCheckBoxMenuItem normalizeFieldBtn = new JCheckBoxMenuItem("Follow visualizer motion");
	private JMenuItem recordVideoStart = new JMenuItem("Start recording video");
	private JMenuItem recordVideoStop = new JMenuItem("Stop recording video");
	private IMediaRecorderListener mediaRecordingListener;


	/**
	 * Default
	 */
	public VisualizerOptionsMenu()
	{
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();

		JMenu pActions = new JMenu("Visualizer");
		add(pActions);
		parentMenus.put(pActions.getText(), pActions);
		fixedMenuEntries.add(pActions.getText());

		JMenuItem btnTurn = new JMenuItem("Turn");
		btnTurn.addActionListener(new TurnFieldListener());
		JMenuItem btnReset = new JMenuItem("Reset");
		btnReset.addActionListener(new ResetFieldListener());
		pActions.add(btnTurn);
		pActions.add(btnReset);

		addMenuEntry(EVisualizerOptions.FANCY);
		addMenuEntry(EVisualizerOptions.DARK);

		JMenu pShortcuts = new JMenu("Shortcuts");
		add(pShortcuts);
		fixedMenuEntries.add(pShortcuts.getText());
		pShortcuts.add(new JMenuItem("Left mouse click:"));
		pShortcuts.add(new JMenuItem("  ctrl: Look at Ball"));
		pShortcuts.add(new JMenuItem("  shift: Kick to"));
		pShortcuts.add(new JMenuItem("  ctrl+shift: Follow mouse"));
		pShortcuts.add(new JMenuItem("  alt: ruler"));

		pShortcuts.add(new JMenuItem("Right mouse click:"));
		pShortcuts.add(new JMenuItem("  none: place ball"));
		pShortcuts.add(new JMenuItem("  ctrl: 8m/s to target"));
		pShortcuts.add(new JMenuItem("  shift: stop at target"));
		pShortcuts.add(new JMenuItem("  ctrl+shift: 2m/s at target"));
		pShortcuts.add(new JMenuItem("  above combinations + alt: chip ball"));

		pActions.addSeparator();
		JMenu mediaSettings = new JMenu("Media Settings");
		pActions.add(mediaSettings);
		mediaSettings.add(new JLabel("width:"));
		mediaSettings.add(wTextField);
		mediaSettings.add(normalizeFieldBtn);
		pActions.addSeparator();
		pActions.add(btnScreenshot);
		pActions.addSeparator();

		recordVideoStop.setEnabled(false);

		btnScreenshot.addActionListener(e -> takeScreenshotPressed());
		recordVideoStart.addActionListener(e -> startRecordingPressed());
		recordVideoStop.addActionListener(e -> stopRecordingPressed());

		pActions.add(recordVideoStart);
		pActions.add(recordVideoStop);
		pSources = new JMenu("Sources");
		add(pSources);
		fixedMenuEntries.add(pSources.getText());

		pSourcesSub = new HashMap<>();
		pSourcesSub.put("general", pSources);
	}


	private void stopRecordingPressed()
	{
		mediaRecordingListener.stopRecordingVideo();
		recordVideoStop.setEnabled(false);
		recordVideoStart.setEnabled(true);
		normalizeFieldBtn.setEnabled(true);
		wTextField.setEnabled(true);
		btnScreenshot.setEnabled(true);
	}


	private void startRecordingPressed()
	{
		EMediaOption option = !normalizeFieldBtn.isSelected() ?
				EMediaOption.FULL_FIELD :
				EMediaOption.CURRENT_SECTION;
		mediaRecordingListener.setMediaParameters(Integer.parseInt(wTextField.getText()), 0, option);
		if (mediaRecordingListener.startRecordingVideo())
		{
			recordVideoStart.setEnabled(false);
			normalizeFieldBtn.setEnabled(false);
			wTextField.setEnabled(false);
			recordVideoStop.setEnabled(true);
			btnScreenshot.setEnabled(false);
		}
	}


	private void takeScreenshotPressed()
	{
		EMediaOption option = !normalizeFieldBtn.isSelected() ?
				EMediaOption.FULL_FIELD :
				EMediaOption.CURRENT_SECTION;
		mediaRecordingListener.setMediaParameters(Integer.parseInt(wTextField.getText()), 0, option);
		mediaRecordingListener.takeScreenshot();
	}


	/**
	 * Update with latest set of layers. Existing layers will not be removed
	 *
	 * @param source
	 */
	public synchronized void addSourceMenuIfNotPresent(ShapeMapSource source)
	{
		if (missesMenuItem(source.getName(), SOURCE_PREFIX))
		{
			String category = source.getCategories().stream().findFirst().orElse("general");
			if (!pSourcesSub.containsKey(category))
			{
				final JMenu catMenu = new JMenu(category);
				pSourcesSub.put(category, catMenu);
				pSources.add(catMenu);
			}

			checkBoxes.add(
					createCheckBox(source.getName(), SOURCE_PREFIX + source.getName(), true, pSourcesSub.get(category)));
		}

		for (String category : source.getCategories())
		{
			if (missesMenuItem(category, CATEGORY_PREFIX))
			{
				checkBoxes.add(createCheckBox("All " + category, CATEGORY_PREFIX + category, true, pSources));
			}
		}
	}


	public void setMediaRecordingListener(IMediaRecorderListener listener)
	{
		this.mediaRecordingListener = listener;
	}


	private boolean missesMenuItem(String name, String prefix)
	{
		for (JMenu menu : pSourcesSub.values())
		{
			for (int i = 0; i < menu.getItemCount(); i++)
			{
				JMenuItem item = menu.getItem(i);
				if (item.getActionCommand().equals(prefix + name))
				{
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * @param shapeLayer
	 */
	public synchronized void addMenuEntry(final IShapeLayer shapeLayer)
	{
		if (knownShapeLayers.contains(shapeLayer))
		{
			return;
		}
		JMenu parent = parentMenus.get(shapeLayer.getCategory());
		if (parent == null)
		{
			parent = new JMenu(shapeLayer.getCategory());
			parentMenus.put(shapeLayer.getCategory(), parent);
			while(getMenuCount() > fixedMenuEntries.size()) {
				remove(fixedMenuEntries.size());
			}
			parentMenus.entrySet().stream()
					.filter(e -> !fixedMenuEntries.contains(e.getKey()))
					.sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).forEach(this::add);
		}

		knownShapeLayers.add(shapeLayer);
		checkBoxes.add(createCheckBox(shapeLayer, parent));
	}


	private JCheckBoxMenuItem createCheckBox(final IShapeLayer option, final JMenu parent)
	{
		return createCheckBox(option.getLayerName(), option.getId(), option.isVisibleByDefault(), parent);
	}


	private JCheckBoxMenuItem createCheckBox(final String name, String actionCommand, boolean visible,
			final JMenu parent)
	{
		JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(name);
		checkbox.addActionListener(checkboxListener);
		checkbox.setActionCommand(actionCommand);
		insertMenuItem(name, parent, checkbox);

		String value = SumatraModel.getInstance().getUserProperty(
				OptionsPanelPresenter.class.getCanonicalName() + "." + checkbox.getActionCommand());
		if (value == null)
		{
			checkbox.setSelected(visible);
		} else
		{
			boolean selected = Boolean.parseBoolean(value);
			checkbox.setSelected(selected);
		}
		for (final IOptionsPanelObserver o : observers)
		{
			o.onCheckboxClick(checkbox.getActionCommand(), checkbox.isSelected());
		}

		return checkbox;
	}


	private void insertMenuItem(final String name, final JMenu parent, final JCheckBoxMenuItem checkbox)
	{
		for (int i = 0; i < parent.getItemCount(); i++)
		{
			final JMenuItem item = parent.getItem(i);
			if (!(item instanceof JCheckBoxMenuItem))
			{
				continue;
			}
			JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) item;
			if (checkBoxMenuItem.getText() != null && item.getText().compareToIgnoreCase(name) >= 0)
			{
				parent.insert(checkbox, i);
				return;
			}
		}
		parent.add(checkbox);
	}


	/**
	 * initialize button states
	 */
	public synchronized void setInitialButtonState()
	{
		for (JCheckBoxMenuItem chk : checkBoxes)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onCheckboxClick(chk.getActionCommand(), chk.isSelected());
			}
		}
	}


	/**
	 * @param enable
	 */
	public synchronized void setButtonsEnabled(final boolean enable)
	{
		for (JCheckBoxMenuItem cb : checkBoxes)
		{
			cb.setEnabled(enable);
		}
	}


	/**
	 * @param o
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public void addObserver(final IOptionsPanelObserver o)
	{
		observers.add(o);
	}


	/**
	 * @param o
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public void removeObserver(final IOptionsPanelObserver o)
	{
		observers.remove(o);
	}


	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------

	protected class CheckboxListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onCheckboxClick(((JCheckBoxMenuItem) e.getSource()).getActionCommand(),
						((JCheckBoxMenuItem) e.getSource()).isSelected());
			}
		}

	}

	protected class TurnFieldListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onActionFired(EVisualizerOptions.TURN_NEXT, true);
			}
		}

	}

	protected class ResetFieldListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onActionFired(EVisualizerOptions.RESET_FIELD, true);
			}
		}

	}
}
