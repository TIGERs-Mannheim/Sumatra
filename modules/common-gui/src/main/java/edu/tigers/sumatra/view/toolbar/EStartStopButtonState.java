/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view.toolbar;

import javax.swing.ImageIcon;

import edu.tigers.sumatra.util.ImageScaler;


/**
 * start stop button states and their images
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EStartStopButtonState
{
	/**  */
	START("/start.png"),
	/**  */
	STOP("/stop.png"),
	/**  */
	LOADING("/LoadingTrans.gif");
	
	private final ImageIcon	icon;
	
	
	private EStartStopButtonState(final String path)
	{
		icon = ImageScaler.scaleDefaultButtonImageIcon(path);
	}
	
	
	/**
	 * @return the icon
	 */
	public final ImageIcon getIcon()
	{
		return icon;
	}
}
