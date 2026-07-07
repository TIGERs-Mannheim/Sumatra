package edu.tigers.autoreferee;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.IAutoRefEngineObserver;


public interface IAutoRefObserver extends IAutoRefEngineObserver
{
	default void onAutoRefModeChanged(EAutoRefMode mode)
	{
	}
}
