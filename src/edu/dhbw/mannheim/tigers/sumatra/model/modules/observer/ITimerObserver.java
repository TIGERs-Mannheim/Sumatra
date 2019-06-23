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

import edu.dhbw.mannheim.tigers.sumatra.model.data.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;

/**
 * This interface receives {@link TimerInfo} from {@link ATimer}-implementations
 * 
 * @author Gero
 * 
 */
public interface ITimerObserver
{
	public void onNewTimerInfo(TimerInfo info);
}
