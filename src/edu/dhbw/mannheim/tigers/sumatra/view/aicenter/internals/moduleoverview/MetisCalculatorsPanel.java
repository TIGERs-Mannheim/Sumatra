/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * Panel for selecting metis calculators
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MetisCalculatorsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**  */
	private static final long	serialVersionUID	= 9125108113440167202L;
	private CalculatorList		calculatorList		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public MetisCalculatorsPanel()
	{
		setBorder(BorderFactory.createTitledBorder("Metis calculators"));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		calculatorList = new CalculatorList();
		add(new JScrollPane(calculatorList));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public CalculatorList getCalculatorList()
	{
		return calculatorList;
	}
}
