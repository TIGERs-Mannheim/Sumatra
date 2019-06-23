/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals;

import java.awt.Color;
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
public class ABCPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long					serialVersionUID	= 6408342941543334436L;
	private final List<IABCPanelObserver>	observers			= new CopyOnWriteArrayList<IABCPanelObserver>();
	private CheckboxListener					checkboxListener	= null;
	
	// --- checkboxes ---
	private JCheckBox								cbVelocity			= null;
	private JCheckBox								cbAcceleration		= null;
	private JCheckBox								cbGrid				= null;
	private JCheckBox								cbPaths				= null;
	private JCheckBox								cbDebugPoints		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public ABCPanel()
	{
		// --- configure panel ---
		super();
		setBackground(Color.yellow);
		setLayout(new MigLayout("", "", "[top]"));
		
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("options");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);
		
		// --- checkbox-listener ---
		checkboxListener = new CheckboxListener();
		
		// --- add checkboxes ---
		add(new JLabel("-------- WP --------"), "center, wrap");
		
		cbVelocity = new JCheckBox("velocity");
		cbVelocity.setBackground(Color.orange);
		cbVelocity.addActionListener(checkboxListener);
		cbVelocity.setActionCommand("velocity");
		add(cbVelocity, "wrap");
		
		cbAcceleration = new JCheckBox("acceleration");
		cbAcceleration.setBackground(Color.orange);
		cbAcceleration.addActionListener(checkboxListener);
		cbAcceleration.setActionCommand("acceleration");
		add(cbAcceleration, "wrap");
		
		add(new JLabel("-------- AI --------"), "center, wrap");
		
		cbGrid = new JCheckBox("grid");
		cbGrid.setBackground(new Color(220, 90, 90));
		cbGrid.addActionListener(checkboxListener);
		cbGrid.setActionCommand("grid");
		add(cbGrid, "wrap");
		
		cbPaths = new JCheckBox("paths");
		cbPaths.setBackground(Color.orange);
		cbPaths.addActionListener(checkboxListener);
		cbPaths.setActionCommand("paths");
		add(cbPaths, "wrap");
		
		cbDebugPoints = new JCheckBox("DEBUG points");
		cbDebugPoints.setBackground(Color.orange);
		cbDebugPoints.addActionListener(checkboxListener);
		cbDebugPoints.setActionCommand("debugPoints");
		add(cbDebugPoints, "wrap");
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * 
	 * @param o
	 */
	public void addObserver(IABCPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(IABCPanelObserver o)
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
			for (final IABCPanelObserver o : observers)
			{
				o.onCheckboxClick(((JCheckBox) e.getSource()).getActionCommand(), ((JCheckBox) e.getSource()).isSelected());
			}
		}
		
	}
	
	
}
