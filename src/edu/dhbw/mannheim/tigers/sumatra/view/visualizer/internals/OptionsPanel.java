/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * 
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 * 
 */
public class OptionsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID	= 6408342941543334436L;
	
	private final List<IOptionsPanelObserver>	observers			= new CopyOnWriteArrayList<IOptionsPanelObserver>();
	private Map<String, JCheckBox>				checkBoxes			= null;
	private CheckboxListener						checkboxListener;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public OptionsPanel()
	{
		// --- configure panel ---
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();
		
		// collect all checkboxes
		checkBoxes = new HashMap<String, JCheckBox>();
		
		JButton btnTurn = new JButton("Turn field");
		btnTurn.addActionListener(new TurnFieldListener());
		JPanel pActions = createOptionsPanel("actions");
		pActions.add(btnTurn);
		
		// --- add checkboxes ---
		JPanel pLayers = createOptionsPanel("layers");
		// createCheckBox(EVisualizerOptions.LAYER_DEBUG_INFOS, pLayers);
		createCheckBox(EVisualizerOptions.FANCY, pLayers);
		createCheckBox(EVisualizerOptions.COORDINATES, pLayers);
		createCheckBox(EVisualizerOptions.FIELD_MARKS, pLayers);
		createCheckBox(EVisualizerOptions.BALL_BUFFER, pLayers);
		createCheckBox(EVisualizerOptions.SHAPES, pLayers);
		
		JPanel pGrid = createOptionsPanel("grid");
		createCheckBox(EVisualizerOptions.POSITION_GRID, pGrid);
		createCheckBox(EVisualizerOptions.ANALYSIS_GRID, pGrid);
		
		JPanel pPathPlanning = createOptionsPanel("path planning");
		createCheckBox(EVisualizerOptions.VELOCITY, pPathPlanning);
		createCheckBox(EVisualizerOptions.ACCELERATION, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATHS, pPathPlanning);
		createCheckBox(EVisualizerOptions.SPLINES, pPathPlanning);
		createCheckBox(EVisualizerOptions.ERROR_TREE, pPathPlanning);
		
		JPanel pAI = createOptionsPanel("AI");
		createCheckBox(EVisualizerOptions.PATTERNS, pAI);
		createCheckBox(EVisualizerOptions.DEFENSE_GOAL_POINTS, pAI);
		createCheckBox(EVisualizerOptions.OFFENSIVE_POINTS, pAI);
		createCheckBox(EVisualizerOptions.ROLE_NAME, pAI);
		
		JPanel pShortcuts = createOptionsPanel("shortcuts");
		JLabel lblShortcutCtrl = new JLabel("ctrl: Look at Ball");
		JLabel lblShortcutAlt = new JLabel("shift: Kick to");
		pShortcuts.add(lblShortcutCtrl, "wrap");
		pShortcuts.add(lblShortcutAlt, "wrap");
		
		// fill the rest of the space
		this.add(Box.createVerticalGlue());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private JPanel createOptionsPanel(String name)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(name));
		panel.add(Box.createHorizontalGlue());
		add(panel);
		return panel;
	}
	
	
	private JCheckBox createCheckBox(EVisualizerOptions option, JPanel parent)
	{
		JCheckBox checkbox = new JCheckBox(option.getName());
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
		for (Entry<String, JCheckBox> cb : checkBoxes.entrySet())
		{
			String value = SumatraModel.getInstance().getUserProperty(
					OptionsPanel.class.getCanonicalName() + "." + cb.getKey());
			Boolean selected = Boolean.valueOf(value);
			cb.getValue().setSelected(selected);
		}
		setButtonsEnabled(false);
	}
	
	
	/**
	 * @param enable
	 */
	public void setButtonsEnabled(boolean enable)
	{
		for (JCheckBox cb : checkBoxes.values())
		{
			cb.setEnabled(enable);
		}
	}
	
	
	/**
	 * Unselects all checkboxes on this panel.
	 */
	private void uncheckAllButtons()
	{
		for (JCheckBox cb : checkBoxes.values())
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
	public void addObserver(IOptionsPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IOptionsPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @return the checkBoxes
	 */
	public final Map<String, JCheckBox> getCheckBoxes()
	{
		return checkBoxes;
	}
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	
	protected class CheckboxListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onCheckboxClick(((JCheckBox) e.getSource()).getActionCommand(), ((JCheckBox) e.getSource()).isSelected());
			}
		}
		
	}
	
	protected class TurnFieldListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (final IOptionsPanelObserver o : observers)
			{
				o.onTurnField();
			}
		}
		
	}
	
}
