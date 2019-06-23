/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.view.testplays.panels;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import edu.tigers.sumatra.testplays.commands.RedirectCommand;
import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class RedirectCommandPanel extends ACommandDetailsPanel
{
	
	private RedirectCommand command;
	private JTextField tfRedirectGroupVal;
	
	private JTextField tfXCoord;
	private JTextField tfYCoord;
	
	
	/**
	 * Default constructor
	 *
	 * @param redirectCommand
	 */
	public RedirectCommandPanel(final RedirectCommand redirectCommand)
	{
		super(redirectCommand.getCommandType());
		this.command = redirectCommand;
		
		tfRedirectGroupVal = new JTextField(String.valueOf(command.getRedirectGroup()), 10);
		addLine(new JLabel("Pass group:"), tfRedirectGroupVal);
		
		JLabel lblDest = new JLabel("Destination:");
		
		JPanel destPanel = new JPanel(new GridLayout(1, 0));
		tfXCoord = new JTextField(String.valueOf(command.getDestination().getX()), 5);
		tfYCoord = new JTextField(String.valueOf(command.getDestination().getY()), 5);
		
		destPanel.add(tfXCoord);
		destPanel.add(tfYCoord);
		
		addLine(lblDest, destPanel);
		
		addFillPanel();
	}
	
	
	private boolean validateInputs()
	{
		return StringUtils.isNumeric(tfRedirectGroupVal.getText()) && NumberUtils.isNumber(tfXCoord.getText())
				&& NumberUtils.isNumber(tfYCoord.getText());
	}
	
	
	@Override
	void onSave()
	{
		
		if (!validateInputs())
		{
			return;
		}
		
		command.setRedirectGroup(Integer.parseInt(tfRedirectGroupVal.getText()));
		command.setDestination(new Point(Double.parseDouble(tfXCoord.getText()), Double.parseDouble(tfYCoord.getText())));
	}
}
