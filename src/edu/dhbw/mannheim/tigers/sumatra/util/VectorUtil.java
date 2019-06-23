/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/**
 * Util class for vectors
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class VectorUtil
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Get a vector from a config.
	 * 
	 * @param config
	 * @return
	 */
	public static Vector2 configToVector2(SubnodeConfiguration config)
	{
		float x = config.getFloat("x", 0.0f);
		float y = config.getFloat("y", 0.0f);
		return new Vector2(x, y);
	}
	
	
	/**
	 * Get a vector from a config.
	 * 
	 * @param config
	 * @return
	 */
	public static Vector3 configToVector3(SubnodeConfiguration config)
	{
		float x = config.getFloat("x", 0.0f);
		float y = config.getFloat("y", 0.0f);
		float z = config.getFloat("z", 0.0f);
		return new Vector3(x, y, z);
	}
	
	
	/**
	 * Create config from vector
	 * 
	 * @param v
	 * @return
	 */
	public static HierarchicalConfiguration vector2ToConfig(IVector2 v)
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("x", v.x());
		config.addProperty("y", v.y());
		
		return config;
	}
	
	
	/**
	 * Create config from vector
	 * 
	 * @param v
	 * @return
	 */
	public static HierarchicalConfiguration vector3ToConfig(IVector3 v)
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("x", v.x());
		config.addProperty("y", v.y());
		config.addProperty("z", v.z());
		
		return config;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
