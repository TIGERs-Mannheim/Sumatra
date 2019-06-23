/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.EventQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import net.miginfocom.swing.MigLayout;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TeamPanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -4100647452685537602L;
	
	private final JButton timeoutBtn;
	private final JButton direct;
	private final JButton indirect;
	private final JButton penalty;
	private final JButton yellow;
	private final JButton red;
	private final JButton kickoff;
	private final JButton goal;
	private final JLabel timeoutClock;
	private final JLabel timeoutCount;
	private final JButton place;
	private final JTextField placementPos;
	
	private final ETeamColor color;
	
	
	/**
	 * Constructor.
	 * 
	 * @param color
	 */
	public TeamPanel(final ETeamColor color)
	{
		this.color = color;
		
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));
		
		timeoutBtn = new JButton("Timeout");
		direct = new JButton("Direct");
		indirect = new JButton("Indirect");
		penalty = new JButton("Penalty");
		yellow = new JButton("Yellow Card");
		red = new JButton("Red Card");
		kickoff = new JButton("Kickoff");
		goal = new JButton("Goal");
		timeoutClock = new JLabel("5:00.0");
		timeoutCount = new JLabel("4");
		placementPos = new JTextField("0,0");
		place = new JButton("Place Ball");
		
		timeoutBtn.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromTimeout(color)));
		direct.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromDirect(color)));
		indirect.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromIndirect(color)));
		penalty.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromPenalty(color)));
		kickoff.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromKickoff(color)));
		goal.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromGoal(color)));
		
		place.addActionListener(e -> {
			IVector2 pos;
			try
			{
				pos = AVector2.valueOf(placementPos.getText());
			} catch (final NumberFormatException ex)
			{
				placementPos.setText("Invalid input!");
				pos = null;
			}
			
			notifyNewControlRequest(RefBoxRemoteControlFactory.fromBallPlacement(color, pos));
		});
		
		yellow.addActionListener(
				e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromCard(color, CardType.CARD_YELLOW)));
		red.addActionListener(
				e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromCard(color, CardType.CARD_RED)));
		
		add(timeoutBtn);
		add(goal);
		add(new JLabel("Timeout Clock:"));
		add(timeoutClock);
		add(new JLabel("Timeouts left:"));
		add(timeoutCount);
		add(direct);
		add(penalty);
		add(indirect);
		add(yellow);
		add(kickoff);
		add(red);
		add(placementPos);
		add(place);
		
		setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(color.getColor().darker()), color.name()));
	}
	
	
	/**
	 * @param msg
	 */
	public void update(final SSL_Referee msg)
	{
		EventQueue.invokeLater(() -> {
			long timeoutTime;
			int timeouts;
			
			if (color == ETeamColor.YELLOW)
			{
				timeoutTime = msg.getYellow().getTimeoutTime();
				timeouts = msg.getYellow().getTimeouts();
			} else
			{
				timeoutTime = msg.getBlue().getTimeoutTime();
				timeouts = msg.getBlue().getTimeouts();
			}
			
			// Timeouts yellow
			long minTo = TimeUnit.MICROSECONDS.toMinutes(timeoutTime);
			long secTo = TimeUnit.MICROSECONDS.toSeconds(timeoutTime) % 60;
			long subTo = (TimeUnit.MICROSECONDS.toMillis(timeoutTime) % 1000) / 100;
			timeoutClock.setText(String.format("%d:%02d:%1d", minTo, secTo, subTo));
			timeoutCount.setText(Integer.toString(timeouts));
		});
	}
	
	
	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		EventQueue.invokeLater(() -> {
			timeoutBtn.setEnabled(enable);
			direct.setEnabled(enable);
			indirect.setEnabled(enable);
			penalty.setEnabled(enable);
			yellow.setEnabled(enable);
			red.setEnabled(enable);
			kickoff.setEnabled(enable);
			goal.setEnabled(enable);
			timeoutClock.setEnabled(enable);
			timeoutCount.setEnabled(enable);
			place.setEnabled(enable);
			placementPos.setEnabled(enable);
		});
	}
}
