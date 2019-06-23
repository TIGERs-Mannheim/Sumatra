/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.view.TextPane;
import net.miginfocom.swing.MigLayout;


/**
 * Incoming referee messages are displayed here.
 * 
 * @author DionH
 */
public class ShowRefereeMsgPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= -508393753936993622L;
																	
	private final TextPane			commandsList;
	private Command					lastCmd				= null;
	private final JLabel				time;
	private final JLabel				timeout;
	private final JLabel				goals;
											
	private final DecimalFormat	df2					= new DecimalFormat("00");
	private final Color				color					= new Color(0, 0, 0);
																	
																	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ShowRefereeMsgPanel()
	{
		setLayout(new MigLayout());
		
		// Goals
		this.add(new JLabel("Goals:"));
		goals = new JLabel();
		goals.setFont(goals.getFont().deriveFont(Font.BOLD));
		this.add(goals);
		
		// Commands
		commandsList = new TextPane(100);
		commandsList.setPreferredSize(new Dimension(250, 50));
		this.add(commandsList, "span 0 3, wrap");
		
		// Time
		this.add(new JLabel("Time:"));
		time = new JLabel();
		time.setFont(time.getFont().deriveFont(Font.BOLD));
		this.add(time, "wrap");
		
		// Timeouts
		this.add(new JLabel("Timeouts:"), "top");
		timeout = new JLabel();
		timeout.setFont(timeout.getFont().deriveFont(Font.BOLD));
		this.add(timeout, "top");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param msg
	 */
	public void newRefereeMsg(final RefereeMsg msg)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Goals
				goals.setText(msg.getTeamInfoYellow().getScore() + " (Y) : (B) " + msg.getTeamInfoBlue().getScore());
				
				// Time
				final long min = TimeUnit.MICROSECONDS.toMinutes(msg.getStageTimeLeft());
				final long sec = TimeUnit.MICROSECONDS.toSeconds(msg.getStageTimeLeft()) - (60 * min);
				time.setText(df2.format(min) + ":" + df2.format(sec) + " (" + msg.getStage().name() + ")");
				
				// Timeouts yellow
				long minTo = TimeUnit.MICROSECONDS.toMinutes(msg.getTeamInfoYellow().getTimeoutTime());
				long secTo = TimeUnit.MICROSECONDS.toSeconds(msg.getTeamInfoYellow().getTimeoutTime()) - (60 * minTo);
				timeout.setText("Y:" + msg.getTeamInfoYellow().getTimeouts() + " (" + df2.format(minTo) + ":"
						+ df2.format(secTo)
						+ ")");
						
				// Timeouts blue
				minTo = TimeUnit.MICROSECONDS.toMinutes(msg.getTeamInfoBlue().getTimeoutTime());
				secTo = TimeUnit.MICROSECONDS.toSeconds(msg.getTeamInfoBlue().getTimeoutTime()) - (60 * minTo);
				timeout.setText(timeout.getText() + " B:" + msg.getTeamInfoBlue().getTimeouts() + " (" + df2.format(minTo)
						+ ":"
						+ df2.format(secTo) + ")");
						
				if (!msg.getCommand().equals(lastCmd))
				{
					// Command
					final StyleContext sc = StyleContext.getDefaultStyleContext();
					final AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
					String msgString = "";
					if (commandsList.getLength() != 0)
					{
						msgString += "\n";
					}
					msgString = msgString + msg.getCommand().toString();
					commandsList.append(msgString, aset);
					lastCmd = msg.getCommand();
				}
			}
		});
	}
	
	
	/**
	 */
	public void init()
	{
		goals.setText("0 (Y) : (B) 0");
		time.setText("00:00");
		timeout.setText("Y:4 (05:00) B:4 (05:00)");
		commandsList.clear();
	}
	
	
	/**
	 */
	public void deinit()
	{
		init();
	}
}