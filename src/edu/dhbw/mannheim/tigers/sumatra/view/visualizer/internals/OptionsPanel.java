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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;


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
	private static final long				serialVersionUID		= 6408342941543334436L;
	private List<IOptionsPanelObserver>	observers				= new CopyOnWriteArrayList<IOptionsPanelObserver>();
	private CheckboxListener				checkboxListener		= null;
	
	// --- checkboxes ---
	private JCheckBox							cbVelocity				= null;
	private JCheckBox							cbAcceleration			= null;
	private JCheckBox							cbPositioningGrid		= null;
	private JCheckBox							cbAnalysingGrid		= null;
	private JCheckBox							cbPaths					= null;
	private JCheckBox							cbSplines				= null;
	private JCheckBox							cbDebugPoints			= null;
	private JCheckBox							cbDefenseGoalPoints	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public OptionsPanel()
	{
		// --- configure panel ---
		setLayout(new MigLayout("", "", "[top]"));
		
		// --- border ---
		TitledBorder border = BorderFactory.createTitledBorder("options");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);
		
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();
		
		// --- add checkboxes ---
		add(new JLabel("-------- WP --------"), "center, wrap");
		
		cbVelocity = new JCheckBox("velocity");
		cbVelocity.addActionListener(checkboxListener);
		cbVelocity.setActionCommand("velocity");
		add(cbVelocity, "wrap");
		
		cbAcceleration = new JCheckBox("acceleration");
		cbAcceleration.addActionListener(checkboxListener);
		cbAcceleration.setActionCommand("acceleration");
		add(cbAcceleration, "wrap");
		
		add(new JLabel("-------- AI --------"), "center, wrap");
		
		cbPositioningGrid = new JCheckBox("position grid");
		cbPositioningGrid.addActionListener(checkboxListener);
		cbPositioningGrid.setActionCommand("posGrid");
		add(cbPositioningGrid, "wrap");
		
		cbAnalysingGrid = new JCheckBox("analyse grid");
		cbAnalysingGrid.addActionListener(checkboxListener);
		cbAnalysingGrid.setActionCommand("analyseGrid");
		add(cbAnalysingGrid, "wrap");
		
		cbPaths = new JCheckBox("paths");
		cbPaths.addActionListener(checkboxListener);
		cbPaths.setActionCommand("paths");
		add(cbPaths, "wrap");
		
		cbSplines = new JCheckBox("splines");
		cbSplines.addActionListener(checkboxListener);
		cbSplines.setActionCommand("splines");
		add(cbSplines, "wrap");
		
		cbDebugPoints = new JCheckBox("debug points");
		cbDebugPoints.addActionListener(checkboxListener);
		cbDebugPoints.setActionCommand("debugPoints");
		add(cbDebugPoints, "wrap");
		
		cbDefenseGoalPoints = new JCheckBox("defense goal points");
		cbDefenseGoalPoints.addActionListener(checkboxListener);
		cbDefenseGoalPoints.setActionCommand("DefenseGoalPoints");
		add(cbDefenseGoalPoints, "wrap");
		

	}
	

	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public void addObserver(IOptionsPanelObserver o)
	{
		observers.add(o);
	}
	

	public void removeObserver(IOptionsPanelObserver o)
	{
		observers.remove(o);
	}
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	
	protected class CheckboxListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			;
			for (IOptionsPanelObserver o : observers)
			{
				o.onCheckboxClick(((JCheckBox) e.getSource()).getActionCommand(), ((JCheckBox) e.getSource()).isSelected());
			}
		}
		
	}
	

}
