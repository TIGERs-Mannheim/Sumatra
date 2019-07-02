/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;


/**
 * @author AndreR <andre@ryll.cc>
 */
public interface IBotMovementLimits
{
	
	/**
	 * @return the velMax
	 */
	double getVelMax();
	
	
	/**
	 * @return the accMax
	 */
	double getAccMax();
	
	
	/**
	 * @return the brkMax
	 */
	double getBrkMax();
	
	
	/**
	 * @return the jerkMax
	 */
	double getJerkMax();
	
	
	/**
	 * @return the velMaxW
	 */
	double getVelMaxW();
	
	
	/**
	 * @return the accMaxW
	 */
	double getAccMaxW();
	
	
	/**
	 * @return the jerkMaxW
	 */
	double getJerkMaxW();
	
	
	/**
	 * @return the velMaxFast
	 */
	double getVelMaxFast();
	
	
	/**
	 * @return the accMaxFast
	 */
	double getAccMaxFast();
}