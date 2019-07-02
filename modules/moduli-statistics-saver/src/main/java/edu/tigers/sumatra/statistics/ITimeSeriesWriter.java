package edu.tigers.sumatra.statistics;

/**
 * Time series writer interface
 */
public interface ITimeSeriesWriter
{
	/**
	 * Add a new entry
	 * 
	 * @param entry the entry
	 */
	void add(TimeSeriesStatsEntry entry);
	
	
	/**
	 * Start the writer
	 */
	void start();
	
	
	/**
	 * Stop the writer
	 */
	void stop();
}
