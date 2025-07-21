/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;

/**
 * Observer for config clients.
 */
public interface IConfigClientsObserver
{
	void onNewConfigClient(String newClient);
}
