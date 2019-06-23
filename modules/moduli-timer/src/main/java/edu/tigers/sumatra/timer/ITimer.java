/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.timer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITimer
{
	
	/**
	 * @param timable
	 * @param id
	 */
	void stop(ETimable timable, long id);
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	void stop(ETimable timable, long id, int customId);
	
	
	/**
	 * @param timable
	 * @param id
	 */
	void start(ETimable timable, long id);
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	void start(ETimable timable, long id, int customId);
}
