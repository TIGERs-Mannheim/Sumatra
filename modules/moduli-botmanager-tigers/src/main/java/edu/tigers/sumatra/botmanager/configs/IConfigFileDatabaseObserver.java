/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.configs;

public interface IConfigFileDatabaseObserver
{
	void onConfigFileAdded(final ConfigFile file);

	void onConfigFileRemoved(final int configId, final int version);
}
