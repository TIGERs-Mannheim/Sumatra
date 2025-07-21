/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.referee.view;

import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class ChangeStatePanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -423240395071869217L;

	private final JComboBox<SslGcRefereeMessage.Referee.Stage> stages;
	private final JButton changeStage;
	private final JButton endGame;


	public ChangeStatePanel()
	{
		setLayout(new MigLayout("wrap 1", "[fill]10[fill]"));

		stages = new JComboBox<>(SslGcRefereeMessage.Referee.Stage.values());
		changeStage = new JButton("Change stage");
		endGame = new JButton("End Game");

		changeStage.addActionListener(
				e -> sendGameControllerEvent(
						GcEventFactory.stage((SslGcRefereeMessage.Referee.Stage) stages.getSelectedItem())));
		endGame.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.endGame()));

		add(stages);
		add(changeStage);
		add(endGame);

		setBorder(BorderFactory.createTitledBorder("Change State"));
	}


	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		stages.setEnabled(enable);
		changeStage.setEnabled(enable);
		endGame.setEnabled(enable);
	}
}
