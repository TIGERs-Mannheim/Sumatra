/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.referee.view;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.util.concurrent.TimeUnit;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TeamPanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -4100647452685537602L;

	private final JButton timeoutBtn;
	private final JButton direct;
	private final JButton penalty;
	private final JButton yellow;
	private final JButton red;
	private final JButton kickoff;
	private final JButton goal;
	private final JLabel timeoutClock;
	private final JLabel timeoutCount;
	private final JButton place;
	private final JTextField placementPos;
	private final JSpinner goalkeeperNumber;

	private final ETeamColor color;
	private int goals = 0;


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
		penalty = new JButton("Penalty");
		yellow = new JButton("Yellow Card");
		red = new JButton("Red Card");
		kickoff = new JButton("Kickoff");
		goal = new JButton("Goal");
		timeoutClock = new JLabel("5:00.0");
		timeoutCount = new JLabel("4");
		placementPos = new JTextField("0,0");
		place = new JButton("Place Ball");
		final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, BotID.BOT_ID_MAX, 1);
		goalkeeperNumber = new JSpinner(model);
		final JLabel goalieLabel = new JLabel("Goalkeeper");

		timeoutBtn.addActionListener(e -> sendGameControllerEvent(GcEventFactory.commandTimeout(color)));
		direct.addActionListener(e -> sendGameControllerEvent(GcEventFactory.commandDirect(color)));
		penalty.addActionListener(e -> sendGameControllerEvent(GcEventFactory.commandPenalty(color)));
		kickoff.addActionListener(e -> sendGameControllerEvent(GcEventFactory.commandKickoff(color)));
		goal.addActionListener(e -> sendGameControllerEvent(GcEventFactory.goals(color, goals + 1)));

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

			sendGameControllerEvent(GcEventFactory.ballPlacement(pos));
			sendGameControllerEvent(GcEventFactory.commandBallPlacement(color));
		});

		yellow.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.yellowCard(color)));
		red.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.redCard(color)));

		goalkeeperNumber.addChangeListener(
				e -> sendGameControllerEvent(GcEventFactory.goalkeeper(color, (Integer) goalkeeperNumber.getValue())));

		add(timeoutBtn);
		add(goal);
		add(new JLabel("Timeout Clock:"));
		add(timeoutClock);
		add(new JLabel("Timeouts left:"));
		add(timeoutCount);
		add(placementPos);
		add(place);
		add(goalieLabel);
		add(goalkeeperNumber);
		add(yellow);
		add(red);
		add(kickoff);
		add(penalty);
		add(direct);

		setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(color.getColor().darker()), color.name()));
	}


	/**
	 * @param msg
	 */
	public void update(final Referee msg)
	{
		long timeoutTime;
		int timeouts;

		if (color == ETeamColor.YELLOW)
		{
			timeoutTime = msg.getYellow().getTimeoutTime();
			timeouts = msg.getYellow().getTimeouts();
			goals = msg.getYellow().getScore();
		} else
		{
			timeoutTime = msg.getBlue().getTimeoutTime();
			timeouts = msg.getBlue().getTimeouts();
			goals = msg.getBlue().getScore();
		}

		// Timeouts yellow
		long minTo = TimeUnit.MICROSECONDS.toMinutes(timeoutTime);
		long secTo = TimeUnit.MICROSECONDS.toSeconds(timeoutTime) % 60;
		long subTo = (TimeUnit.MICROSECONDS.toMillis(timeoutTime) % 1000) / 100;
		timeoutClock.setText(String.format("%d:%02d:%1d", minTo, secTo, subTo));
		timeoutCount.setText(Integer.toString(timeouts));
	}


	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		timeoutBtn.setEnabled(enable);
		direct.setEnabled(enable);
		penalty.setEnabled(enable);
		yellow.setEnabled(enable);
		red.setEnabled(enable);
		kickoff.setEnabled(enable);
		goal.setEnabled(enable);
		timeoutClock.setEnabled(enable);
		timeoutCount.setEnabled(enable);
		place.setEnabled(enable);
		placementPos.setEnabled(enable);
		goalkeeperNumber.setEnabled(enable);
	}
}
