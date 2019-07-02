/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import edu.tigers.sumatra.referee.control.GcEventFactory;
import net.miginfocom.swing.MigLayout;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class ChangeStatePanel extends ARefBoxRemoteControlGeneratorPanel
{
	private static final long serialVersionUID = -423240395071869217L;
	
	private final JButton previousStage;
	private final JButton nextStage;
	private final JButton endGame;
	
	
	/** Constructor */
	public ChangeStatePanel()
	{
		setLayout(new MigLayout("wrap 1", "[fill]10[fill]"));
		
		previousStage = new JButton("Previous Stage");
		nextStage = new JButton("Next Stage");
		endGame = new JButton("End Game");
		
		previousStage.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.previousStage()));
		nextStage.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.nextStage()));
		endGame.addActionListener(
				e -> sendGameControllerEvent(GcEventFactory.endGame()));
		
		add(previousStage);
		add(nextStage);
		add(endGame);
		
		setBorder(BorderFactory.createTitledBorder("Change State"));
	}
	
	
	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		EventQueue.invokeLater(() -> {
			previousStage.setEnabled(enable);
			nextStage.setEnabled(enable);
			endGame.setEnabled(enable);
		});
	}
}
