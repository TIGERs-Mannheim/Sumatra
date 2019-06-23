/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.11.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;


/**
 * Kicker autoload panel.
 * 
 * @author AndreR
 * 
 */
public class KickerAutoloadPanel extends JPanel
{
	public interface IKickerAutoloadPanelObserver
	{
		public void onKickerChargeAuto(int max);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long									serialVersionUID	= 1487814019231988244L;
	
	private JTextField											maxCap;
	
	private final List<IKickerAutoloadPanelObserver>	observers			= new ArrayList<IKickerAutoloadPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickerAutoloadPanel()
	{
		setLayout(new MigLayout("fill, wrap 2", "[80]10[50,fill]"));
		
		maxCap = new JTextField("150");
		
		JButton save = new JButton("Set");
		save.addActionListener(new Set());
		
		add(new JLabel("Max Cap Level:"));
		add(maxCap, "grow");
		add(save, "span 2, grow");
		
		setBorder(BorderFactory.createTitledBorder("Auto Charge"));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IKickerAutoloadPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IKickerAutoloadPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	private void notifyKickerSetAutoload(int max)
	{
		synchronized (observers)
		{
			for (IKickerAutoloadPanelObserver observer : observers)
			{
				observer.onKickerChargeAuto(max);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class Set implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			int max = 0;
			
			try
			{
				max = Integer.parseInt(maxCap.getText());
			}

			catch (NumberFormatException e)
			{
				return;
			}
			
			notifyKickerSetAutoload(max);
		}
	}
}
