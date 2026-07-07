package edu.tigers.sumatra.persistence;

/**
 * An interface for all persistence stores
 */
public interface IPersistenceRecorder
{
	/**
	 * Start recorder
	 */
	void start();
	
	
	/**
	 * Stop recorder
	 */
	void stop();
	
	
	/**
	 * Flush all buffered data
	 */
	void flush();
}
