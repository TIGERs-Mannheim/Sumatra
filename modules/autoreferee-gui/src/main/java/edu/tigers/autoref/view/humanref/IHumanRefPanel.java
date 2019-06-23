/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref;

import edu.tigers.autoref.view.humanref.driver.IHumanRefViewDriver;


/**
 * @author "Lukas Magel"
 */
public interface IHumanRefPanel
{
	/**
	 * @author "Lukas Magel"
	 */
	enum EPanelType
	{
		BASE,
		ACTIVE,
		PASSIVE
	}
	
	
	/**
	 * @return
	 */
	IHumanRefViewDriver getDriver();
	
	
	/**
	 * @param type
	 */
	void setPanelType(EPanelType type);
	
	
	/**
	 * @return
	 */
	EPanelType getPanelType();
}
