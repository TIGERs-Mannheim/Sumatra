/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

/**
 * Implement this in an enum that provides {@link InstanceableClass}s
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IInstanceableEnum
{
	/**
	 * @return
	 */
	InstanceableClass getInstanceableClass();
	
	
	/**
	 * @return
	 */
	String name();
}
