/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.configs;

import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigWrite;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration file.
 */
@Log4j2
public final class ConfigFile
{
	private final TigerConfigFileStructure structure;
	private final List<String> names = new ArrayList<>();
	private List<String> values = new ArrayList<>();
	private String name = null;


	/**
	 * @param structure
	 */
	public ConfigFile(final TigerConfigFileStructure structure)
	{
		this.structure = structure;
	}


	/**
	 * Constructor
	 *
	 * @param structure
	 * @param name
	 * @param names
	 * @param values
	 */
	public ConfigFile(final TigerConfigFileStructure structure, String name, List<String> names, List<String> values)
	{
		this.structure = structure;
		this.name = name;
		this.names.addAll(names);
		this.values = values;
	}


	/**
	 * Check if all item names have been retrieved.
	 *
	 * @return
	 */
	public boolean isComplete()
	{
		return structure.getElements().size() == names.size();
	}


	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * @return
	 */
	public int getConfigId()
	{
		return structure.getConfigId();
	}


	/**
	 * @return
	 */
	public int getVersion()
	{
		return structure.getVersion();
	}


	/**
	 * @return
	 */
	public List<TigerConfigFileStructure.EElementType> getElements()
	{
		return structure.getElements();
	}


	/**
	 * Get next item description request to complete this file.
	 *
	 * @return
	 */
	public TigerConfigItemDesc getNextRequest()
	{
		if (name == null)
		{
			log.debug("Requesting file name for config id {}", structure.getConfigId());
			return new TigerConfigItemDesc(structure.getConfigId(), TigerConfigItemDesc.CONFIG_ITEM_FILE_NAME);
		}

		if (isComplete())
		{
			throw new IllegalStateException("Config is already complete!");
		}

		log.debug("Requesting description of element {} for config id {}", names.size(), structure.getConfigId());
		return new TigerConfigItemDesc(structure.getConfigId(), names.size());
	}


	/**
	 * Add an item description to this file.
	 *
	 * @param desc
	 */
	public void setItemDesc(final TigerConfigItemDesc desc)
	{
		if (desc.getElement() == TigerConfigItemDesc.CONFIG_ITEM_FILE_NAME)
		{
			name = desc.getName();
			return;
		}

		if (names.size() != desc.getElement())
		{
			log.warn("Element mismatch: {}({}) {}!={}", names, desc.getName(), names.size(), desc.getElement());
			return;
		}

		names.add(desc.getName());
	}


	/**
	 * Set config values.
	 *
	 * @param read
	 */
	public void setValues(final TigerConfigRead read)
	{
		values = read.getData(structure);
	}


	/**
	 * Retrieve write command from current values.
	 *
	 * @return
	 */
	public TigerConfigWrite getWriteCmd()
	{
		TigerConfigWrite write = new TigerConfigWrite(structure.getConfigId());
		write.setData(structure, values);

		return write;
	}

	/**
	 * @return the names
	 */
	public List<String> getNames()
	{
		return names;
	}


	/**
	 * @return the values
	 */
	public List<String> getValues()
	{
		return values;
	}
}
