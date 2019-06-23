/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import net.miginfocom.swing.MigLayout;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class ChangeStatePanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -423240395071869217L;
	
	private final JButton firstHalf;
	private final JButton halfTime;
	private final JButton secondHalf;
	private final JButton overtime1;
	private final JButton overtime2;
	private final JButton penaltyShootout;
	private final JButton endGame;
	
	
	/** Constructor */
	public ChangeStatePanel()
	{
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));
		
		firstHalf = new JButton("First Half");
		halfTime = new JButton("Half Time");
		secondHalf = new JButton("Second Half");
		overtime1 = new JButton("Overtime1");
		overtime2 = new JButton("Overtime2");
		penaltyShootout = new JButton("Penalty Shootout");
		endGame = new JButton("End Game");
		
		firstHalf.addActionListener(
				e -> sendStage(Stage.NORMAL_FIRST_HALF_PRE));
		halfTime.addActionListener(
				e -> sendStage(Stage.NORMAL_HALF_TIME));
		secondHalf.addActionListener(
				e -> sendStage(Stage.NORMAL_SECOND_HALF_PRE));
		overtime1.addActionListener(
				e -> sendStage(Stage.EXTRA_FIRST_HALF_PRE));
		overtime2.addActionListener(
				e -> sendStage(Stage.EXTRA_SECOND_HALF_PRE));
		penaltyShootout.addActionListener(
				e -> sendStage(Stage.PENALTY_SHOOTOUT));
		endGame.addActionListener(
				e -> sendStage(Stage.POST_GAME));
		
		add(firstHalf);
		add(halfTime);
		add(secondHalf);
		add(overtime1);
		add(overtime2);
		add(penaltyShootout);
		add(endGame);
		
		setBorder(BorderFactory.createTitledBorder("Change State"));
	}
	
	
	private void sendStage(Stage stage)
	{
		notifyNewControlRequest(RefBoxRemoteControlFactory.fromStage(stage));
		notifyNewControlRequest(RefBoxRemoteControlFactory.fromCommand(Referee.SSL_Referee.Command.HALT));
	}
	
	
	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		EventQueue.invokeLater(() -> {
			firstHalf.setEnabled(enable);
			halfTime.setEnabled(enable);
			secondHalf.setEnabled(enable);
			overtime1.setEnabled(enable);
			overtime2.setEnabled(enable);
			penaltyShootout.setEnabled(enable);
			endGame.setEnabled(enable);
		});
	}
}
