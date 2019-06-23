/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import net.miginfocom.swing.MigLayout;


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
	private static final long serialVersionUID = 5629008300026034985L;
	
	private final JButton sendButton;
	private final JButton start;
	private final JButton forceStart;
	private final JButton stop;
	private final JButton halt;
	private final JTextField goalsYellow;
	private final JTextField goalsBlue;
	private final JComboBox<Command> commandBox;
	private final JCheckBox chkReceive;
	private final JTextField placementPos;
	private final transient List<ICreateRefereeMsgObserver> observers = new ArrayList<>();
	
	
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
		placementPos = new JTextField("0,0");
		
		commandBox = new JComboBox<>(Command.values());
		
		sendButton = new JButton("Send!");
		sendButton.addActionListener(e -> CreateRefereeMsgPanel.this.sendOwnRefereeMsg());
		
		start = new JButton("Normal Start");
		start.addActionListener(e -> CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.NORMAL_START));
		
		forceStart = new JButton("Force Start");
		forceStart.addActionListener(e -> CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.FORCE_START));
		
		stop = new JButton("Stop");
		stop.addActionListener(e -> CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.STOP));
		
		halt = new JButton("halt");
		halt.addActionListener(e -> CreateRefereeMsgPanel.this.sendOwnRefereeMsg(Command.HALT));
		
		chkReceive = new JCheckBox("Recv ext");
		chkReceive.setSelected(true);
		chkReceive.addActionListener(e -> notifyEnableReceive(chkReceive.isSelected()));
		
		this.add(chkReceive, "cell 0 1");
		
		this.add(commandBox, "cell 0 0, span 3 0");
		this.add(sendButton, "cell 1 1, grow x");
		
		this.add(halt, "cell 2 1, grow x");
		this.add(start, "cell 3 0, grow x");
		this.add(stop, "cell 3 1, grow x");
		this.add(forceStart, "cell 4 0, grow x");
		
		this.add(new JLabel("Blue"), "cell 5 0");
		this.add(goalsBlue, "cell 5 1");
		
		this.add(new JLabel("Yellow"), "cell 6 0");
		this.add(goalsYellow, "cell 6 1");
		
		
		this.add(new JLabel("Ball place pos"), "cell 7 0");
		this.add(placementPos, "cell 7 1");
		
		goalsBlue.setPreferredSize(sendButton.getMaximumSize());
		goalsYellow.setPreferredSize(sendButton.getMaximumSize());
		placementPos.setPreferredSize(sendButton.getMaximumSize());
		sendButton.setEnabled(false);
		goalsYellow.setEnabled(false);
		goalsBlue.setEnabled(false);
		placementPos.setEnabled(false);
		commandBox.setEnabled(false);
		forceStart.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
		halt.setEnabled(false);
		
		add(new TeamPanel(ETeamColor.YELLOW));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void sendOwnRefereeMsg(final Command cmd)
	{
		if ((cmd == Command.BALL_PLACEMENT_BLUE) || (cmd == Command.BALL_PLACEMENT_YELLOW))
		{
			Optional<IVector2> place = getPlacementPos();
			if (place.isPresent())
			{
				if (cmd == Command.BALL_PLACEMENT_BLUE)
				{
					notifyRefBoxRequest(RefBoxRemoteControlFactory.fromBallPlacement(ETeamColor.BLUE, place.get()));
				} else
				{
					notifyRefBoxRequest(RefBoxRemoteControlFactory.fromBallPlacement(ETeamColor.YELLOW, place.get()));
				}
				
				return;
			}
		}
		
		// send Message
		notifyRefBoxRequest(RefBoxRemoteControlFactory.fromCommand(cmd));
	}
	
	
	private Optional<IVector2> getPlacementPos()
	{
		IVector2 pos;
		try
		{
			pos = AVector2.valueOf(placementPos.getText());
		} catch (final NumberFormatException e)
		{
			placementPos.setText("Invalid input!");
			pos = null;
		}
		
		return Optional.ofNullable(pos);
	}
	
	
	private void sendOwnRefereeMsg()
	{
		// create Message
		final Command cmd = (Command) commandBox.getSelectedItem();
		sendOwnRefereeMsg(cmd);
	}
	
	
	private void notifyRefBoxRequest(final SSL_RefereeRemoteControlRequest request)
	{
		for (ICreateRefereeMsgObserver observer : observers)
		{
			observer.onRefBoxRequest(request);
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
		placementPos.setEnabled(true);
		commandBox.setEnabled(true);
		start.setEnabled(true);
		stop.setEnabled(true);
		halt.setEnabled(true);
		forceStart.setEnabled(true);
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
		forceStart.setEnabled(false);
		placementPos.setEnabled(false);
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
