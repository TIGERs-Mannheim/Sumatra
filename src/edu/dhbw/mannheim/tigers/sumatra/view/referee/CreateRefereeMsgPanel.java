/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private final JButton									halt;
	private final JTextField								goalsYellow;
	private final JTextField								goalsBlue;
	private final JComboBox<Command>						commandBox;
	private final JCheckBox									chkReceive;
	
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
			public void actionPerformed(final ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg();
			}
		});
		
		start = new JButton("Normal start");
		start.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.NORMAL_START);
			}
		});
		
		
		stop = new JButton("Stop");
		stop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.STOP);
			}
		});
		
		
		halt = new JButton("halt");
		halt.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.HALT);
			}
		});
		
		chkReceive = new JCheckBox("Recv ext");
		chkReceive.setSelected(true);
		chkReceive.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				notifyEnableReceive(chkReceive.isSelected());
			}
		});
		this.add(chkReceive, "cell 0 1");
		
		this.add(commandBox, "cell 0 0, span 3 0");
		this.add(sendButton, "cell 1 1, grow x");
		
		this.add(halt, "cell 2 1, grow x");
		this.add(start, "cell 3 0, grow x");
		this.add(stop, "cell 3 1, grow x");
		
		this.add(new JLabel("Blue"), "cell 4 0");
		this.add(goalsBlue, "cell 4 1");
		
		this.add(new JLabel("Yellow"), "cell 5 0");
		this.add(goalsYellow, "cell 5 1");
		
		goalsBlue.setPreferredSize(sendButton.getMaximumSize());
		goalsYellow.setPreferredSize(sendButton.getMaximumSize());
		sendButton.setEnabled(false);
		goalsYellow.setEnabled(false);
		goalsBlue.setEnabled(false);
		commandBox.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
		halt.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void sendOwnRefereeMsg(final Command cmd)
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
		// send Message
		notifySendOwnRefereeMsg(cmd, gBlue, gYellow, (short) 899);
	}
	
	
	private void sendOwnRefereeMsg()
	{
		// create Message
		final Command cmd = (Command) commandBox.getSelectedItem();
		sendOwnRefereeMsg(cmd);
	}
	
	
	/**
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 */
	public void notifySendOwnRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow,
			final short timeLeft)
	{
		synchronized (observers)
		{
			for (final ICreateRefereeMsgObserver observer : observers)
			{
				observer.onSendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft);
			}
		}
	}
	
	
	/**
	 * @param receive
	 */
	public void notifyEnableReceive(final boolean receive)
	{
		synchronized (observers)
		{
			for (final ICreateRefereeMsgObserver observer : observers)
			{
				observer.onEnableReceive(receive);
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
		halt.setEnabled(true);
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
		halt.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param obs
	 */
	public void addObserver(final ICreateRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.add(obs);
		}
	}
	
	
	/**
	 * @param obs
	 */
	public void removeObserver(final ICreateRefereeMsgObserver obs)
	{
		synchronized (observers)
		{
			observers.remove(obs);
		}
	}
}
