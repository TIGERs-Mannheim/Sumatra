package edu.tigers.sumatra.botmanager.configs;

import java.util.Optional;


public interface IConfigFileDatabase
{
	Optional<ConfigFile> getSelectedEntry(int configId, int version);

	boolean isAutoUpdate(int configId, int version);
}
