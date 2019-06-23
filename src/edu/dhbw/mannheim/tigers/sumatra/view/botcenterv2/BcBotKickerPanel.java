/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerFirePanelV2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotKickerPanel extends JPanel
{
	/**  */
	private static final long			serialVersionUID	= -725531316816891840L;
	private final KickerFirePanelV2	kickerFirePanel	= new KickerFirePanelV2();
	private final KickerConfigPanel	kickerConfigPanel	= new KickerConfigPanel();
	
	
	/**
	 * 
	 */
	public BcBotKickerPanel()
	{
		setLayout(new MigLayout("fillx, wrap 1"));
		add(kickerFirePanel);
		add(kickerConfigPanel);
	}
	
	
	/**
	 * @return the kickerFirePanel
	 */
	public KickerFirePanelV2 getKickerFirePanel()
	{
		return kickerFirePanel;
	}
	
	
	/**
	 * @return the kickerConfigPanel
	 */
	public KickerConfigPanel getKickerConfigPanel()
	{
		return kickerConfigPanel;
	}
}
