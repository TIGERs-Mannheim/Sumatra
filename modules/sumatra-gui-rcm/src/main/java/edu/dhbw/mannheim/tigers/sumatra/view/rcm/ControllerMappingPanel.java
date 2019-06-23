/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.IRCMConfigChangedObserver;
import edu.tigers.sumatra.rcm.ExtIdentifier;
import edu.tigers.sumatra.rcm.RcmAction;
import edu.tigers.sumatra.rcm.RcmActionMapping;


/**
 * Panel for a single Controller mapping
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ControllerMappingPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long								serialVersionUID	= -8086651107326508468L;
	private static final Logger							log					= Logger.getLogger(ControllerMappingPanel.class
																									.getName());
	private final RcmActionMapping						actionMapping;
	private final List<IRCMConfigChangedObserver>	observers;
	
	private final JComboBox<RcmAction>					actionComboBox;
	private final JPanel										identifiersPanel	= new JPanel();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param actionMapping
	 * @param observers
	 */
	public ControllerMappingPanel(final RcmActionMapping actionMapping, final List<IRCMConfigChangedObserver> observers)
	{
		this.actionMapping = actionMapping;
		this.observers = observers;
		
		actionComboBox = createActionList();
		updateIdentifiersPanel();
		JButton btnAssign = new JButton("Assign");
		btnAssign.addActionListener(new AssignListener());
		
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		identifiersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(actionComboBox);
		add(btnAssign);
		add(identifiersPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private JComboBox<RcmAction> createActionList()
	{
		JComboBox<RcmAction> comboBox = new JComboBox<RcmAction>();
		for (RcmAction e : RcmAction.getAllActions())
		{
			comboBox.addItem(e);
		}
		comboBox.setSelectedItem(actionMapping.getAction());
		comboBox.addItemListener(new ItemListener()
		{
			
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				actionMapping.setAction((RcmAction) e.getItem());
				notifyChanged();
			}
		});
		return comboBox;
	}
	
	
	private void updateIdentifiersPanel()
	{
		identifiersPanel.removeAll();
		for (ExtIdentifier extId : actionMapping.getIdentifiers())
		{
			identifiersPanel.add(createSingleIdPanel(extId));
		}
	}
	
	
	private JPanel createSingleIdPanel(final ExtIdentifier extId)
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.setAlignmentY(CENTER_ALIGNMENT);
		JTextField txtId = new JTextField(extId.toString(), 8);
		txtId.setEditable(false);
		txtId.setHorizontalAlignment(SwingConstants.CENTER);
		// --- reset color to white (setEditable(false) turns textfield grey) ---
		txtId.setBackground(Color.white);
		JTextField txtCharge = new JTextField(2);
		panel.add(txtId);
		txtCharge.setHorizontalAlignment(SwingConstants.CENTER);
		txtCharge.addFocusListener(new ChargeChangeListener(txtCharge, extId));
		double charge = extId.getParams().getChargeTime();
		if (charge < 0.00001)
		{
			txtCharge.setText("");
		} else
		{
			txtCharge.setText(String.valueOf(charge));
		}
		panel.add(txtCharge);
		return panel;
	}
	
	
	/**
	 * @param mapping
	 */
	public void updateConfig(final RcmActionMapping mapping)
	{
		actionComboBox.setSelectedItem(mapping.getAction());
		updateIdentifiersPanel();
	}
	
	
	private void notifyChanged()
	{
		synchronized (observers)
		{
			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onActionMappingChanged(actionMapping);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class ChargeChangeListener implements FocusListener
	{
		private final JTextField		txtField;
		private final ExtIdentifier	extId;
		
		
		/**
		 * @param txtField
		 * @param extId
		 */
		public ChargeChangeListener(final JTextField txtField, final ExtIdentifier extId)
		{
			this.txtField = txtField;
			this.extId = extId;
		}
		
		
		private void check()
		{
			boolean valid = false;
			double charge = 0;
			if (txtField.getText().isEmpty())
			{
				valid = true;
			} else
			{
				try
				{
					charge = Double.valueOf(txtField.getText());
					if (charge >= 0)
					{
						valid = true;
					}
				} catch (NumberFormatException err)
				{
				}
			}
			
			if (valid)
			{
				txtField.setForeground(Color.black);
				int idx = actionMapping.getIdentifiers().indexOf(extId);
				if (idx == -1)
				{
					log.warn("Unexpected: Index not found.");
				} else
				{
					extId.getParams().setChargeTime(charge);
					notifyChanged();
				}
				
			} else
			{
				txtField.setForeground(Color.red);
			}
		}
		
		
		@Override
		public void focusGained(final FocusEvent e)
		{
		}
		
		
		@Override
		public void focusLost(final FocusEvent e)
		{
			check();
		}
	}
	
	private class AssignListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onSelectAssignment(actionMapping);
				}
			}
		}
	}
}
