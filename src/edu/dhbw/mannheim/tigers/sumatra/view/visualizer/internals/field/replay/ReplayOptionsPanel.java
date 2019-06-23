/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IReplayOptionsPanelObserver;


/**
 * This panel contains primary the record button for capturing
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ReplayOptionsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long								serialVersionUID	= 1L;
	private final List<IReplayOptionsPanelObserver>	observers			= new CopyOnWriteArrayList<IReplayOptionsPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ReplayOptionsPanel()
	{
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("replay");
		setBorder(border);
		
		setLayout(new MigLayout());
		
		JToggleButton btnRec = new JToggleButton("Record");
		JToggleButton btnRecSave = new JToggleButton("Record&Save");
		
		btnRec.addActionListener(new RecordButtonListener());
		btnRecSave.addActionListener(new RecordSaveButtonListener());
		
		add(btnRec, "wrap");
		add(btnRecSave);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(IReplayOptionsPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IReplayOptionsPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	private class RecordButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (IReplayOptionsPanelObserver observer : observers)
			{
				if (e.getSource() instanceof JToggleButton)
				{
					JToggleButton btn = (JToggleButton) e.getSource();
					observer.onRecord(btn.isSelected());
					if (btn.isSelected())
					{
						btn.setText("Recording...");
					} else
					{
						btn.setText("Record");
					}
				}
			}
		}
		
	}
	
	private class RecordSaveButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (IReplayOptionsPanelObserver observer : observers)
			{
				if (e.getSource() instanceof JToggleButton)
				{
					JToggleButton btn = (JToggleButton) e.getSource();
					observer.onSave(btn.isSelected());
					if (btn.isSelected())
					{
						btn.setText("Recording...");
					} else
					{
						btn.setText("Record&Save");
					}
					
				}
			}
		}
	}
	
}
