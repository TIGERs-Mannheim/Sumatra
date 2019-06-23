/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.10.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;

/**
 * Observer interface for the enhanced control input panel
 * 
 * @author AndreR
 * 
 */
public interface IMotorEnhancedInputPanel
{
	public void onNewVelocity(Vector2 xy);
	public void onNewAngularVelocity(float w);
}
