/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 04.03.2010
 * Authors:
 * Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.moduli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.DependencyException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.LoadModulesException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesStateVariable;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;


/**
 * Main-class of moduli.
 * It contains the handeling of the modules.
 */
public class Moduli
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- global configuration ---
	private SubnodeConfiguration		globalConfiguration;
	
	// --- logger ---
	private static final Logger		log					= Logger.getLogger(Moduli.class.getName());
	
	// --- moduleList ---
	private ArrayList<AModule>			moduleList			= new ArrayList<AModule>();
	
	// --- module-state-variable ---
	private ModulesStateVariable		modulesState		= new ModulesStateVariable();
	
	private static final Class<?>[]	PROP_ARGS_CLASS	= new Class[] { SubnodeConfiguration.class };
	
	
	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Getter modulesState.
	 * 
	 * @return
	 */
	public ModulesStateVariable getModulesState()
	{
		return modulesState;
	}
	
	
	/**
	 * Setter modulesState.
	 * Only to use if you know what you are doing ;).
	 * 
	 * @param modulesState
	 */
	public void setModulesState(final ModulesStateVariable modulesState)
	{
		this.modulesState = modulesState;
	}
	
	
	/**
	 * Getter global configuration
	 * 
	 * @return
	 */
	public SubnodeConfiguration getGlobalConfiguration()
	{
		return globalConfiguration;
	}
	
	
	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Loads all available modules from configuration-file into modulesList.
	 * 
	 * @param xmlFile (module-)configuration-file
	 * @throws LoadModulesException an error occurs... Can't continue.
	 * @throws DependencyException
	 */
	public void loadModules(final String xmlFile) throws LoadModulesException, DependencyException
	{
		// --- clear all moduleList ---
		moduleList.clear();
		
		// --- set state "NOT_LOADED" ---
		modulesState.set(ModulesState.NOT_LOADED);
		
		// --- fill it with new modules ---
		try
		{
			XMLConfiguration config;
			
			config = new XMLConfiguration(xmlFile);
			
			// --- set moduli-folder ---
			String implsPath = config.getString("moduliPath") + ".impls";
			
			// --- set globalConfiguration ---
			globalConfiguration = config.configurationAt("globalConfiguration");
			
			// --- load modules into modulesList ---
			for (int i = 0; i <= config.getMaxIndex("module"); i++)
			{
				
				// --- create implementation- and properties-class ---
				Class<?> clazz = Class.forName(implsPath + "." + config.getString("module(" + i + ").implementation"));
				
				// --- get properties from configuration and put it into a object[] ---
				SubnodeConfiguration moduleConfig = config.configurationAt("module(" + i + ").properties");
				Object[] propArgs = new Object[] { moduleConfig };
				
				// --- get constructor of implementation-class with subnodeConfiguration-parameter ---
				Constructor<?> clazzConstructor = clazz.getConstructor(PROP_ARGS_CLASS);
				
				// --- create object (use constructor) ---
				AModule module = (AModule) createObject(clazzConstructor, propArgs);
				
				// --- set module config ---
				module.setSubnodeConfiguration(moduleConfig);
				
				// --- set id ---
				module.setId(config.getString("module(" + i + ")[@id]"));
				
				// --- set type ---
				module.setType(config.getString("module(" + i + ")[@type]"));
				
				// --- check if module is unique ---
				for (AModule m : moduleList)
				{
					if (m.getId().equals(module.getId()))
					{
						throw new LoadModulesException("module-id '" + module.getId() + "' isn't unique.");
					}
				}
				
				// --- set dependency-list ---
				List<String> depList = Arrays.asList(config.getStringArray("module(" + i + ").dependency"));
				module.setDependencies(depList);
				
				
				// --- put module into moduleList ---
				moduleList.add(module);
				
				log.trace("Module created: " + module);
			}
			
		} catch (ConfigurationException e)
		{
			throw new LoadModulesException("Configuration contains errors: " + e.getMessage(), e);
		} catch (ClassNotFoundException e)
		{
			throw new LoadModulesException("Class in configuration can't be found: " + e.getMessage(), e);
		} catch (SecurityException e)
		{
			throw new LoadModulesException("Security issue at configuration : " + e.getMessage(), e);
		} catch (NoSuchMethodException e)
		{
			throw new LoadModulesException(
					"Can't find a constructor <init>(SubnodeConfiguration) of this class. Please add one. : "
							+ e.getMessage(), e);
		} catch (IllegalArgumentException e)
		{
			throw new LoadModulesException("An argument isn't valid : " + e.getMessage(), e);
		}
		
		// --- check dependencies ---
		checkDependencies();
		
		// --- set state "RESOLVED" ---
		modulesState.set(ModulesState.RESOLVED);
	}
	
	
	/**
	 * Load modules and catch exceptions
	 * 
	 * @param filename
	 */
	public void loadModulesSafe(final String filename)
	{
		try
		{
			// --- get modules from configuration-file ---
			SumatraModel.getInstance().loadModules(
					SumatraModel.MODULI_CONFIG_PATH + filename);
			log.debug("Loaded config: " + filename);
		} catch (final LoadModulesException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + filename
					+ "') ", e);
		} catch (final DependencyException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + filename
					+ "') ", e);
		}
	}
	
	
	/**
	 * Starts all modules in modulesList.
	 * 
	 * @throws InitModuleException
	 * @throws StartModuleException
	 */
	public void startModules() throws InitModuleException, StartModuleException
	{
		
		// --- init modules ---
		for (AModule m : moduleList)
		{
			try
			{
				log.trace("Initializing module " + m);
				m.initModule();
				log.trace("Module " + m + " initialized");
			} catch (Exception err)
			{
				throw new InitModuleException("Could not initialize module " + m, err);
			}
		}
		
		// --- start modules ---
		for (AModule m : moduleList)
		{
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				log.trace("Starting module " + m);
				m.startModule();
				log.trace("Module " + m + " started");
			} catch (Exception err)
			{
				throw new StartModuleException("Could not initialize module " + m, err);
			}
		}
		
		// --- set state "RESOLVED" ---
		modulesState.set(ModulesState.ACTIVE);
	}
	
	
	/**
	 * Stops all modules in modulesList.
	 */
	public void stopModules()
	{
		// --- stop modules ---
		for (AModule m : moduleList)
		{
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				m.stopModule();
				log.trace("Module " + m + " stopped");
			} catch (Exception err)
			{
				log.error("Exception while stopping module: " + m, err);
			}
		}
		
		// --- deinit modules ---
		for (AModule m : moduleList)
		{
			try
			{
				m.deinitModule();
				log.trace("Module " + m + " deinitialized");
			} catch (Exception err)
			{
				log.error("Exception while deinitializing module: " + m, err);
			}
		}
		
		// --- set state "RESOLVED" ---
		modulesState.set(ModulesState.RESOLVED);
	}
	
	
	/**
	 * Returns a list with all loaded modules.
	 * 
	 * @return
	 */
	public List<AModule> getModules()
	{
		return moduleList;
	}
	
	
	/**
	 * Gets a module from current module-list.
	 * 
	 * @param moduleId module-id-string
	 * @return
	 * @throws ModuleNotFoundException
	 */
	public AModule getModule(final String moduleId) throws ModuleNotFoundException
	{
		// --- search for the module ---
		for (AModule m : moduleList)
		{
			if (m.getId().equals(moduleId))
			{
				return m;
			}
		}
		
		// --- if nothing was found, throw a ModuleNotFoundException ---
		throw new ModuleNotFoundException("Module " + moduleId + " not found");
	}
	
	
	// --------------------------------------------------------------------------
	// --- private-method(s) ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Checks, if dependencies can be resolved.
	 * 
	 * @throws DependencyException ... if at least one modules can't be resolved
	 */
	private void checkDependencies() throws DependencyException
	{
		
		// --- variable which indicates if dependencies are okay ---
		boolean depOk = false;
		
		// --- check if all dependencies can be resolved ---
		for (AModule m : moduleList)
		{
			
			for (String dependency : m.getDependencies())
			{
				// --- reset depOk ---
				depOk = false;
				
				for (AModule n : moduleList)
				{
					if (n.getId().equals(dependency))
					{
						// --- dep is okay ---
						depOk = true;
						break;
					}
				}
				
				// --- check if one dependencies isn't met ---
				if (!depOk)
				{
					throw new DependencyException("Dependency '" + dependency + "' isn't met at module '" + m.getId() + "'");
				}
				
			}
			
		}
	}
	
	
	/**
	 * Creates an object from a constructor and its arguments.
	 */
	private Object createObject(final Constructor<?> constructor, final Object[] arguments)
	{
		Object object = null;
		
		try
		{
			object = constructor.newInstance(arguments);
			return object;
		} catch (InstantiationException e)
		{
			log.error(e.getMessage(), e);
		} catch (IllegalAccessException e)
		{
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException e)
		{
			log.error(e.getMessage(), e);
		} catch (InvocationTargetException e)
		{
			log.error(e.getMessage(), e);
		}
		return object;
	}
}
