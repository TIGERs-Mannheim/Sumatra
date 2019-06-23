/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions.LoadConfigException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObserver;


/**
 * This holds some static variables to parameterize the AI
 * hard choices - null == usual procedures in classes
 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte
 */
public class AIConfig implements ICamGeomObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** Senseless Vector. Vector2f(42000,42000). Use it to initialize your vector. */
	public static final Vector2f	INIT_VECTOR		= new Vector2f(42000, 42000);
	
	/** Senseless Vector. Vector3f(42000,42000). Use it to initialize your vector. */
	public static final Vector3f	INIT_VECTOR3	= new Vector3f(42000, 42000, 42000);
	

	protected final Log				log				= LogFactory.getLog(this.getClass().getName());
	
	private XMLConfiguration		aiXMLConfig;
	private XMLConfiguration		tacticsXMLConfig;
	
	private General					general;
	private Tolerances				tolerances;
	private Errt						errt;
	private Gui							gui;
	private volatile Geometry		geometry;
	private FieldRaster				fieldRaster;
	private Skills						skills;
	private Roles						roles;
	private Plays						plays;
	private Calculators				calucators;
	private AthenaConfig				athenaConfig;
	
	private Tactics					tactics;
	
	private boolean					loaded;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class AIConfigHolder
	{
		private static final AIConfig	CONFIG	= new AIConfig();
	}
	
	
	private AIConfig()
	{
		loaded = false;
	}
	

	public static AIConfig getInstance()
	{
		return AIConfigHolder.CONFIG;
	}
	

	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Function to load ai configuration from file.
	 * 
	 * @param xmlFile Path to configuration file.
	 * @throws Throws LoadConfigException when configuration file cannot be read or configuration is invalid.
	 */
	public void loadAIConfig(String xmlFile) throws LoadConfigException
	{
		try
		{
			aiXMLConfig = new XMLConfiguration();
			aiXMLConfig.setDelimiterParsingDisabled(true);
			aiXMLConfig.load(xmlFile);
			aiXMLConfig.setFileName(xmlFile.substring(xmlFile.lastIndexOf('/')+1));
			
			general = new General(aiXMLConfig);
			tolerances = new Tolerances(aiXMLConfig);
			gui = new Gui(aiXMLConfig);
			geometry = new Geometry(aiXMLConfig);
			errt = new Errt(aiXMLConfig);
			fieldRaster = new FieldRaster(aiXMLConfig);
			skills = new Skills(aiXMLConfig);
			calucators = new Calculators(aiXMLConfig);
			athenaConfig = new AthenaConfig(aiXMLConfig);
			
			loaded = true;
		} catch (Exception e)
		{
			log.error("Error loading AI-Config!", e);
			throw new RuntimeException("Error loading AI-Config!", e);
		}
	}
	

	/**
	 * Stores the actual aiConfiguration to file. Nothing happens when
	 * no configuration has been loaded.
	 * 
	 * @throws ConfigurationException
	 */
	public void saveAIConfig() throws ConfigurationException
	{
		if (aiXMLConfig != null)
		{
			aiXMLConfig.save();
		} else
		{
			log.error("Error while saving AI-Config! Reason: no configuration loaded yet");
		}
	}
	

	/**
	 * Function to load tactics configuration from file.
	 * 
	 * @param xmlFile Path to configuration file.
	 * @throws Throws LoadConfigException when configuration file cannot be read or configuration is invalid.
	 * @author Malte
	 */
	public void loadTacticsConfig(String xmlFile) throws LoadConfigException
	{
		try
		{
			tacticsXMLConfig = new XMLConfiguration(xmlFile);
			tactics = new Tactics(tacticsXMLConfig);
			roles = new Roles(tacticsXMLConfig);
			plays = new Plays(tacticsXMLConfig);
			
		} catch (Exception e)
		{
			log.error("Error loading Tactics-Config!", e);
			throw new RuntimeException("Error loading Tactics-Config!", e);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- private-method(s) ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the general configuration
	 */
	public static General getGeneral()
	{
		return AIConfig.getInstance().general;
	}
	

	/**
	 * @return the tolerances configuration
	 */
	public static Tolerances getTolerances()
	{
		return AIConfig.getInstance().tolerances;
	}
	

	/**
	 * @return the errt configuration
	 */
	public static Errt getErrt()
	{
		return AIConfig.getInstance().errt;
	}
	

	/**
	 * @return the gui configuration
	 */
	public static Gui getGui()
	{
		return AIConfig.getInstance().gui;
	}
	

	/**
	 * @return geometry values
	 */
	public static Geometry getGeometry()
	{
		return AIConfig.getInstance().geometry;
	}
	

	/**
	 * @return the field raster configuration
	 */
	public static FieldRaster getFieldRaster()
	{
		return AIConfig.getInstance().fieldRaster;
	}
	

	/**
	 * @return the skill configuration
	 */
	public static Skills getSkills()
	{
		return AIConfig.getInstance().skills;
	}
	

	/**
	 * @return the role configuration
	 */
	public static Roles getRoles()
	{
		return AIConfig.getInstance().roles;
	}
	

	/**
	 * @return the play configuration
	 */
	public static Plays getPlays()
	{
		return AIConfig.getInstance().plays;
	}
	

	/**
	 * @return the skill configuration
	 */
	public static Calculators getCalculators()
	{
		return AIConfig.getInstance().calucators;
	}
	

	public static AthenaConfig getAthenaConfig()
	{
		return AIConfig.getInstance().athenaConfig;
	}
	

	public static Tactics getTactics()
	{
		return AIConfig.getInstance().tactics;
	}
	

	@Override
	public void update(ICamGeomObservable observable, CamGeometryFrame event)
	{
		Geometry newGeom = new Geometry(event.fieldGeometry, AIConfig.getInstance().geometry);
		AIConfig.getInstance().geometry = newGeom;
	}
	
	public static boolean isLoaded()
	{
		return AIConfig.getInstance().loaded;
	}
}
