/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import net.miginfocom.swing.MigLayout;


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
		
		halt.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromHalt()));
		stop.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromStop()));
		forceStart.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromForceStart()));
		normalStart.addActionListener(e -> notifyNewControlRequest(RefBoxRemoteControlFactory.fromNormalStart()));
		
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
		EventQueue.invokeLater(() -> {
			halt.setEnabled(enable);
			stop.setEnabled(enable);
			forceStart.setEnabled(enable);
			normalStart.setEnabled(enable);
		});
	}
}
