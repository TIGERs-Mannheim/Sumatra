package edu.tigers.sumatra.botmanager.ping;

/**
 * Ping statistics.
 */
@SuppressWarnings("squid:ClassVariableVisibilityCheck")
public class PingStats
{
	/** Average Delay */
	public double	avgDelay;
	/** Minimum Delay */
	public double	minDelay;
	/** Maximum Delay */
	public double	maxDelay;
	/** Lost pings per second */
	public int		lostPings;
	
	
	/**
	 * Default constrcutor.
	 */
	public PingStats()
	{
		avgDelay = 0;
		minDelay = Double.MAX_VALUE;
		maxDelay = 0;
		lostPings = 0;
	}
}
