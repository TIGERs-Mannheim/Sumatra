/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.08.2010
 * Author(s): Gero
 * *********************************************************
 */
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
