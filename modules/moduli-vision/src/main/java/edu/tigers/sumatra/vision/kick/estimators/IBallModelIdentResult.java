/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import java.util.Map;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public interface IBallModelIdentResult
{
	/**
	 * Get the type of model.
	 * 
	 * @return
	 */
	EBallModelIdentType getType();
	
	
	/**
	 * Kick position [mm]
	 * 
	 * @return
	 */
	IVector2 getKickPosition();
	
	
	/**
	 * Kick timestamp
	 * 
	 * @return
	 */
	long getKickTimestamp();
	
	
	/**
	 * Kick velocity [mm/s]
	 * 
	 * @return
	 */
	IVector3 getKickVelocity();
	
	
	/**
	 * Name/Value pairs of parameters.
	 * 
	 * @return
	 */
	Map<String, Double> getModelParameters();
}
