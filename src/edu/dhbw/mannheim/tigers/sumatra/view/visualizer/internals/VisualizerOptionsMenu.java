/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.OptionsPanelPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.IVisualizerOption;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 */
public class VisualizerOptionsMenu extends JMenuBar
{
	private static final long						serialVersionUID	= 6408342941543334436L;
	
	private final List<IOptionsPanelObserver>	observers			= new CopyOnWriteArrayList<IOptionsPanelObserver>();
	private Map<String, JCheckBoxMenuItem>		checkBoxes			= new HashMap<String, JCheckBoxMenuItem>();
	private CheckboxListener						checkboxListener;
	
	
	/**
	 */
	public VisualizerOptionsMenu()
	{
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();
		
		JMenu pActions = new JMenu("Field");
		add(pActions);
		JMenuItem btnTurn = new JMenuItem("Turn");
		btnTurn.addActionListener(new TurnFieldListener());
		JMenuItem btnReset = new JMenuItem("Reset");
		btnReset.addActionListener(new ResetFieldListener());
		pActions.add(btnTurn);
		pActions.add(btnReset);
		
		// --- add checkboxes ---
		JMenu pLayers = new JMenu("Layer");
		add(pLayers);
		createCheckBox(EVisualizerOptions.LAYER_DEBUG_INFOS, pLayers);
		createCheckBox(EVisualizerOptions.FANCY, pLayers);
		createCheckBox(EVisualizerOptions.COORDINATES, pLayers);
		createCheckBox(EVisualizerOptions.FIELD_MARKS, pLayers);
		createCheckBox(EVisualizerOptions.POSITION_BUFFER, pLayers);
		createCheckBox(EVisualizerOptions.REFEREE, pLayers);
		createCheckBox(EVisualizerOptions.FIELD_PREDICTION, pLayers);
		createCheckBox(EVisualizerOptions.ROLE_NAME, pLayers);
		createCheckBox(EVisualizerOptions.BOT_STATUS, pLayers);
		createCheckBox(EVisualizerOptions.VISION, pLayers);
		createCheckBox(EVisualizerOptions.SUPPORT_POS, pLayers);
		createCheckBox(EVisualizerOptions.SUPPORT_GRID, pLayers);
		createCheckBox(EVisualizerOptions.INTERSECTION, pLayers);
		
		JMenu pPathPlanning = new JMenu("Pathplanning");
		add(pPathPlanning);
		createCheckBox(EVisualizerOptions.VELOCITY, pPathPlanning);
		createCheckBox(EVisualizerOptions.ACCELERATION, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATHS, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATH_DECORATION, pPathPlanning);
		createCheckBox(EVisualizerOptions.PP_DEBUG, pPathPlanning);
		createCheckBox(EVisualizerOptions.POT_PATHS, pPathPlanning);
		createCheckBox(EVisualizerOptions.POT_PATH_DECORATION, pPathPlanning);
		createCheckBox(EVisualizerOptions.POT_DEBUG, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATHS_UNSMOOTHED, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATHS_RAMBO, pPathPlanning);
		
		JMenu pAI = new JMenu("AI");
		add(pAI);
		createCheckBox(EVisualizerOptions.YELLOW_AI, pAI);
		createCheckBox(EVisualizerOptions.BLUE_AI, pAI);
		
		
		JMenu pShapes = new JMenu("Shapes");
		add(pShapes);
		for (EDrawableShapesLayer layer : EDrawableShapesLayer.values())
		{
			createCheckBox(layer, pShapes);
		}
		
		JMenu pShortcuts = new JMenu("Shortcuts");
		add(pShortcuts);
		pShortcuts.add(new JMenuItem("ctrl: Look at Ball"));
		pShortcuts.add(new JMenuItem("shift: Kick to"));
		pShortcuts.add(new JMenuItem("rightClick: Place ball"));
		pShortcuts.add(new JMenuItem("ctrl+shift: Follow mouse"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private JCheckBoxMenuItem createCheckBox(final IVisualizerOption option, final JMenu parent)
	{
		JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(option.getName());
		checkbox.addActionListener(checkboxListener);
		checkbox.setActionCommand(option.name());
		checkbox.setEnabled(false);
		parent.add(checkbox);
		checkBoxes.put(checkbox.getActionCommand(), checkbox);
		return checkbox;
	}
	
	
	/**
	 * initialize button states
	 */
	public void setInitialButtonState()
	{
		uncheckAllButtons();
		for (Entry<String, JCheckBoxMenuItem> cb : checkBoxes.entrySet())
		{
			String value = SumatraModel.getInstance().getUserProperty(
					OptionsPanelPresenter.class.getCanonicalName() + "." + cb.getKey());
			Boolean selected = Boolean.valueOf(value);
			cb.getValue().setSelected(selected);
		}
		setButtonsEnabled(false);
	}
	
	
	/**
	 * @param enable
	 */
	public void setButtonsEnabled(final boolean enable)
	{
		for (JCheckBoxMenuItem cb : checkBoxes.values())
		{
			cb.setEnabled(enable);
		}
	}
	
	
	/**
	 * Unselects all checkboxes on this panel.
	 */
	private void uncheckAllButtons()
	{
		for (JCheckBoxMenuItem cb : checkBoxes.values())
		{
			cb.setSelected(false);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(final IOptionsPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IOptionsPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @return the checkBoxes
	 */
	public final Map<String, JCheckBoxMenuItem> getCheckBoxes()
	{
		return checkBoxes;
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
