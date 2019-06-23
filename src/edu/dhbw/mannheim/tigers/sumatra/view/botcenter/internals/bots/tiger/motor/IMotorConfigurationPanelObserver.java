/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

/**
 * Interface of MotorConfigurationPanel
 * 
 * @author AndreR
 * 
 */
public interface IMotorConfigurationPanelObserver
{
	public void onSetLog(boolean logging);
	public void onSetPidParams(float kp, float ki, float kd, int slew);
	public void onSetManual(int power);
	public void onSetPidSetpoint(int setpoint);
}
