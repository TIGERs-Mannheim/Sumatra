/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli;

import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for all modules.
 */
public abstract class AModule
{
	private Class<? extends AModule> clazz;
	private SubnodeConfiguration subnodeConfiguration;
	private List<Class<? extends AModule>> dependencies = new ArrayList<>();


	/**
	 * Inits module.
	 */
	public void initModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * DeInits module.
	 */
	public void deinitModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * Starts module.
	 */
	public void startModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * Stops module.
	 */
	public void stopModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * @return the module clazz
	 */
	public Class<? extends AModule> getId()
	{
		return clazz;
	}
	
	
	/**
	 * @param clazz the module clazz
	 */
	public void setId(final Class<? extends AModule> clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the list of dependencies
	 */
	public List<Class<? extends AModule>> getDependencies()
	{
		return dependencies;
	}
	
	
	/**
	 * @param dependencies the new list of dependencies
	 */
	public void setDependencies(final List<Class<? extends AModule>> dependencies)
	{
		this.dependencies = dependencies;
	}
	
	
	/**
	 * @return the subnode configuration
	 */
	public SubnodeConfiguration getSubnodeConfiguration()
	{
		return subnodeConfiguration;
	}
	
	
	void setSubnodeConfiguration(final SubnodeConfiguration subnodeConfiguration)
	{
		this.subnodeConfiguration = subnodeConfiguration;
	}
	
	
	@Override
	public String toString()
	{
		return clazz.getSimpleName();
	}
}
