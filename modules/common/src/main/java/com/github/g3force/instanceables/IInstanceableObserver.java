/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

/**
 * Observer interface to get notified about new instances
 */
public interface IInstanceableObserver
{
	/**
	 * @param object the object that was created
	 */
	void onNewInstance(Object object);
}
