/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;


/**
 * This interface receives {@link TimerInfo} from {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer}
 * -implementations
 * 
 * @author Gero
 * 
 */
public interface ITimerObserver
{
	/**
	 * 
	 * @param info
	 */
	void onNewTimerInfo(TimerInfo info);
}
