/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter.bots;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;


/**
 * Simple numeric XYW(V) input for movement
 * 
 * @author AndreR
 */
public class MotorInputPanel extends JPanel
{
	/** Observer interface. */
	@FunctionalInterface
	public interface IMotorInputPanelObserver
	{
		/**
		 * @param x
		 * @param y
		 * @param w
		 */
		void onSetSpeed(double x, double y, double w);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID	= -5046635212496152005L;
	private JTextField									xSpeed				= null;
	private JTextField									ySpeed				= null;
	private JTextField									wSpeed				= null;
	
	private final List<IMotorInputPanelObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/** Constructor. */
	public MotorInputPanel()
	{
		setLayout(new MigLayout("wrap 2", "[100]10[100,fill]", ""));
		
		xSpeed = new JTextField("0");
		ySpeed = new JTextField("0");
		wSpeed = new JTextField("0");
		
		final JButton setSpeed = new JButton("Set Speed");
		setSpeed.addActionListener(new SetSpeed());
		
		add(new JLabel("X Speed:"));
		add(xSpeed);
		add(new JLabel("Y Speed:"));
		add(ySpeed);
		add(new JLabel("W Speed:"));
		add(wSpeed);
		add(setSpeed, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IMotorInputPanelObserver observer)
	{
			observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IMotorInputPanelObserver observer)
	{
			observers.remove(observer);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// -------------------------------------------------------------------------
	
	private class SetSpeed implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			double x;
			double y;
			double w;
			
			try
			{
				x = Double.valueOf(xSpeed.getText());
				y = Double.valueOf(ySpeed.getText());
				w = Double.valueOf(wSpeed.getText());
			}
			
			catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySetSpeed(x, y, w);
		}
		
		
		private void notifySetSpeed(final double x, final double y, final double w)
		{
				for (final IMotorInputPanelObserver observer : observers)
				{
					observer.onSetSpeed(x, y, w);
				}
		}
	}
}
