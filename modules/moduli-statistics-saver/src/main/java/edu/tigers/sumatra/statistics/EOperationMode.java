package edu.tigers.sumatra.statistics;

/**
 * The operation mode of the statistics saver.
 */
public enum EOperationMode
{
	/**
	 * Do not save anything
	 */
	OFF,
	
	/**
	 * Save to a file in InfluxDB line protocol
	 */
	FILE_LINE_PROTOCOL,
	
	/**
	 * Save to InfluxDB, if sufficient connection information are given. Else fallback to OFF
	 */
	INFLUX_DB,
}
