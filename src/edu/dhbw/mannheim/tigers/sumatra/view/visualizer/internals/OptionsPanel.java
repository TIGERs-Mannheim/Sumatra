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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
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
	private JTabbedPane								tabs					= new JTabbedPane(JTabbedPane.RIGHT);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public OptionsPanel()
	{
		// --- configure panel ---
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// tabs.setMinimumSize(new Dimension(0, 200));
		
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();
		
		// collect all checkboxes
		checkBoxes = new HashMap<String, JCheckBox>();
		
		JButton btnTurn = new JButton("Turn field");
		btnTurn.addActionListener(new TurnFieldListener());
		JButton btnReset = new JButton("Reset field");
		btnReset.addActionListener(new ResetFieldListener());
		JPanel pActions = createOptionsPanel("actions");
		pActions.add(btnTurn);
		pActions.add(btnReset);
		
		// --- add checkboxes ---
		JPanel pLayers = createOptionsPanel("layers");
		createCheckBox(EVisualizerOptions.LAYER_DEBUG_INFOS, pLayers);
		createCheckBox(EVisualizerOptions.FANCY, pLayers);
		createCheckBox(EVisualizerOptions.COORDINATES, pLayers);
		createCheckBox(EVisualizerOptions.FIELD_MARKS, pLayers);
		createCheckBox(EVisualizerOptions.BALL_BUFFER, pLayers);
		createCheckBox(EVisualizerOptions.REFEREE, pLayers);
		createCheckBox(EVisualizerOptions.FIELD_PREDICTION, pLayers);
		createCheckBox(EVisualizerOptions.POSITION_GRID, pLayers);
		createCheckBox(EVisualizerOptions.MISC, pLayers);
		
		JPanel pPathPlanning = createOptionsPanel("path plan.");
		createCheckBox(EVisualizerOptions.VELOCITY, pPathPlanning);
		createCheckBox(EVisualizerOptions.ACCELERATION, pPathPlanning);
		createCheckBox(EVisualizerOptions.PATHS, pPathPlanning);
		createCheckBox(EVisualizerOptions.SPLINES, pPathPlanning);
		createCheckBox(EVisualizerOptions.ERROR_TREE, pPathPlanning);
		createCheckBox(EVisualizerOptions.POT_PATHS, pPathPlanning);
		createCheckBox(EVisualizerOptions.POT_SPLINES, pPathPlanning);
		
		JPanel pAI = createOptionsPanel("AI");
		createCheckBox(EVisualizerOptions.YELLOW_AI, pAI);
		createCheckBox(EVisualizerOptions.BLUE_AI, pAI);
		createCheckBox(EVisualizerOptions.SHAPES, pAI);
		createCheckBox(EVisualizerOptions.PATTERNS, pAI);
		createCheckBox(EVisualizerOptions.GOAL_POINTS, pAI);
		createCheckBox(EVisualizerOptions.ROLE_NAME, pAI);
		createCheckBox(EVisualizerOptions.TACTICS, pAI);
		createCheckBox(EVisualizerOptions.ANALYSIS_GRID, pAI);
		createCheckBox(EVisualizerOptions.SUPPORT_POS, pAI);
		
		JPanel pShortcuts = createOptionsPanel("shortcuts");
		pShortcuts.add(new JLabel("ctrl:"), "wrap");
		pShortcuts.add(new JLabel("  Look at Ball"), "wrap");
		pShortcuts.add(new JLabel("shift:"), "wrap");
		pShortcuts.add(new JLabel("  Kick to"), "wrap");
		pShortcuts.add(new JLabel("rightClick:"), "wrap");
		pShortcuts.add(new JLabel("  Place ball"), "wrap");
		
		this.add(tabs);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private JPanel createOptionsPanel(final String name)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(name));
		panel.add(Box.createHorizontalGlue());
		tabs.add(name, panel);
		return panel;
	}
	
	
	private JCheckBox createCheckBox(final EVisualizerOptions option, final JPanel parent)
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
	public void setButtonsEnabled(final boolean enable)
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
		public void actionPerformed(final ActionEvent e)
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
