/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

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

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.view.TextPane;
import net.miginfocom.swing.MigLayout;


/**
 * Incoming referee messages are displayed here.
 * 
 * @author DionH
 * @author AndreR <andre@ryll.cc>
 */
public class ShowRefereeMsgPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long serialVersionUID = -508393753936993622L;
	
	private final TextPane commandsList;
	private Command lastCmd = null;
	private final JLabel time;
	private final JLabel goals;
	private final JLabel stage;
	private final JLabel command;
	
	private final DecimalFormat df2 = new DecimalFormat("00");
	private final Color color = new Color(0, 0, 0);
	
	
	/** Constructor. */
	public ShowRefereeMsgPanel()
	{
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));
		
		add(new JLabel("Stage:"));
		stage = new JLabel();
		stage.setFont(stage.getFont().deriveFont(Font.BOLD));
		add(stage);

		add(new JLabel("Last Command:"));
		command = new JLabel();
		command.setFont(stage.getFont().deriveFont(Font.BOLD));
		add(command);
		
		// Goals
		add(new JLabel("Goals:"));
		goals = new JLabel();
		goals.setFont(goals.getFont().deriveFont(Font.BOLD));
		add(goals);
		
		// Time
		add(new JLabel("Time:"));
		time = new JLabel();
		time.setFont(time.getFont().deriveFont(Font.BOLD));
		add(time);
		
		// Commands
		add(new JLabel("All Commands: "), "wrap");
		commandsList = new TextPane(100);
		commandsList.setMaximumSize(new Dimension(commandsList.getMaximumSize().width, this.getPreferredSize().height));
		add(commandsList, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------

	
	/**
	 * @param msg
	 */
	public void update(final SSL_Referee msg)
	{
		//Information on Top
		EventQueue.invokeLater(() -> {
			// Goals
			goals.setText(msg.getYellow().getScore() + " (Y) : (B) " + msg.getBlue().getScore());
			// Time
			final long min = TimeUnit.MICROSECONDS.toMinutes(msg.getStageTimeLeft());
			final long sec = TimeUnit.MICROSECONDS.toSeconds(msg.getStageTimeLeft()) - (60 * min);
			time.setText(df2.format(min) + ":" + df2.format(sec));
			stage.setText(msg.getStage().name());
			command.setText(msg.getCommand().name());
		});

		// Command History
		EventQueue.invokeLater(() -> {
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
		});
	}
}