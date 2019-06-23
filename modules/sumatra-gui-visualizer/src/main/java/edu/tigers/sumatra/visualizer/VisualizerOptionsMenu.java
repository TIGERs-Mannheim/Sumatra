/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 */
public class VisualizerOptionsMenu extends JMenuBar
{
	static final String SOURCE_PREFIX = "SOURCE_";
	private static final long serialVersionUID = 6408342941543334436L;
	private final List<IOptionsPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final Map<String, JMenu> parentMenus = new HashMap<>();
	private final List<JCheckBoxMenuItem> checkBoxes = new ArrayList<>();
	private final Set<IShapeLayer> knownShapeLayers = new HashSet<>();
	private final CheckboxListener checkboxListener;
	
	private final JMenu pSources;
	
	
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
		
		JMenuItem btnTurn = new JMenuItem("Turn");
		btnTurn.addActionListener(new TurnFieldListener());
		JMenuItem btnReset = new JMenuItem("Reset");
		btnReset.addActionListener(new ResetFieldListener());
		pActions.add(btnTurn);
		pActions.add(btnReset);
		
		addMenuEntry(EVisualizerOptions.FANCY);
		
		JMenu pShortcuts = new JMenu("Shortcuts");
		add(pShortcuts);
		pShortcuts.add(new JMenuItem("Left mouse click:"));
		pShortcuts.add(new JMenuItem("  ctrl: Look at Ball"));
		pShortcuts.add(new JMenuItem("  shift: Kick to"));
		pShortcuts.add(new JMenuItem("  ctrl+shift: Follow mouse"));
		
		pShortcuts.add(new JMenuItem("Right mouse click:"));
		pShortcuts.add(new JMenuItem("  none: place ball"));
		pShortcuts.add(new JMenuItem("  ctrl: 8m/s to target"));
		pShortcuts.add(new JMenuItem("  shift: stop at target"));
		pShortcuts.add(new JMenuItem("  ctrl+shift: 2m/s at target"));
		pShortcuts.add(new JMenuItem("  above combinations + alt: chip ball"));
		
		pSources = new JMenu("Sources");
		add(pSources);
	}
	
	
	/**
	 * Update with latest set of layers. Existing layers will not be removed
	 * 
	 * @param source
	 */
	public synchronized void addSourceMenuIfNotPresent(String source)
	{
		if (!containsSource(source))
		{
			checkBoxes.add(createCheckBox(source, SOURCE_PREFIX + source, true, pSources));
		}
	}
	
	
	private boolean containsSource(String source)
	{
		for (int i = 0; i < pSources.getItemCount(); i++)
		{
			JMenuItem item = pSources.getItem(i);
			if (item.getActionCommand().equals(SOURCE_PREFIX + source))
			{
				return true;
			}
		}
		return false;
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
			add(parent);
			parentMenus.put(shapeLayer.getCategory(), parent);
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
		parent.add(checkbox);
		
		String value = SumatraModel.getInstance().getUserProperty(
				OptionsPanelPresenter.class.getCanonicalName() + "." + checkbox.getActionCommand());
		if (value == null)
		{
			checkbox.setSelected(visible);
		} else
		{
			Boolean selected = Boolean.valueOf(value);
			checkbox.setSelected(selected);
		}
		for (final IOptionsPanelObserver o : observers)
		{
			o.onCheckboxClick(checkbox.getActionCommand(), checkbox.isSelected());
		}
		
		return checkbox;
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
