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
