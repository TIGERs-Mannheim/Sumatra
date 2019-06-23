/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.replay;

import edu.dhbw.mannheim.tigers.sumatra.view.main.AMainFrame;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayWindow extends AMainFrame
{
	private static final long	serialVersionUID	= 4040295061416588239L;
	
	
	/**
	 */
	public ReplayWindow()
	{
		setTitle("Replay");
		showFrame();
	}
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 */
	public void activate()
	{
		setVisible(true);
		requestFocus();
	}
}
