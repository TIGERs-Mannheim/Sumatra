package edu.tigers.sumatra.botmanager.configs;

public interface IConfigFileDatabaseObserver
{
	void onConfigFileAdded(final ConfigFile file);

	void onConfigFileRemoved(final int configId, final int version);
}
