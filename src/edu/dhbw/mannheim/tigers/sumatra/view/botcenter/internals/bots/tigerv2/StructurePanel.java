/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemQuery.EQueryType;


/**
 * Set structure of bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
		
		
		/**
		 * @param type
		 */
		void onQuery(EQueryType type);
	}
	
	private final List<IStructureObserver>	observers	= new CopyOnWriteArrayList<IStructureObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IStructureObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IStructureObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyNewStructure(final Structure structure)
	{
		for (IStructureObserver observer : observers)
		{
			observer.onNewStructure(structure);
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
		JButton btnQuery = new JButton("Query");
		btnQuery.addActionListener(new QueryActionListener());
		add(btnSave, "span 1");
		add(btnQuery, "span 1");
	}
	
	
	/**
	 * @param structure
	 */
	public void setStructure(final Structure structure)
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
		public void actionPerformed(final ActionEvent e)
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
	
	private class QueryActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IStructureObserver observer : observers)
			{
				observer.onQuery(EQueryType.CTRL_STRUCTURE);
			}
		}
	}
}
