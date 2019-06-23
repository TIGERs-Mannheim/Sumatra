/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Configure PID parameters and control dribbler.
 * 
 * @author AndreR
 * 
 */
public class DribblerConfigurationPanel extends JPanel
{
	/**
	 * Interface of MotorConfigurationPanel
	 * 
	 * @author AndreR
	 * 
	 */
	public interface IDribblerConfigurationPanelObserver
	{
		/**
		 * 
		 * @param logging
		 */
		void onSetDribblerLog(boolean logging);
		
		
		/**
		 * 
		 * @param kp
		 * @param ki
		 * @param kd
		 */
		void onSetDribblerPidParams(float kp, float ki, float kd);
		
		
		/**
		 * 
		 * @param rpm
		 */
		void onSetDribblerRPM(int rpm);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long											serialVersionUID	= -2637554368822866305L;
	private JTextField													latest				= null;
	private JTextField													eCurrent				= null;
	private JTextField													kp						= null;
	private JTextField													ki						= null;
	private JTextField													kd						= null;
	private JTextField													setpoint				= null;
	private JCheckBox														overload				= null;
	private JCheckBox														speedReached		= null;
	private JButton														logging				= null;
	private boolean														logOn					= false;
	
	private final List<IDribblerConfigurationPanelObserver>	observers			= new ArrayList<IDribblerConfigurationPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DribblerConfigurationPanel()
	{
		setLayout(new MigLayout("fill, wrap 1", "", ""));
		
		latest = new JTextField();
		eCurrent = new JTextField();
		logging = new JButton("Enable Log");
		kp = new JTextField();
		ki = new JTextField();
		kd = new JTextField();
		setpoint = new JTextField();
		overload = new JCheckBox();
		speedReached = new JCheckBox();
		
		final JButton setPidParams = new JButton("Save");
		final JButton setPid = new JButton("Set");
		
		setPidParams.addActionListener(new SetPidParams());
		setPid.addActionListener(new SetRPM());
		logging.addActionListener(new ToggleLog());
		
		latest.setEditable(false);
		overload.setEnabled(false);
		speedReached.setEnabled(false);
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
		infoPanel.add(new JLabel("Latest:"));
		infoPanel.add(latest);
		infoPanel.add(new JLabel("I:"));
		infoPanel.add(eCurrent);
		infoPanel.add(new JLabel("Overload:"));
		infoPanel.add(overload);
		infoPanel.add(new JLabel("Speed reached:"));
		infoPanel.add(speedReached);
		infoPanel.add(logging, "span 2, growx");
		
		final JPanel pidParamsPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		pidParamsPanel.setBorder(BorderFactory.createTitledBorder("PID Parameters"));
		pidParamsPanel.add(new JLabel("Kp:"));
		pidParamsPanel.add(kp);
		pidParamsPanel.add(new JLabel("Ki:"));
		pidParamsPanel.add(ki);
		pidParamsPanel.add(new JLabel("Kd:"));
		pidParamsPanel.add(kd);
		pidParamsPanel.add(setPidParams, "span 2");
		
		final JPanel pidPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		pidPanel.setBorder(BorderFactory.createTitledBorder("Control"));
		pidPanel.add(new JLabel("RPM:"));
		pidPanel.add(setpoint);
		pidPanel.add(setPid, "span 2");
		
		add(infoPanel);
		add(pidParamsPanel);
		add(pidPanel);
		add(Box.createVerticalGlue(), "grow, pushy");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IDribblerConfigurationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IDribblerConfigurationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifySetDribblerLog(boolean logging)
	{
		synchronized (observers)
		{
			for (final IDribblerConfigurationPanelObserver observer : observers)
			{
				observer.onSetDribblerLog(logging);
			}
		}
	}
	
	
	private void notifySetDribblerPidParams(float kp, float ki, float kd)
	{
		synchronized (observers)
		{
			for (final IDribblerConfigurationPanelObserver observer : observers)
			{
				observer.onSetDribblerPidParams(kp, ki, kd);
			}
		}
	}
	
	
	private void notifySetDribblerRPM(int rpm)
	{
		synchronized (observers)
		{
			for (final IDribblerConfigurationPanelObserver observer : observers)
			{
				observer.onSetDribblerRPM(rpm);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param p
	 * @param i
	 * @param d
	 */
	public void setPidParams(final float p, final float i, final float d)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				kp.setText(String.valueOf(p));
				ki.setText(String.valueOf(i));
				kd.setText(String.valueOf(d));
			}
		});
	}
	
	
	/**
	 * @param enLog
	 */
	public void setLogging(final boolean enLog)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				logOn = enLog;
				
				if (logOn)
				{
					logging.setText("Disable log");
				} else
				{
					logging.setText("Enable log");
				}
			}
		});
	}
	
	
	/**
	 * @param l
	 */
	public void setLatest(final int l)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				latest.setText(String.valueOf(l));
			}
		});
	}
	
	
	/**
	 * @param ol
	 */
	public void setOverload(final boolean ol)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				overload.setSelected(ol);
			}
		});
	}
	
	
	/**
	 * @param sr
	 */
	public void setSpeedReached(final boolean sr)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				speedReached.setSelected(sr);
			}
		});
	}
	
	
	/**
	 * @param cur
	 */
	public void setECurrent(final float cur)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				eCurrent.setText(String.format(Locale.ENGLISH, "%1.3f", cur));
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class ToggleLog implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (logOn)
			{
				logOn = false;
				logging.setText("Enable log");
			} else
			{
				logOn = true;
				logging.setText("Disable log");
			}
			
			notifySetDribblerLog(logOn);
		}
	}
	
	private class SetPidParams implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float p;
			float i;
			float d;
			
			try
			{
				p = Float.parseFloat(kp.getText());
				i = Float.parseFloat(ki.getText());
				d = Float.parseFloat(kd.getText());
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySetDribblerPidParams(p, i, d);
		}
	}
	
	private class SetRPM implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int sp;
			
			try
			{
				sp = Integer.parseInt(setpoint.getText());
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySetDribblerRPM(sp);
		}
	}
}
