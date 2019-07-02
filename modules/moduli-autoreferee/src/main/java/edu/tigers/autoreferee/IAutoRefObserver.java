/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.IAutoRefEngineObserver;


public interface IAutoRefObserver extends IAutoRefEngineObserver
{
	default void onAutoRefModeChanged(EAutoRefMode mode)
	{
	}
}
