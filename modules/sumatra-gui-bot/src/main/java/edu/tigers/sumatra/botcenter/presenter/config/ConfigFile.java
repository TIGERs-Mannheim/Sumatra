/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.presenter.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigWrite;


/**
 * Configuration file.
 */
public final class ConfigFile
{
	private static final Logger log = LogManager.getLogger(ConfigPresenter.class.getName());

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
			log.warn("Element mismatch: " + names + "(" + desc.getName() + ") " + names.size() + "!="
					+ desc.getElement());
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
