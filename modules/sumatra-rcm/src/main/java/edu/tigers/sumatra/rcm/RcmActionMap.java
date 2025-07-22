/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Store action mapping for controller
 */
public class RcmActionMap
{
	private static final Logger log = LogManager.getLogger(RcmActionMap.class.getName());
	private static final String CONFIG_DIR = "config/rcm/";
	private static final String CONFIG_ENDING = ".rcc";
	private static final String ENCODING = "UTF-8";

	private final List<RcmActionMapping> actionMappings = new ArrayList<>();
	private final Controller controller;
	private final Map<ERcmControllerConfig, Double> configValues = new EnumMap<>(ERcmControllerConfig.class);
	private String configName = "default";
	private boolean enabled = true;


	public RcmActionMap(final Controller controller)
	{
		this.controller = controller;
		configValues.put(ERcmControllerConfig.DEADZONE, 0.0);
	}


	public void addMapping(final RcmActionMapping mapping)
	{
		actionMappings.add(mapping);
	}


	public void removeMapping(final RcmActionMapping mapping)
	{
		actionMappings.remove(mapping);
	}


	public void save(final File file)
	{
		Map<String, Object> jsonMap = new LinkedHashMap<>();
		List<JsonObject> jsonArray = new ArrayList<>(actionMappings.size());
		for (RcmActionMapping mapping : actionMappings)
		{
			jsonArray.add(mapping.toJSON());
		}
		for (Map.Entry<ERcmControllerConfig, Double> entry : configValues.entrySet())
		{
			jsonMap.put(entry.getKey().name(), entry.getValue());
		}
		jsonMap.put("mapping", jsonArray);

		String json = Jsoner.serialize(jsonMap);
		try
		{
			Files.write(Paths.get(file.getAbsolutePath()), json.getBytes());
		} catch (IOException err)
		{
			log.error("Could not save config.", err);
		}
	}


	@SuppressWarnings("unchecked")
	public void load(final File file)
	{
		String json = "";
		try
		{
			json = new String(Files.readAllBytes(file.toPath()), ENCODING);
			Object obj = Jsoner.deserialize(json);
			Map<String, Object> jsonMap = (Map<String, Object>) obj;

			List<JsonObject> jsonArray = (List<JsonObject>) jsonMap.get("mapping");
			actionMappings.clear();
			for (JsonObject jsonObj : jsonArray)
			{
				RcmActionMapping mapping = RcmActionMapping.fromJSON(jsonObj);
				actionMappings.add(mapping);
			}
			configValues.replaceAll((k, v) -> (Double) jsonMap.get(k.name()));
			configName = file.getName();
			log.info("Loaded {}", file.getName());
		} catch (IOException err)
		{
			log.error("Could not load config from {}", file, err);
		} catch (JsonException err)
		{
			log.error("Could not parse json: {}", json, err);
		} catch (IllegalArgumentException err)
		{
			log.error("Error loading config", err);
		}
	}


	private File getDefaultFile(final Controller controller)
	{
		String ctrlName = controller.getName().replaceAll("[ /\\\\]", "_");
		return Paths.get(CONFIG_DIR, ctrlName + CONFIG_ENDING).toFile();
	}


	/**
	 * @param controller
	 */
	public void loadDefault(final Controller controller)
	{
		File file = getDefaultFile(controller);
		if (file.exists())
		{
			load(file);
		}
	}


	/**
	 * @param controller
	 */
	public void saveDefault(final Controller controller)
	{
		File file = getDefaultFile(controller);
		save(file);
	}


	/**
	 * Create all used components and adapt them if necessary
	 *
	 * @return
	 */
	public List<ExtComponent> createComponents()
	{
		List<ExtComponent> comps = new LinkedList<>();
		for (RcmActionMapping mapping : actionMappings)
		{
			populateComponent(comps, mapping);
		}
		return comps;
	}


	private void populateComponent(final List<ExtComponent> comps, final RcmActionMapping mapping)
	{
		ExtComponent component = null;
		for (ExtIdentifier extId : mapping.getIdentifiers())
		{
			for (Component comp : controller.getComponents())
			{
				ExtComponent newComp = getNewComponent(mapping, extId, comp);

				if (newComp != null)
				{
					processDependency(comps, component, newComp);
					component = newComp;
					break;
				}
			}
		}
	}


	private void processDependency(final List<ExtComponent> comps, final ExtComponent component,
			final ExtComponent newComp)
	{
		if (component == null)
		{
			comps.add(newComp);
		} else
		{
			component.setDependentComp(newComp);
		}
	}


	private ExtComponent getNewComponent(final RcmActionMapping mapping, final ExtIdentifier extId, final Component comp)
	{
		ExtComponent newComp = null;
		if (extId.getIdentifier().equals(comp.getIdentifier().toString()))
		{
			if (comp.isAnalog())
			{
				double min = extId.getParams().getMinValue();
				double max = extId.getParams().getMaxValue();
				newComp = new ExtComponent(new DynamicAxis(comp, min, max), mapping.getAction());
			} else
			{
				double chargeTime = extId.getParams().getChargeTime();
				newComp = new ExtComponent(new ChargeButtonComponent(comp, chargeTime), mapping.getAction());
			}
		} else if ("pov".equals(comp.getIdentifier().toString()) && extId.getIdentifier().startsWith("pov"))
		{
			newComp = new ExtComponent(new POVToButton(comp, extId.getIdentifier()), mapping.getAction());
		}
		return newComp;
	}


	/**
	 * @return the configName
	 */
	public String getConfigName()
	{
		return configName;
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * @return the controller
	 */
	public Controller getController()
	{
		return controller;
	}


	/**
	 * @return the actionMappings
	 */
	public final List<RcmActionMapping> getActionMappings()
	{
		return Collections.unmodifiableList(actionMappings);
	}


	/**
	 * @return the configValues
	 */
	public Map<ERcmControllerConfig, Double> getConfigValues()
	{
		return configValues;
	}


	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;
	}


	public boolean isEnabled()
	{
		return enabled;
	}


	/**
	 * custom controller configs
	 */
	public enum ERcmControllerConfig
	{
		DEADZONE,
	}
}
