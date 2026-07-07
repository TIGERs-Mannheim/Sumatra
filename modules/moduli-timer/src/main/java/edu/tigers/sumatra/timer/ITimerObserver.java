package edu.tigers.sumatra.timer;

/**
 * This interface receives {@link TimerInfo} from {@link edu.tigers.sumatra.timer.ATimer}
 * -implementations
 * 
 * @author Gero
 */
public interface ITimerObserver
{
	/**
	 * @param info
	 */
	void onNewTimerInfo(TimerInfo info);
}
