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
	/**
	 * 
	 * @param logging
	 */
	void onSetLog(boolean logging);
	
	
	/**
	 * 
	 * @param kp
	 * @param ki
	 * @param kd
	 * @param slew
	 */
	void onSetPidParams(float kp, float ki, float kd, int slew);
	
	
	/**
	 * 
	 * @param power
	 */
	void onSetManual(int power);
	
	
	/**
	 * 
	 * @param setpoint
	 */
	void onSetPidSetpoint(int setpoint);
}
