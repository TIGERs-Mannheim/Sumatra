/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.Structure;


/**
 * Set structure of bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class StructurePanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID	= 186301220589710406L;
	private static final Logger	log					= Logger.getLogger(StructurePanel.class.getName());
	
	private final JTextField		txtFrontAngle		= new JTextField(5);
	private final JTextField		txtBackAngle		= new JTextField(5);
	private final JTextField		txtBotRadius		= new JTextField(5);
	private final JTextField		txtWheelRadius		= new JTextField(5);
	private final JTextField		txtMass				= new JTextField(5);
	
	
	/**
	 */
	public interface IStructureObserver
	{
		/**
		 * @param structure
		 */
		void onNewStructure(Structure structure);
	}
	
	private final List<IStructureObserver>	observers	= new CopyOnWriteArrayList<IStructureObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(IStructureObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IStructureObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewStructure(Structure structure)
	{
		synchronized (observers)
		{
			for (IStructureObserver observer : observers)
			{
				observer.onNewStructure(structure);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public StructurePanel()
	{
		setLayout(new MigLayout("wrap 2"));
		setBorder(BorderFactory.createTitledBorder("Structure"));
		add(new JLabel("front angle: "));
		add(txtFrontAngle);
		add(new JLabel("back angle: "));
		add(txtBackAngle);
		add(new JLabel("bot radius: "));
		add(txtBotRadius);
		add(new JLabel("wheel radius: "));
		add(txtWheelRadius);
		add(new JLabel("mass: "));
		add(txtMass);
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new SaveActionListener());
		add(btnSave, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param structure
	 */
	public void setStructure(Structure structure)
	{
		txtFrontAngle.setText(String.valueOf(structure.getFrontAngle()));
		txtBackAngle.setText(String.valueOf(structure.getBackAngle()));
		txtBotRadius.setText(String.valueOf(structure.getBotRadius()));
		txtWheelRadius.setText(String.valueOf(structure.getWheelRadius()));
		txtMass.setText(String.valueOf(structure.getMass()));
	}
	
	private class SaveActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				float frontAngle = Float.parseFloat(txtFrontAngle.getText());
				float backAngle = Float.parseFloat(txtBackAngle.getText());
				float botRadius = Float.parseFloat(txtBotRadius.getText());
				float wheelRadius = Float.parseFloat(txtWheelRadius.getText());
				float mass = Float.parseFloat(txtMass.getText());
				Structure structure = new Structure(frontAngle, backAngle, botRadius, wheelRadius, mass);
				notifyNewStructure(structure);
			} catch (NumberFormatException err)
			{
				log.error("Number could not be parsed.", err);
			}
		}
	}
}
