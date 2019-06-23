/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log.internals;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Filter panel, select a user filter here.
 * Multiple words may be separated by a comma.
 * 
 * @author AndreR
 * 
 */
public class FilterPanel extends JPanel
{
	private static final long	serialVersionUID	= 1L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private ArrayList<IFilterPanelObserver> observers = new ArrayList<IFilterPanelObserver>();
	
	private JTextField	text	= null;
	private JButton		reset	= null;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public FilterPanel()
	{
		setLayout(new MigLayout("fill", "[][][]", ""));
		
		text = new JTextField();
		text.addActionListener(new TextChange());
		
		reset = new JButton("Reset");
		reset.addActionListener(new Reset());
		
		add(new JLabel("Filter: "));
		add(text, "growx, push, gapright 50");
		add(reset);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IFilterPanelObserver o)
	{
		observers.add(o);
	}
	
	public void removeObserver(IFilterPanelObserver o)
	{
		observers.remove(o);
	}

	// --------------------------------------------------------------------------
	// --- action listener ------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Reset implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			text.setText("");
			
			for(IFilterPanelObserver o : observers)
			{
				o.onNewFilter(new ArrayList<String>());
			}
		}
	}
	
	protected class TextChange implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ArrayList<String> allowed = new ArrayList<String>(Arrays.asList(text.getText().split(",")));

			for(IFilterPanelObserver o : observers)
			{
				o.onNewFilter(allowed);
			}
		}
	}
}
