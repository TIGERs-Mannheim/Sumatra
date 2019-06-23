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

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TextPane;


/**
 * Incoming referee messages are displayed here.
 * 
 * @author DionH
 * 
 */
public class ShowRefereeMsgPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= -508393753936993622L;
	
	private final TextPane			commandsList;
	private final JLabel				time;
	private final JLabel				timeout;
	private final JLabel				goals;
	
	private long						oldId;
	private final DecimalFormat	df2					= new DecimalFormat("00");
	Color									color					= new Color(0, 0, 0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ShowRefereeMsgPanel()
	{
		setLayout(new MigLayout());
		
		oldId = -1;
		
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
				// int a = msg.goalsTigers & 0x80;
				goals.setText(msg.getTeamInfoTigers().getScore() + " (T) : (E) " + msg.getTeamInfoThem().getScore());
				
				// Time
				final long min = TimeUnit.MICROSECONDS.toMinutes(msg.getStageTimeLeft());
				final long sec = TimeUnit.MICROSECONDS.toSeconds(msg.getStageTimeLeft()) - (60 * min);
				time.setText(df2.format(min) + ":" + df2.format(sec) + " (" + msg.getStage().name() + ")");
				
				// Timeouts
				final long minTo = TimeUnit.MICROSECONDS.toMinutes(msg.getTeamInfoTigers().getTimeoutTime());
				final long secTo = TimeUnit.MICROSECONDS.toSeconds(msg.getTeamInfoTigers().getTimeoutTime()) - (60 * minTo);
				timeout.setText(msg.getTeamInfoTigers().getTimeouts() + " (" + df2.format(minTo) + ":" + df2.format(secTo)
						+ ")");
				
				// Command
				if (msg.getCommandCounter() != oldId)
				{
					final StyleContext sc = StyleContext.getDefaultStyleContext();
					final AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
					String msgString = "";
					if (commandsList.getLength() != 0)
					{
						msgString += "\n";
					}
					msgString = msgString + msg.getCommand().toString();
					commandsList.append(msgString, aset);
					oldId = msg.getCommandCounter();
				}
			}
		});
	}
	
	
	/**
	 */
	public void init()
	{
		goals.setText("0 (T) : (E) 0");
		time.setText("00:00");
		timeout.setText("4 (05:00)");
		commandsList.clear();
	}
	
	
	/**
	 */
	public void deinit()
	{
		init();
	}
}