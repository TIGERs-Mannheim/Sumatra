/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

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
 * Configure PID parameters and control motor.
 * 
 * @author AndreR
 * 
 */
public class MotorConfigurationPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long										serialVersionUID	= -2637554368822866305L;
	private JTextField												latest				= null;
	private JTextField												eCurrent				= null;
	private JTextField												kp						= null;
	private JTextField												ki						= null;
	private JTextField												kd						= null;
	private JTextField												slewMax				= null;
	private JTextField												power					= null;
	private JTextField												setpoint				= null;
	private JCheckBox													overload				= null;
	private JButton													logging				= null;
	private boolean													logOn					= false;
	
	private final List<IMotorConfigurationPanelObserver>	observers			= new ArrayList<IMotorConfigurationPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public MotorConfigurationPanel()
	{
		setLayout(new MigLayout("fill, wrap 1", "", ""));
		
		latest = new JTextField();
		eCurrent = new JTextField();
		logging = new JButton("Enable Log");
		kp = new JTextField();
		ki = new JTextField();
		kd = new JTextField();
		slewMax = new JTextField();
		power = new JTextField();
		setpoint = new JTextField();
		overload = new JCheckBox();
		
		final JButton setPidParams = new JButton("Save");
		final JButton setManual = new JButton("Set");
		final JButton setPid = new JButton("Set");
		
		setPidParams.addActionListener(new SetPidParams());
		setManual.addActionListener(new SetManual());
		setPid.addActionListener(new SetPidSetpoint());
		logging.addActionListener(new ToggleLog());
		
		latest.setEditable(false);
		overload.setEnabled(false);
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
		infoPanel.add(new JLabel("Latest:"));
		infoPanel.add(latest);
		infoPanel.add(new JLabel("I:"));
		infoPanel.add(eCurrent);
		infoPanel.add(new JLabel("Overload:"));
		infoPanel.add(overload);
		infoPanel.add(logging, "span 2, growx");
		
		final JPanel pidParamsPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		pidParamsPanel.setBorder(BorderFactory.createTitledBorder("PID Parameters"));
		pidParamsPanel.add(new JLabel("Kp:"));
		pidParamsPanel.add(kp);
		pidParamsPanel.add(new JLabel("Ki:"));
		pidParamsPanel.add(ki);
		pidParamsPanel.add(new JLabel("Kd:"));
		pidParamsPanel.add(kd);
		pidParamsPanel.add(new JLabel("Slew max:"));
		pidParamsPanel.add(slewMax);
		pidParamsPanel.add(setPidParams, "span 2");
		
		final JPanel manualPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		manualPanel.setBorder(BorderFactory.createTitledBorder("Manual Control"));
		manualPanel.add(new JLabel("Power:"));
		manualPanel.add(power);
		manualPanel.add(setManual, "span 2");
		
		final JPanel pidPanel = new JPanel(new MigLayout("fill, wrap 2", "[80]10[100,fill]"));
		pidPanel.setBorder(BorderFactory.createTitledBorder("PID Control"));
		pidPanel.add(new JLabel("Setpoint:"));
		pidPanel.add(setpoint);
		pidPanel.add(setPid, "span 2");
		
		add(infoPanel);
		add(pidParamsPanel);
		add(manualPanel);
		add(pidPanel);
		add(Box.createVerticalGlue(), "grow, pushy");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IMotorConfigurationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IMotorConfigurationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifySetLog(boolean logging)
	{
		synchronized (observers)
		{
			for (final IMotorConfigurationPanelObserver observer : observers)
			{
				observer.onSetLog(logging);
			}
		}
	}
	
	
	private void notifySetPidParams(float kp, float ki, float kd, int slew)
	{
		synchronized (observers)
		{
			for (final IMotorConfigurationPanelObserver observer : observers)
			{
				observer.onSetPidParams(kp, ki, kd, slew);
			}
		}
	}
	
	
	private void notifySetManual(int power)
	{
		synchronized (observers)
		{
			for (final IMotorConfigurationPanelObserver observer : observers)
			{
				observer.onSetManual(power);
			}
		}
	}
	
	
	private void notifySetPidSetpoint(int setpoint)
	{
		synchronized (observers)
		{
			for (final IMotorConfigurationPanelObserver observer : observers)
			{
				observer.onSetPidSetpoint(setpoint);
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
	 * @param s
	 */
	public void setPidParams(final float p, final float i, final float d, final int s)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				kp.setText(String.valueOf(p));
				ki.setText(String.valueOf(i));
				kd.setText(String.valueOf(d));
				slewMax.setText(String.valueOf(s));
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
			
			notifySetLog(logOn);
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
			int s;
			
			try
			{
				p = Float.parseFloat(kp.getText());
				i = Float.parseFloat(ki.getText());
				d = Float.parseFloat(kd.getText());
				s = Integer.parseInt(slewMax.getText());
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySetPidParams(p, i, d, s);
		}
	}
	
	private class SetManual implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int pow;
			
			try
			{
				pow = Integer.parseInt(power.getText());
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySetManual(pow);
		}
	}
	
	private class SetPidSetpoint implements ActionListener
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
			
			notifySetPidSetpoint(sp);
		}
	}
}
