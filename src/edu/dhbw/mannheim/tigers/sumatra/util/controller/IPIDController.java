/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.controller;

/**
 * Interface for standard PID controller usage.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public interface IPIDController
{
	/**
	 * 
	 * Calculates the output value for next iteration.
	 * 
	 * @param actualValue
	 * @param setPoint
	 * @param deltaT
	 * @return controlled output
	 */
	public float process(float actualValue, float setPoint, double deltaT);
	
	
	/**
	 * @return The current state of the PID-controller
	 */
	public PidState getState();
	
	
	/**
	 * @param newState The new state of the PID-controller
	 */
	public void setState(PidState newState);
}
