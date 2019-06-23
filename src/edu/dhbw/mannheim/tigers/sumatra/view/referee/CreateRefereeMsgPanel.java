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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;


/**
 * Panel where you can create your own Referee Messages.
 * 
 * @author FriederB, MalteM
 */
public class CreateRefereeMsgPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= 5629008300026034985L;
	
	private final JButton									sendButton;
	private final JButton									start;
	private final JButton									stop;
	private final JTextField								goalsYellow;
	private final JTextField								goalsBlue;
	private final JComboBox<Command>						commandBox;
	
	private int													id;
	
	private final List<ICreateRefereeMsgObserver>	observers			= new ArrayList<ICreateRefereeMsgObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public CreateRefereeMsgPanel()
	{
		setLayout(new MigLayout("wrap 4"));
		
		goalsYellow = new JTextField("0");
		goalsBlue = new JTextField("0");
		
		commandBox = new JComboBox<Command>(Command.values());
		
		sendButton = new JButton("Send!");
		sendButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg();
			}
		});
		
		start = new JButton("Normal start");
		start.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				commandBox.setSelectedItem(Command.NORMAL_START);
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg();
			}
		});
		
		
		stop = new JButton("Stop");
		stop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				commandBox.setSelectedItem(Command.STOP);
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg();
			}
		});
		
		
		this.add(new JLabel("Goals Blue"), "cell 0 0");
		this.add(goalsBlue, "cell 0 1");
		
		this.add(new JLabel("Goals Yellow"), "cell 1 0");
		this.add(goalsYellow, "cell 1 1");
		
		this.add(new JLabel("Command"), "cell 2 0");
		this.add(commandBox, "cell 2 1");
		
		this.add(sendButton, "cell 3 0, span 0 2, grow y");
		
		this.add(start, "cell 4 0, grow x");
		this.add(stop, "cell 4 1, grow x");
		
		goalsBlue.setPreferredSize(sendButton.getMaximumSize());
		goalsYellow.setPreferredSize(sendButton.getMaximumSize());
		sendButton.setEnabled(false);
		goalsYellow.setEnabled(false);
		goalsBlue.setEnabled(false);
		commandBox.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void sendOwnRefereeMsg()
	{
		// check whether input goals are numbers
		int gBlue;
		int gYellow;
		try
		{
			gBlue = Integer.parseInt(goalsBlue.getText());
			gYellow = Integer.parseInt(goalsYellow.getText());
		} catch (final NumberFormatException e)
		{
			goalsBlue.setText("Not a");
			goalsYellow.setText("Number!");
			return;
		}
		
		// create Message
		final Command cmd = (Command) commandBox.getSelectedItem();
		
		// send Message
		notifySendOwnRefereeMsg(id, cmd, gBlue, gYellow, (short) 899);
		id++;
	}
	
	
	/**
	 * @param id
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 */
	public void notifySendOwnRefereeMsg(int id, Command cmd, int goalsBlue, int goalsYellow, short timeLeft)
	{
		synchronized (observers)
		{
			for (final ICreateRefereeMsgObserver observer : observers)
			{
				observer.onSendOwnRefereeMsg(id, cmd, goalsBlue, goalsYellow, timeLeft);
			}
		}
	}
	
	
	/**
	 */
	public void init()
	{
		sendButton.setEnabled(true);
		goalsYellow.setEnabled(true);
		goalsBlue.setEnabled(true);
		commandBox.setEnabled(true);
		start.setEnabled(true);
		stop.setEnabled(true);
		id = 0;
	}
	
	
	/**
	 */
	public void deinit()
	{
		sendButton.setEnabled(false);
		goalsYellow.setEnabled(false);
		goalsBlue.setEnabled(false);
		commandBox.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
		id = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param obs
	 */
	public void addObserver(ICreateRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.add(obs);
		}
	}
	
	
	/**
	 * @param obs
	 */
	public void removeObserver(ICreateRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.remove(obs);
		}
	}
}
