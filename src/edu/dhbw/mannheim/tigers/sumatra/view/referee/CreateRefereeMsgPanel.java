/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IOwnRefereeMsgObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IOwnRefereeMsgObservable;


/**
 * Panel where you can create your own Referee Messages.
 * 
 * @author FriederB, MalteM
 */
public class CreateRefereeMsgPanel extends JPanel implements IOwnRefereeMsgObservable
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= 5629008300026034985L;
	
	private final JButton								sendButton;
	private final JTextField							goalsYellow;
	private final JTextField							goalsBlue;
	private final JComboBox								commandBox;
	
	private int												id;
	
	private final List<IOwnRefereeMsgObserver>	observers			= new ArrayList<IOwnRefereeMsgObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CreateRefereeMsgPanel()
	{
		setLayout(new MigLayout("wrap 3"));
		setBorder(BorderFactory.createTitledBorder("Create Your Own Referee Messages"));
		
		goalsYellow = new JTextField("0");
		goalsBlue = new JTextField("0");
		String[] commands = new String[ERefereeCommand.values().length];
		for (int i = 0; i < ERefereeCommand.values().length; i++)
		{
			commands[i] = ERefereeCommand.values()[i].toString();
		}
		commandBox = new JComboBox(commands);
		
		sendButton = new JButton("Send!");
		sendButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg();
			}
		});
		
		this.add(new JLabel("Goals Blue"), "cell 0 0");
		this.add(goalsBlue, "cell 0 1");
		
		this.add(new JLabel("Goals Yellow"), "cell 1 0");
		this.add(goalsYellow, "cell 1 1");
		
		this.add(commandBox, "cell 0 2, span 2 1");
		this.add(sendButton, "cell 2 1, span 1 2, grow y");
		
		goalsBlue.setPreferredSize(sendButton.getMaximumSize());
		goalsYellow.setPreferredSize(sendButton.getMaximumSize());
		sendButton.setEnabled(false);
		goalsYellow.setEnabled(false);
		goalsBlue.setEnabled(false);
		commandBox.setEnabled(false);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void sendOwnRefereeMsg()
	{
		// check whether input goals are numbers
		int b;
		int y;
		try
		{
			b = Integer.parseInt(goalsBlue.getText());
			y = Integer.parseInt(goalsYellow.getText());
		} catch (NumberFormatException e)
		{
			goalsBlue.setText("Not a");
			goalsYellow.setText("Number!");
			return;
		}
		
		// create Message
		ERefereeCommand cmd = ERefereeCommand.valueOf((String) commandBox.getSelectedItem());
		RefereeMsg msg = new RefereeMsg(id, cmd, b, y, (short) 899);
		
		// send Message
		notifyNewOwnRefereeMsg(msg);
		id++;
	}
	

	public void notifyNewOwnRefereeMsg(RefereeMsg refMsg)
	{
		synchronized (observers)
		{
			for (IOwnRefereeMsgObserver observer : observers)
			{
				observer.onNewOwnRefereeMsg(refMsg);
			}
		}
	}
	

	public void start()
	{
		sendButton.setEnabled(true);
		goalsYellow.setEnabled(true);
		goalsBlue.setEnabled(true);
		commandBox.setEnabled(true);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addOwnRefMsgObserver(IOwnRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.add(obs);
		}
	}
	

	@Override
	public void removeOwnRefMsgObserver(IOwnRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.remove(obs);
		}
	}
}
