/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import javax.swing.*;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import org.apache.commons.lang.StringUtils;

import edu.tigers.sumatra.testplays.commands.PassCommand;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PassCommandPanel extends ACommandDetailsPanel
{
	
	private PassCommand	command;
	private JTextField	tfReceiveGroupVal;
	private JComboBox<EKickerDevice>	cbKickerDevice;
	
	
	/**
	 * Constructs a new SyncCommandPanel
	 *
	 * @param command The command
	 */
	public PassCommandPanel(PassCommand command)
	{
		
		super(command.getCommandType());
		this.command = command;
		
		tfReceiveGroupVal = new JTextField(String.valueOf(command.getPassGroup()), 10);
		addLine(new JLabel("Pass group:"), tfReceiveGroupVal);

		JLabel lblKickerDevice = new JLabel("Kicker device:");
		cbKickerDevice = new JComboBox<>(EKickerDevice.values());

		cbKickerDevice.setSelectedItem(command.getKickerDevice());

		addLine(lblKickerDevice, cbKickerDevice);
		
		addFillPanel();
	}
	
	
	private boolean validateInputs()
	{
		
		return StringUtils.isNumeric(tfReceiveGroupVal.getText());
	}
	
	
	@Override
	void onSave()
	{
		
		if (!validateInputs())
		{
			return;
		}
		
		command.setPassGroup(Integer.parseInt(tfReceiveGroupVal.getText()));
		command.setKickerDevice((EKickerDevice) cbKickerDevice.getSelectedItem());
	}
}
