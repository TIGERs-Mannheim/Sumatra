/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli;

import edu.tigers.sumatra.moduli.exceptions.DependencyException;
import edu.tigers.sumatra.moduli.exceptions.InitModuleException;
import edu.tigers.sumatra.moduli.exceptions.LoadModulesException;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.observer.StateDistributor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Main-class of moduli.
 * It contains the handling of the modules.
 */
@Log4j2
public class Moduli
{
	private final Map<Class<? extends AModule>, AModule> modules = new HashMap<>();
	private final List<AModule> orderedModules = new LinkedList<>();
	@Getter
	private final StateDistributor<ModulesState> modulesState = new StateDistributor<>(ModulesState.NOT_LOADED);
	@Getter
	private SubnodeConfiguration globalConfiguration;
	private XMLConfiguration config;


	/**
	 * Loads all available modules from configuration-file into modulesList.
	 *
	 * @param xmlFile (module-)configuration-file
	 * @throws LoadModulesException an error occurs... Can't continue.
	 * @throws DependencyException  on dependency issues
	 */
	protected void loadModules(final String xmlFile) throws LoadModulesException, DependencyException
	{
		modules.clear();
		orderedModules.clear();

		loadModulesFromFile(xmlFile);

		Graph<AModule, DefaultEdge> dependencyGraph = buildDependencyGraph();
		new TopologicalOrderIterator<>(dependencyGraph).forEachRemaining(orderedModules::addFirst);

		modulesState.set(ModulesState.RESOLVED);
	}


	private void loadModulesFromFile(String xmlFile) throws LoadModulesException
	{
		try
		{
			config = new XMLConfiguration(xmlFile);

			setGlobalConfiguration();

			constructModules();

		} catch (ConfigurationException e)
		{
			throw new LoadModulesException("Configuration contains errors", e);
		} catch (ClassNotFoundException e)
		{
			throw new LoadModulesException("Class in configuration can't be found", e);
		} catch (ClassCastException e)
		{
			throw new LoadModulesException("Given implementation is not an instance of AModule", e);
		} catch (SecurityException e)
		{
			throw new LoadModulesException("Security issue at configuration", e);
		} catch (IllegalArgumentException e)
		{
			throw new LoadModulesException("An argument isn't valid", e);
		}
	}


	private void setGlobalConfiguration()
	{
		globalConfiguration = getModuleConfig("globalConfiguration");
	}


	@SuppressWarnings("unchecked")
	private void constructModules() throws ClassNotFoundException, LoadModulesException
	{
		for (int i = 0; i <= config.getMaxIndex("module"); i++)
		{
			Class<? extends AModule> id = (Class<? extends AModule>) Class
					.forName(config.getString(moduleMessage(i, "[@id]")));

			Class<? extends AModule> clazz = getImplementation(i, id);

			SubnodeConfiguration moduleConfig = getModuleConfig(moduleMessage(i, ".properties"));

			AModule module = (AModule) createObject(clazz);

			module.setSubnodeConfiguration(moduleConfig);

			module.setId(id);

			checkModuleIsUnique(module);

			// --- set dependency-list ---
			String[] rawDependencyList = config.getStringArray(moduleMessage(i, ".dependency"));
			List<Class<? extends AModule>> dependencyList = new ArrayList<>();
			for (String dependency : rawDependencyList)
			{
				dependencyList.add((Class<? extends AModule>) Class.forName(dependency));
			}
			module.setDependencies(dependencyList);


			modules.put(id, module);

			log.trace("Created module {}", module);
		}
	}


	@SuppressWarnings("unchecked")
	private Class<? extends AModule> getImplementation(final int i, final Class<? extends AModule> id)
			throws ClassNotFoundException
	{
		final String implementationKey = moduleMessage(i, ".implementation");
		if (config.containsKey(implementationKey))
		{
			return (Class<? extends AModule>) Class.forName(config.getString(implementationKey));
		}
		return id;
	}


	private SubnodeConfiguration getModuleConfig(final String key)
	{
		try
		{
			return config.configurationAt(key);
		} catch (IllegalArgumentException e)
		{
			return new SubnodeConfiguration(new HierarchicalConfiguration(), new DefaultConfigurationNode());
		}
	}


	private void checkModuleIsUnique(final AModule module) throws LoadModulesException
	{
		if (modules.containsKey(module.getId()))
		{
			throw new LoadModulesException("module-id '" + module.getId() + "' isn't unique.");
		}
	}


	/**
	 * Load modules and catch exceptions
	 *
	 * @param filename of the moduli config
	 */
	protected void loadModulesSafe(final String filename)
	{
		try
		{
			loadModules(filename);
			log.debug("Loaded config: {}", filename);
		} catch (final LoadModulesException | DependencyException e)
		{
			log.error("Failed to load modules from {}", filename, e);
		}
	}


	/**
	 * Starts all modules in modulesList.
	 */
	public synchronized void startModules()
	{
		if (modulesState.get() == ModulesState.ACTIVE)
		{
			return;
		}

		initModules(orderedModules);
		startUpModules(orderedModules);

		modulesState.set(ModulesState.ACTIVE);
	}


	private Graph<AModule, DefaultEdge> buildDependencyGraph() throws DependencyException
	{
		try
		{
			DirectedAcyclicGraph<AModule, DefaultEdge> dependencyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
			for (AModule module : modules.values())
			{
				dependencyGraph.addVertex(module);
				for (Class<? extends AModule> dependencyId : module.getDependencies())
				{
					AModule dependency = modules.get(dependencyId);
					if (dependency == null)
					{
						throw new DependencyException(
								"Dependency " + dependencyId + " is required by " + module + ", but not started.");
					}
					dependencyGraph.addVertex(dependency);
					dependencyGraph.addEdge(module, dependency);
				}
			}
			return dependencyGraph;
		} catch (IllegalArgumentException e)
		{
			throw new DependencyException("Cycle in dependencies: ", e);
		}
	}


	private void initModules(List<AModule> orderedModules)
	{
		for (AModule m : orderedModules)
		{
			try
			{
				log.trace("Initializing module {}", m);
				m.initModule();
				log.trace("Initialized module {}", m);
			} catch (Exception err)
			{
				throw new InitModuleException("Could not initialize module " + m, err);
			}
		}
	}


	private void startUpModules(List<AModule> orderedModules)
	{
		for (AModule m : orderedModules)
		{
			try
			{
				log.trace("Starting module {}", m);
				m.startModule();
				log.trace("Started module {}", m);
			} catch (Exception err)
			{
				throw new StartModuleException("Could not initialize module " + m, err);
			}
		}
	}


	/**
	 * Stops all modules in modulesList.
	 */
	public synchronized void stopModules()
	{
		if (modulesState.get() != ModulesState.ACTIVE)
		{
			return;
		}

		List<AModule> reversedModules = new ArrayList<>(orderedModules);
		Collections.reverse(reversedModules);

		internalStopModules(reversedModules);

		deinitModules(reversedModules);

		modulesState.set(ModulesState.RESOLVED);
	}


	private void internalStopModules(final List<AModule> reversedModules)
	{
		for (AModule m : reversedModules)
		{
			try
			{
				log.trace("Stopping module {}", m);
				m.stopModule();
				log.trace("Stopped module {}", m);
			} catch (Exception err)
			{
				log.error("Exception while stopping module {}", m, err);
			}
		}
	}


	private void deinitModules(final List<AModule> reversedModules)
	{
		for (AModule m : reversedModules)
		{
			try
			{
				log.trace("Uninitializing module {}", m);
				m.deinitModule();
				log.trace("Uninitialized module {}", m);
			} catch (Exception err)
			{
				log.error("Exception while uninitializing module {}", m, err);
			}
		}
	}


	/**
	 * Gets a module from current module-list.
	 *
	 * @param moduleId the type of the model
	 * @param <T>      the type of the module
	 * @return the instance of the module for the id
	 * @throws ModuleNotFoundException if the module couldn't be found
	 */
	public <T extends AModule> T getModule(Class<T> moduleId)
	{
		return getModuleOpt(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException("Module " + moduleId + " not found"));
	}


	/**
	 * Gets a module from current module-list.
	 *
	 * @param moduleId the type of the model
	 * @param <T>      the type of the module
	 * @return the instance of the module for the id
	 */
	@SuppressWarnings("unchecked")
	public <T extends AModule> Optional<T> getModuleOpt(Class<T> moduleId)
	{
		final AModule aModule = modules.get(moduleId);
		if (aModule != null)
		{
			return Optional.of((T) aModule);
		}
		return modules.values().stream()
				.filter(m -> moduleId.isAssignableFrom(m.getClass()))
				.map(a -> (T) a)
				.findFirst();
	}


	/**
	 * Check whether a module is loaded.
	 *
	 * @param moduleId the Class of the module
	 * @return if the module is loaded
	 */
	public boolean isModuleLoaded(Class<? extends AModule> moduleId)
	{
		return modules.containsKey(moduleId)
				|| modules.values().stream().map(Object::getClass).anyMatch(c -> c.equals(moduleId));
	}


	/**
	 * Creates an object from a clazz.
	 *
	 * @param clazz
	 */
	private Object createObject(final Class<? extends AModule> clazz)
	{
		try
		{
			Constructor<? extends AModule> constructor = clazz.getDeclaredConstructor();
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
		         | IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Error constructing module", e);
		}
	}


	private String moduleMessage(int moduleNumber, String property)
	{
		return "module(" + moduleNumber + ")" + property;
	}
}
