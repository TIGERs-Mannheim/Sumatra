/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;


/**
 * Simple numeric XYW(V) input for movement
 * 
 * @author AndreR
 * 
 */
public class MotorInputPanel extends JPanel
{
	/** */
	public interface IMotorInputPanelObserver
	{
		/**
		 * 
		 * @param x
		 * @param y
		 * @param w
		 * @param v
		 */
		void onSetSpeed(float x, float y, float w, float v);
		
		
		/**
		 * 
		 * @param x
		 * @param y
		 * @param w
		 */
		void onSetSpeed(float x, float y, float w);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID	= -5046635212496152005L;
	private JTextField									xSpeed				= null;
	private JTextField									ySpeed				= null;
	private JTextField									wSpeed				= null;
	private JTextField									vSpeed				= null;
	
	private final List<IMotorInputPanelObserver>	observers			= new ArrayList<IMotorInputPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param useV use V component (compensated rotation)
	 */
	public MotorInputPanel(boolean useV)
	{
		setLayout(new MigLayout("wrap 2", "[100]10[100,fill]", ""));
		
		xSpeed = new JTextField();
		ySpeed = new JTextField();
		wSpeed = new JTextField();
		if (useV)
		{
			vSpeed = new JTextField();
		}
		
		final JButton setSpeed = new JButton("Set Speed");
		setSpeed.addActionListener(new SetSpeed());
		
		add(new JLabel("X Speed:"));
		add(xSpeed);
		add(new JLabel("Y Speed:"));
		add(ySpeed);
		add(new JLabel("W Speed:"));
		add(wSpeed);
		if (useV)
		{
			add(new JLabel("V Speed:"));
			add(vSpeed);
		}
		add(setSpeed, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IMotorInputPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(IMotorInputPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// -------------------------------------------------------------------------
	
	private class SetSpeed implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float x;
			float y;
			float w;
			float v = 0.0f;
			
			try
			{
				x = Float.valueOf(xSpeed.getText());
				y = Float.valueOf(ySpeed.getText());
				w = Float.valueOf(wSpeed.getText());
				if (vSpeed != null)
				{
					v = Float.valueOf(vSpeed.getText());
				}
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			if (vSpeed != null)
			{
				notifySetSpeed(x, y, w, v);
			} else
			{
				notifySetSpeed(x, y, w);
			}
		}
	}
	
	
	private void notifySetSpeed(float x, float y, float w)
	{
		synchronized (observers)
		{
			for (final IMotorInputPanelObserver observer : observers)
			{
				observer.onSetSpeed(x, y, w);
			}
		}
	}
	
	
	private void notifySetSpeed(float x, float y, float w, float v)
	{
		synchronized (observers)
		{
			for (final IMotorInputPanelObserver observer : observers)
			{
				observer.onSetSpeed(x, y, w, v);
			}
		}
	}
}
