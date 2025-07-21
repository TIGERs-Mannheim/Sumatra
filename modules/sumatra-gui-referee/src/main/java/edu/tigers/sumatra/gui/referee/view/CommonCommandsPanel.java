/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.referee.view;

import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class CommonCommandsPanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -1270833222588447522L;

	private final JButton halt;
	private final JButton stop;
	private final JButton forceStart;
	private final JButton normalStart;


	/** Constructor */
	public CommonCommandsPanel()
	{
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));

		halt = new JButton("Halt");
		stop = new JButton("Stop");
		forceStart = new JButton("Force Start");
		normalStart = new JButton("Normal Start");

		halt.addActionListener(e -> sendGameControllerEvent(GcEventFactory.command(SslGcRefereeMessage.Referee.Command.HALT)));
		stop.addActionListener(e -> sendGameControllerEvent(GcEventFactory.command(SslGcRefereeMessage.Referee.Command.STOP)));
		forceStart.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.command(SslGcRefereeMessage.Referee.Command.FORCE_START)));
		normalStart.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.command(SslGcRefereeMessage.Referee.Command.NORMAL_START)));

		add(halt);
		add(stop);
		add(forceStart);
		add(normalStart);

		setBorder(BorderFactory.createTitledBorder("Common Commands"));
	}


	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		halt.setEnabled(enable);
		stop.setEnabled(enable);
		forceStart.setEnabled(enable);
		normalStart.setEnabled(enable);
	}
}
