package edu.tigers.autoreferee.engine;

public enum EAutoRefMode
{
	OFF,
	ACTIVE,
	PASSIVE;
	
	public EAutoRefMode next()
	{
		if (this == EAutoRefMode.OFF)
		{
			return ACTIVE;
		} else if (this == EAutoRefMode.ACTIVE)
		{
			return PASSIVE;
		} else if (this == EAutoRefMode.PASSIVE)
		{
			return OFF;
		}
		return null;
	}
}
