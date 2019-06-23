/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;


/**
 * This class shares everything within the WorldPredictor what is necessary to perform the prediction.
 * 
 * @author Gero
 * @author Peter
 * @author Maren
 * 
 */
public class PredictionContext
{
	/** used for prediction and updates */
	/** The currently detected TIGER-bots */
	public final Map<Integer, IFilter>					tigers;
	/** The currently detected foe-bots */
	public final Map<Integer, IFilter>						food;
	/** The currently detected balls */
	public 		 IFilter										ball;
	
	/** used for tracking-manager */
	/** The new detected TIGER-bots */
	public final Map<Integer, UnregisteredBot> 				newTigers;
	/** The new detected foe-bots */
	public final Map<Integer, UnregisteredBot>				newFood;
	/** The new detected balls */
	public 		UnregisteredBall									newBall;
	
	/** used for lookahead predictions and to determine time to predict */
	/** time which one lookahead prediction step should look ahead (in internalTime)*/
	public final double													stepSize;
	/** number of lookahead steps which should be performed */
	public final int													stepCount;
	/** number of particles used in particle filter */
	public final int													numberParticle;
	/** threshold for particle filter when to resample */
	public final double												pfESS;
	public final String												resampler;
	
	/** used for cam-frame merging */
	public final int 													numberCams;

	public final int													minTigersInWorldFrame;
	public final int													minFoodInWorldFrame;
	
	/** time-stamp of last incoming frame */
	private volatile double											latestCaptureTimestamp;
	
	
	public PredictionContext(SubnodeConfiguration properties)
	{
		// maps an id (integer) to a ObjectData, maximum 10 elements, 100% load
		tigers = new HashMap<Integer, IFilter>(10, 1);
		food = new HashMap<Integer, IFilter>(10, 1);
		ball = null;
		
		newTigers = new HashMap<Integer, UnregisteredBot>(10, 1);
		newFood = new HashMap<Integer, UnregisteredBot>(10, 1);
		newBall = null;
		
		stepSize = properties.getLong("stepSize", 10)*1000000*WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;//convert from ms to ns
		stepCount = properties.getInt("stepCount", 5);
		
		numberParticle = properties.getInt("numberParticle", 500);
		pfESS = properties.getDouble("ESS", 0.2);
		resampler = properties.getString("resampler", "stratified");
		
		numberCams = properties.getInt("camNo", 2);
		
		minTigersInWorldFrame = properties.getInt("minTigersInWorldFrame", 5);
		minFoodInWorldFrame = properties.getInt("minFoesInWorldFrame", 5);
		reset();
	}

	/**
	 * Clear all maps and resets the rest to default
	 */
	public void reset()
	{
		tigers.clear();
		food.clear();
		ball = null;
		newTigers.clear();
		newFood.clear();
		newBall = null;
		
		latestCaptureTimestamp = -1.0*Double.MAX_VALUE;
	}

	/**
	 * @return The time-stamp of the latest frame processed
	 */
	public double getLatestCaptureTimestamp()
	{
		return latestCaptureTimestamp;
	}
	

	/**
	 * @return The time-stamp of the latest frame processed
	 */
	public void setLatestCaptureTimestamp(double latestCaptureTimestamp)
	{
		this.latestCaptureTimestamp = latestCaptureTimestamp;
	}
}
