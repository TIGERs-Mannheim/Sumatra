/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.KickerFirePanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotKickerPanel extends JPanel
{
	/**  */
	private static final long		serialVersionUID	= -725531316816891840L;
	private final KickerFirePanel	kickerFirePanel	= new KickerFirePanel();
	
	
	/**
	 * 
	 */
	public BcBotKickerPanel()
	{
		setLayout(new MigLayout("fillx, wrap 1"));
		add(kickerFirePanel);
	}
	
	
	/**
	 * @return the kickerFirePanel
	 */
	public KickerFirePanel getKickerFirePanel()
	{
		return kickerFirePanel;
	}
}
