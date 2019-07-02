/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.testplays.commands.KickCommand;
import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class KickCommandPanel extends ACommandDetailsPanel
{
	
	private static final Logger		log	= Logger
			.getLogger(KickCommandPanel.class.getName());
	
	private KickCommand					command;
	
	private JTextField					tfSpeed;
	private JTextField					tfXCoord;
	private JTextField					tfYCoord;
	private JComboBox<EKickerDevice>	cbKickerDevice;
	
	
	/**
	 * Creates a new KickCommandPanel
	 * 
	 * @param command The KickCommand
	 */
	public KickCommandPanel(KickCommand command)
	{
		
		super(command.getCommandType());
		
		this.command = command;
		
		JLabel lblDest = new JLabel("Destination:");
		
		JPanel destPanel = new JPanel(new GridLayout(1, 0));
		tfXCoord = new JTextField(String.valueOf(command.getDestination().getX()), 5);
		tfYCoord = new JTextField(String.valueOf(command.getDestination().getY()), 5);
		
		destPanel.add(tfXCoord);
		destPanel.add(tfYCoord);
		
		addLine(lblDest, destPanel);
		
		JLabel lblSpeed = new JLabel("Kick speed:");
		tfSpeed = new JTextField(String.valueOf(command.getKickSpeed()), 4);
		
		addLine(lblSpeed, tfSpeed);

		JLabel lblKickerDevice = new JLabel("Kicker device:");
		cbKickerDevice = new JComboBox<>(EKickerDevice.values());

		cbKickerDevice.setSelectedItem(command.getKickerDevice());

		addLine(lblKickerDevice, cbKickerDevice);
		
		addFillPanel();
	}
	
	
	@Override
	void onSave()
	{
		if (!verifyInput())
		{
			log.warn("Invalid input.");
			return;
		}
		
		command.setKickSpeed(Double.parseDouble(tfSpeed.getText()));
		command.setDestination(new Point(Double.parseDouble(tfXCoord.getText()), Double.parseDouble(tfYCoord.getText())));
		command.setKickerDevice((EKickerDevice) cbKickerDevice.getSelectedItem());
	}
	
	
	private boolean verifyInput()
	{
		
		return NumberUtils.isNumber(tfXCoord.getText()) && NumberUtils.isNumber(tfYCoord.getText())
				&& NumberUtils.isNumber(tfSpeed.getText());
	}
}
