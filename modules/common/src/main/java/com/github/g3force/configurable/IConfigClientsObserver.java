package com.github.g3force.configurable;

/**
 * Observer for config clients.
 */
public interface IConfigClientsObserver
{
	void onNewConfigClient(String newClient);
}
