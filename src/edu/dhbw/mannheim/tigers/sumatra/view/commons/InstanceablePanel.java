/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.commons;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * Panel for creating custom instances
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InstanceablePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= -6272636064374504265L;
	private static final Logger						log					= Logger.getLogger(InstanceablePanel.class.getName());
	private final JComboBox<IInstanceableEnum>	cbbInstances;
	private final JPanel									inputPanel;
	private final List<JTextField>					inputFields			= new ArrayList<JTextField>();
	private final JButton								btnCreate;
	private final List<IInstanceableObserver>		observers			= new CopyOnWriteArrayList<IInstanceableObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewInstance(final Object instance)
	{
		synchronized (observers)
		{
			for (IInstanceableObserver observer : observers)
			{
				observer.onNewInstance(instance);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param instanceableEnums
	 */
	public InstanceablePanel(final IInstanceableEnum[] instanceableEnums)
	{
		cbbInstances = new JComboBox<IInstanceableEnum>(instanceableEnums);
		CbbInstancesActionListener cbbInstAl = new CbbInstancesActionListener();
		cbbInstances.addActionListener(cbbInstAl);
		inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0, 2));
		btnCreate = new JButton("Create");
		btnCreate.addActionListener(new CreateInstanceActionListener());
		
		cbbInstAl.actionPerformed(null);
		
		setLayout(new BorderLayout());
		add(cbbInstances, BorderLayout.NORTH);
		add(inputPanel, BorderLayout.CENTER);
		add(btnCreate, BorderLayout.SOUTH);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param item
	 */
	public final void setSelectedItem(final Enum<?> item)
	{
		cbbInstances.setSelectedItem(item);
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		cbbInstances.setEnabled(enabled);
		inputPanel.setEnabled(enabled);
		btnCreate.setEnabled(enabled);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class CbbInstancesActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			IInstanceableEnum instance = (IInstanceableEnum) cbbInstances.getSelectedItem();
			inputPanel.removeAll();
			inputFields.clear();
			for (InstanceableParameter param : instance.getInstanceableClass().getParams())
			{
				inputPanel.add(new JLabel(param.getDescription()));
				int size = param.getDefaultValue().length() + 2;
				JTextField textField = new JTextField(param.getDefaultValue(), size);
				inputPanel.add(textField);
				inputFields.add(textField);
			}
			updateUI();
		}
	}
	
	private class CreateInstanceActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			IInstanceableEnum instanceName = (IInstanceableEnum) cbbInstances.getSelectedItem();
			int i = 0;
			List<Object> params = new ArrayList<Object>(instanceName.getInstanceableClass().getParams().size());
			for (InstanceableParameter param : instanceName.getInstanceableClass().getParams())
			{
				JTextField textField = inputFields.get(i);
				try
				{
					Object value = param.parseString(textField.getText());
					params.add(value);
				} catch (NumberFormatException err)
				{
					log.error("Could not parse parameter: " + textField.getText(), err);
					return;
				}
				i++;
			}
			Object instance;
			try
			{
				instance = instanceName.getInstanceableClass().newInstance(params.toArray());
				notifyNewInstance(instance);
			} catch (NotCreateableException err)
			{
				log.error("Could not create instance: " + instanceName, err);
			}
		}
	}
}
