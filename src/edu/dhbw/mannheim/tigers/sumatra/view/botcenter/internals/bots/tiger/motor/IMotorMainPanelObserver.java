/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;


/**
 * MotorMainPanel observer interface.
 * 
 * @author AndreR
 * 
 */
public interface IMotorMainPanelObserver
{
	/**
	 * 
	 * @param mode
	 */
	void onSetMotorMode(MotorMode mode);
}
