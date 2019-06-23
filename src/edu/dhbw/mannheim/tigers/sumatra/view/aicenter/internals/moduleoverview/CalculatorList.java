/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * List for Calculators to select
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class CalculatorList extends JList<String>
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long						serialVersionUID	= -2303360994526135863L;
	
	private final List<ICalculatorObserver>	observers			= new LinkedList<ICalculatorObserver>();
	private final DefaultListModel<String>		model;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public CalculatorList()
	{
		super();
		addListSelectionListener(new CalculatorSelect());
		model = new DefaultListModel<String>();
		setModel(model);
		setVisible(true);
		setToolTipText("A selected calculator is active an unselected inactive. Use Crtl + Mouse to Select several Calculators");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void notifyObserver()
	{
		List<String> values = getSelectedValuesList();
		for (ICalculatorObserver o : observers)
		{
			o.selectedCalculatorsChanged(values);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(ICalculatorObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(ICalculatorObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * Adds a calculator to the model
	 * @param calculatorName
	 * @param selected
	 */
	public void addElement(String calculatorName, boolean selected)
	{
		model.addElement(calculatorName);
		
		// Select all elements at beginning, as the calculator is per default active
		int[] selectedIndices = new int[model.size()];
		int[] lastSelIndices = getSelectedIndices();
		for (int i = 0; i < lastSelIndices.length; i++)
		{
			selectedIndices[i] = lastSelIndices[i];
		}
		if (selected)
		{
			selectedIndices[lastSelIndices.length] = selectedIndices.length - 1;
		}
		setSelectedIndices(selectedIndices);
	}
	
	// --------------------------------------------------------------------------
	// --- Listener -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	public class CalculatorSelect implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			notifyObserver();
		}
	}
}
