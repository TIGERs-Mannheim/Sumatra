/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.configs;

import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import lombok.Getter;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class ConfigFileDatabase implements IConfigFileDatabase
{
	@Getter
	private final Map<Integer, Map<Integer, Map<String, Object>>> savedConfigFiles = new HashMap<>();
	@Getter
	private final Map<Integer, Set<Integer>> autoUpdateConfigs = new HashMap<>();


	void addEntry(final ConfigFile file)
	{
		Map<String, Object> configData = new HashMap<>();
		configData.put("name", file.getName());
		configData.put("names", file.getNames());
		configData.put("values", file.getValues());
		configData.put("elements",
				file.getElements().stream().map(TigerConfigFileStructure.EElementType::getTypeId).toList());

		Map<Integer, Map<String, Object>> versionedConfigs = savedConfigFiles.getOrDefault(file.getConfigId(),
				new HashMap<>());
		versionedConfigs.put(file.getVersion(), configData);
		savedConfigFiles.put(file.getConfigId(), versionedConfigs);
	}


	void deleteEntry(final int configId, final int version)
	{
		Validate.isTrue(savedConfigFiles.containsKey(configId));
		Map<Integer, Map<String, Object>> versionedConfigs = savedConfigFiles.get(configId);

		Validate.isTrue(versionedConfigs.containsKey(version));
		versionedConfigs.remove(version);

		if (versionedConfigs.isEmpty())
		{
			savedConfigFiles.remove(configId);
		}

		noUpdateFor(configId, version);
	}


	@Override
	@SuppressWarnings("unchecked")
	public Optional<ConfigFile> getSelectedEntry(final int configId, final int version)
	{
		if (!savedConfigFiles.containsKey(configId) || !savedConfigFiles.get(configId).containsKey(version))
		{
			return Optional.empty();
		}
		Map<String, Object> configData = savedConfigFiles.get(configId).get(version);
		List<Integer> elements = (List<Integer>) configData.get("elements");
		byte[] e = new byte[elements.size()];
		for (int i = 0; i < elements.size(); i++)
		{
			e[i] = elements.get(i).byteValue();
		}
		TigerConfigFileStructure structure = new TigerConfigFileStructure(configId, version, e);
		return Optional.of(new ConfigFile(structure,
				(String) configData.get("name"),
				(List<String>) configData.get("names"),
				(List<String>) configData.get("values")));
	}


	private void noUpdateFor(int configId, int version)
	{
		if (autoUpdateConfigs.containsKey(configId) && autoUpdateConfigs.get(configId).contains(version))
		{
			autoUpdateConfigs.get(configId).remove(version);
			if (autoUpdateConfigs.get(configId).isEmpty())
			{
				autoUpdateConfigs.remove(configId);
			}
		}
	}


	void setAutoUpdateFor(int configId, int version, boolean update)
	{
		if (update)
		{
			Set<Integer> updatedVersions = autoUpdateConfigs.getOrDefault(configId, new HashSet<>());
			updatedVersions.add(version);
			autoUpdateConfigs.put(configId, updatedVersions);
		} else
		{
			noUpdateFor(configId, version);
		}
	}


	@Override
	public boolean isAutoUpdate(final int configId, final int version)
	{
		return autoUpdateConfigs.containsKey(configId) && autoUpdateConfigs.get(configId).contains(version);
	}
}
