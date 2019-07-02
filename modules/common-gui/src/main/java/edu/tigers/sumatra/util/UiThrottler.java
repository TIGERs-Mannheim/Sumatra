package edu.tigers.sumatra.util;

import javax.swing.Timer;


/**
 * Throttle UI updates by executing the update at a fixed rate on the event dispatcher thread.
 * Intermediate updates are lost, but the last update is guaranteed to be executed.
 */
public class UiThrottler
{
	private final Timer timer;

	private Runnable updater;


	/**
	 * Create a new throttler in inactive state.
	 *
	 * @param rateMs the throttling rate in milliseconds
	 */
	public UiThrottler(int rateMs)
	{
		timer = new Timer(rateMs, e -> update());
	}


	/**
	 * Start the timer of the throttler
	 */
	public void start()
	{
		timer.start();
	}


	/**
	 * Stop the timer of the throttler.
	 */
	public void stop()
	{
		timer.stop();
	}


	private void update()
	{
		if (updater != null)
		{
			updater.run();
		}
		updater = null;
	}


	public void execute(Runnable runnable)
	{
		updater = runnable;
	}
}
