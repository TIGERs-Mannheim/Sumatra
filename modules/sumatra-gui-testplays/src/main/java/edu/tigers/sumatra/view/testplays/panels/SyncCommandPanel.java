/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import javax.swing.*;

import org.apache.commons.lang.StringUtils;

import edu.tigers.sumatra.testplays.commands.SynchronizeCommand;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class SyncCommandPanel extends ACommandDetailsPanel
{
	
	private SynchronizeCommand	command;
	private JTextField			tfSyncGroupVal;
	
	
	/**
	 * Constructs a new SyncCommandPanel
	 * 
	 * @param command The command
	 */
	public SyncCommandPanel(SynchronizeCommand command)
	{
		
		super(command.getCommandType());
		this.command = command;

		tfSyncGroupVal = new JTextField(String.valueOf(command.getSyncGroup()), 10);
		addLine(new JLabel("Sync group:"), tfSyncGroupVal);

		addFillPanel();
	}
	
	
	private boolean validateInputs()
	{
		
		return StringUtils.isNumeric(tfSyncGroupVal.getText());
	}
	
	
	@Override
	void onSave()
	{
		
		if (!validateInputs())
		{
			return;
		}
		
		command.setSyncGroup(Integer.parseInt(tfSyncGroupVal.getText()));
	}
}
