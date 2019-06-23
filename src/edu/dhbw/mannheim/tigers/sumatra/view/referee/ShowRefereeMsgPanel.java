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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
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
	private static final long			serialVersionUID	= -508393753936993622L;
	
	private final TextPane				commandsList;
	private final JLabel					time;
	private final JLabel					goals;
	
	private int								oldId;
	private final DecimalFormat		df2					= new DecimalFormat("00");
	Color color = new Color(0, 0, 0);
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ShowRefereeMsgPanel()
	{
		this.setLayout(new MigLayout());
		this.setBorder(BorderFactory.createTitledBorder("Referee Messages"));
		
		oldId = -1;
		
		// Goals
		goals = new JLabel("0 : 0");
		goals.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
		this.add(goals, "cell 0 0");
		
		// Time
		time = new JLabel("Time:\t00:00");
		this.add(time, "cell 0 1");
		
		// Commands
		commandsList = new TextPane(100);
		commandsList.setPreferredSize(new Dimension(150, 200));
		this.add(commandsList, "cell 1 0 , span 2 2");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void newRefereeMsg(final RefereeMsg msg)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				// Goals
				// int a = msg.goalsTigers & 0x80;
				goals.setText(msg.goalsTigers + " : " + msg.goalsEnemies);
				
				// Time
				final long timePassed = 900 - msg.timeRemaining; // [s]
				final long min = TimeUnit.SECONDS.toMinutes(timePassed);
				final long sec = timePassed % 60;
				time.setText("Time:\t" + df2.format(min) + ":" + df2.format(sec));
				
				// Command
				if (msg.id != oldId)
				{
					
					StyleContext sc = StyleContext.getDefaultStyleContext();
					AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
					commandsList.append(msg.cmd.toString()+"\n", aset);
					oldId = msg.id;
				}
			}
		});
	}
	

	public void start()
	{
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
