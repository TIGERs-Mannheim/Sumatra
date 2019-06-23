/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import javax.swing.*;

import org.apache.commons.lang.StringUtils;

import edu.tigers.sumatra.testplays.commands.ReceiveCommand;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class ReceiveCommandPanel extends ACommandDetailsPanel
{
	
	private ReceiveCommand	command;
	private JTextField		tfPassGroupVal;
	
	
	/**
	 * Constructs a new SyncCommandPanel
	 *
	 * @param command The command
	 */
	public ReceiveCommandPanel(ReceiveCommand command)
	{
		
		super(command.getCommandType());
		this.command = command;
		
		tfPassGroupVal = new JTextField(String.valueOf(command.getPassGroup()), 10);
		addLine(new JLabel("Pass group:"), tfPassGroupVal);
		
		addFillPanel();
	}
	
	
	private boolean validateInputs()
	{
		
		return StringUtils.isNumeric(tfPassGroupVal.getText());
	}
	
	
	@Override
	void onSave()
	{
		
		if (!validateInputs())
		{
			return;
		}
		
		command.setPassGroup(Integer.parseInt(tfPassGroupVal.getText()));
	}
}
