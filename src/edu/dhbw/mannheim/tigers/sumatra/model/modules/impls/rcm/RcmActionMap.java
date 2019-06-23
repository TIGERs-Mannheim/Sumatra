/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RcmAction.EActionType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Store action mapping for controller
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RcmActionMap
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger							log				= Logger
																								.getLogger(RcmActionMap.class.getName());
	private static final String							CONFIG_DIR		= "config/rcm/";
	private static final String							CONFIG_ENDING	= ".rcc";
	private static final String							ENCODING			= "UTF-8";
	
	private final List<RcmActionMapping>				actionMappings	= new ArrayList<RcmActionMapping>();
	private final Controller								controller;
	private String												configName		= "default";
	
	private final Map<ERcmControllerConfig, Float>	configValues	= new LinkedHashMap<ERcmControllerConfig, Float>();
	
	
	/**
	 */
	public enum ERcmControllerConfig
	{
		/**  */
		DEADZONE,
		/**  */
		SPEED_DAMP,
		/**  */
		BREAK_DAMP;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param controller
	 */
	public RcmActionMap(final Controller controller)
	{
		this.controller = controller;
		configValues.put(ERcmControllerConfig.DEADZONE, 0.0f);
		configValues.put(ERcmControllerConfig.SPEED_DAMP, 1.0f);
		configValues.put(ERcmControllerConfig.BREAK_DAMP, 1.0f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Put a mapping for a skill
	 * 
	 * @param identifiers
	 * @param eSkill
	 */
	public void addMapping(final List<ExtIdentifier> identifiers, final ESkillName eSkill)
	{
		RcmAction action = new RcmAction(eSkill, EActionType.SKILL);
		RcmActionMapping mapping = new RcmActionMapping(identifiers, action);
		actionMappings.add(mapping);
	}
	
	
	/**
	 * @param identifiers
	 * @param eAction
	 */
	public void addMapping(final List<ExtIdentifier> identifiers, final EControllerAction eAction)
	{
		RcmAction action = new RcmAction(eAction, EActionType.SIMPLE);
		RcmActionMapping mapping = new RcmActionMapping(identifiers, action);
		actionMappings.add(mapping);
	}
	
	
	/**
	 * @param mapping
	 */
	public void addMapping(final RcmActionMapping mapping)
	{
		actionMappings.add(mapping);
	}
	
	
	/**
	 * @param mapping
	 */
	public void removeMapping(final RcmActionMapping mapping)
	{
		actionMappings.remove(mapping);
	}
	
	
	/**
	 * @param file
	 */
	public void save(final File file)
	{
		Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
		List<JSONObject> jsonArray = new ArrayList<JSONObject>(actionMappings.size());
		for (RcmActionMapping mapping : actionMappings)
		{
			jsonArray.add(mapping.toJSON());
		}
		for (Map.Entry<ERcmControllerConfig, Float> entry : configValues.entrySet())
		{
			jsonMap.put(entry.getKey().name(), entry.getValue());
		}
		jsonMap.put("mapping", jsonArray);
		
		String json = JSONValue.toJSONString(jsonMap);
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));
			bw.write(json);
			
		} catch (IOException err)
		{
			log.error("Could not save config.", err);
		} finally
		{
			if (bw != null)
			{
				try
				{
					bw.close();
				} catch (IOException err)
				{
					log.error("Could not close config file " + file, err);
				}
			}
		}
	}
	
	
	/**
	 * @param file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean load(final File file)
	{
		JSONParser parser = new JSONParser();
		String json = "";
		try
		{
			json = new String(Files.readAllBytes(file.toPath()), ENCODING);
			Object obj = parser.parse(json);
			Map<String, Object> jsonMap = (Map<String, Object>) obj;
			
			List<JSONObject> jsonArray = (List<JSONObject>) jsonMap.get("mapping");
			actionMappings.clear();
			for (JSONObject jsonObj : jsonArray)
			{
				RcmActionMapping mapping = RcmActionMapping.fromJSON(jsonObj);
				actionMappings.add(mapping);
			}
			for (Map.Entry<ERcmControllerConfig, Float> entry : configValues.entrySet())
			{
				configValues.put(entry.getKey(), new Float((Double) jsonMap.get(entry.getKey().name())));
			}
			configName = file.getName();
			log.info("Loaded " + file.getName());
		} catch (IOException err)
		{
			log.error("Could not load config from " + file, err);
			return false;
		} catch (ParseException err)
		{
			log.error("Could not parse json: " + json, err);
			return false;
		} catch (IllegalArgumentException err)
		{
			log.error("Error loading config", err);
			return false;
		}
		return true;
	}
	
	
	private File getDefaultFile(final Controller controller)
	{
		String ctrlName = controller.getName().replaceAll("[ /\\\\]", "_");
		File file = Paths.get(CONFIG_DIR, ctrlName + CONFIG_ENDING).toFile();
		return file;
	}
	
	
	/**
	 * @param controller
	 */
	public void loadDefault(final Controller controller)
	{
		File file = getDefaultFile(controller);
		if (file.exists() && load(file))
		{
			return;
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
		List<ExtComponent> comps = new LinkedList<ExtComponent>();
		for (RcmActionMapping mapping : actionMappings)
		{
			ExtComponent component = null;
			for (ExtIdentifier extId : mapping.getIdentifiers())
			{
				for (Component comp : controller.getComponents())
				{
					ExtComponent newComp = null;
					if (extId.getIdentifier().equals(comp.getIdentifier().toString()))
					{
						if (comp.isAnalog())
						{
							float min = extId.getParams().getMinValue();
							float max = extId.getParams().getMaxValue();
							newComp = new ExtComponent(new DynamicAxis(comp, min, max), mapping.getAction());
						}
						else
						{
							float chargeTime = extId.getParams().getChargeTime();
							newComp = new ExtComponent(new ChargeButtonComponent(comp, chargeTime), mapping.getAction());
						}
					} else if (comp.getIdentifier().toString().equals("pov") && extId.getIdentifier().startsWith("pov"))
					{
						newComp = new ExtComponent(new POVToButton(comp, extId.getIdentifier()), mapping.getAction());
					}
					
					if (newComp != null)
					{
						if (component == null)
						{
							comps.add(newComp);
						} else
						{
							component.setDependentComp(newComp);
						}
						component = newComp;
						break;
					}
				}
			}
		}
		return comps;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the configName
	 */
	public String getConfigName()
	{
		return configName;
	}
	
	
	/**
	 * @param configName the configName to set
	 */
	public void setConfigName(final String configName)
	{
		this.configName = configName;
	}
	
	
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
	public Map<ERcmControllerConfig, Float> getConfigValues()
	{
		return configValues;
	}
}
