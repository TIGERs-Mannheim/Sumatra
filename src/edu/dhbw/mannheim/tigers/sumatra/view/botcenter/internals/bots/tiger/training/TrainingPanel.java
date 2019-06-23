/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.06.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.training;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;

/**
 * Training panel for modified kinematics.
 * 
 * @author AndreR
 * 
 */
public class TrainingPanel extends JPanel
{
	public interface ITrainingPanelObserver
	{
		void onStartTraining();
		void onTrainedMove(IVector2 dir, float w);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 6689893277248270747L;
	private final List<ITrainingPanelObserver> observers = new ArrayList<ITrainingPanelObserver>();
	
	private JTextField dirX = null;
	private JTextField dirY = null;
	private JTextField rot = null;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TrainingPanel()
	{
		setLayout(new MigLayout("wrap 2", "[50]10[100,fill]"));
		
		dirX = new JTextField();
		dirY = new JTextField();
		rot = new JTextField();
		
		JButton start = new JButton("Move");
		start.addActionListener(new Move());
		
		JButton train = new JButton("Train");
		train.addActionListener(new Train());
		
		add(train, "span 2");
		add(new JLabel("X:"));
		add(dirX);
		add(new JLabel("Y:"));
		add(dirY);
		add(new JLabel("W:"));
		add(rot);
		add(start, "span 2");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ITrainingPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
		}
	}
	
	
	public void removeObserver(ITrainingPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
		}
	}
	
	private void notifyStartTraining()
	{
		synchronized(observers)
		{
			for (ITrainingPanelObserver observer : observers)
			{
				observer.onStartTraining();
			}
		}
	}
	
	private void notifyTrainedMove(IVector2 dir, float w)
	{
		synchronized(observers)
		{
			for (ITrainingPanelObserver observer : observers)
			{
				observer.onTrainedMove(dir, w);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class Move implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Vector2 dir = new Vector2();
			float w;
			try
			{
				dir.x = Float.parseFloat(dirX.getText());
				dir.y = Float.parseFloat(dirY.getText());
				w = Float.parseFloat(rot.getText());
			}
			catch(NumberFormatException ex)
			{
				return;
			}
			
			notifyTrainedMove(dir, w);
		}
	}
	
	private class Train implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyStartTraining();
		}
	}
}
