/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.09.2010
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
 * Manual duty cycle control.
 * 
 * @author AndreR
 * 
 */
public class KickerChargeManualPanel extends JPanel
{
	/**
	 */
	public interface IKickerChargeManualObserver
	{
		/**
		 * @param duration
		 * @param on
		 * @param off
		 */
		void onKickerChargeManual(int duration, int on, int off);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= 4491788507190622525L;
	
	private final JTextField								time;
	private final JTextField								on;
	private final JTextField								off;
	
	private final List<IKickerChargeManualObserver>	observers			= new ArrayList<IKickerChargeManualObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KickerChargeManualPanel()
	{
		setLayout(new MigLayout("fill, wrap 3", "[50][100,fill][30]"));
		
		on = new JTextField();
		off = new JTextField();
		time = new JTextField();
		
		final JButton set = new JButton("Set");
		
		set.addActionListener(new Set());
		
		add(new JLabel("Charge time:"));
		add(time, "w 100");
		add(new JLabel("ms"));
		add(new JLabel("On:"));
		add(on);
		add(new JLabel("ticks"));
		add(new JLabel("Off:"));
		add(off);
		add(new JLabel("ticks"));
		add(set, "span");
		
		setBorder(BorderFactory.createTitledBorder("Manual Charge"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IKickerChargeManualObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IKickerChargeManualObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifySet(int duration, int on, int off)
	{
		synchronized (observers)
		{
			for (final IKickerChargeManualObserver observer : observers)
			{
				observer.onKickerChargeManual(duration, on, off);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class Set implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int tOn;
			int tOff;
			int t;
			
			try
			{
				t = Integer.parseInt(time.getText());
				tOn = Integer.parseInt(on.getText());
				tOff = Integer.parseInt(off.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifySet(t, tOn, tOff);
		}
	}
}
