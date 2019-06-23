/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.FilterSelector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class shares everything within the WorldPredictor what is necessary to perform the prediction.
 * 
 * @author Gero
 * @author Peter
 * @author Maren
 */
public class PredictionContext
{
	/** used for prediction and updates */
	/** The currently detected TIGER-bots */
	private final Map<Integer, IFilter>				yellowBots;
	/** The currently detected foe-bots */
	private final Map<Integer, IFilter>				blueBots;
	/** The currently detected balls */
	private IFilter										ball;
	
	/** The new detected TIGER-bots */
	private final Map<Integer, UnregisteredBot>	newYellowBots;
	/** The new detected foe-bots */
	private final Map<Integer, UnregisteredBot>	newBlueBots;
	
	@Configurable(comment = "time which one lookahead prediction step should look ahead (in internalTime)")
	private static double								stepSize				= 10e-3;
	@Configurable(comment = "number of lookahead steps which should be performed")
	private static int									stepCount			= 5;
	@Configurable(comment = "number of particles used in particle filter")
	private static int									numberParticle		= 500;
	@Configurable(comment = "threshold for particle filter when to resample")
	private static double								pfESS					= 0.2;
	@Configurable
	private static String								resampler			= "stratified";
	@Configurable(comment = "constant offset between capture time and now")
	private static double								filterTimeOffset	= 0.001;
	
	
	/**
	 */
	public PredictionContext()
	{
		// maps an id (integer) to a ObjectData, maximum 10 elements, 100% load
		yellowBots = new ConcurrentHashMap<Integer, IFilter>(10, 1);
		blueBots = new ConcurrentHashMap<Integer, IFilter>(10, 1);
		
		newYellowBots = new HashMap<Integer, UnregisteredBot>(10, 1);
		newBlueBots = new HashMap<Integer, UnregisteredBot>(10, 1);
		
		reset();
	}
	
	
	/**
	 * Clear all maps and resets the rest to default
	 */
	public void reset()
	{
		getYellowBots().clear();
		getBlueBots().clear();
		
		IFilter filter = FilterSelector.getBallFilter();
		filter.init(new BallMotionModel(this), this, SumatraClock.nanoTime(), new WPCamBall(CamBall.defaultInstance()));
		setBall(filter);
		getNewYellowBots().clear();
		getNewBlueBots().clear();
	}
	
	
	/**
	 * @return the stepCount
	 */
	public int getStepCount()
	{
		return stepCount;
	}
	
	
	/**
	 * @return the stepSize
	 */
	public double getStepSize()
	{
		return stepSize;
	}
	
	
	/**
	 * @return the numberParticle
	 */
	public final int getNumberParticle()
	{
		return numberParticle;
	}
	
	
	/**
	 * @return the resampler
	 */
	public final String getResampler()
	{
		return resampler;
	}
	
	
	/**
	 * @return the pfESS
	 */
	public final double getPfESS()
	{
		return pfESS;
	}
	
	
	/**
	 * @return the yellowBots
	 */
	public Map<Integer, IFilter> getYellowBots()
	{
		return yellowBots;
	}
	
	
	/**
	 * @return the blueBots
	 */
	public Map<Integer, IFilter> getBlueBots()
	{
		return blueBots;
	}
	
	
	/**
	 * @return the ball
	 */
	public IFilter getBall()
	{
		return ball;
	}
	
	
	/**
	 * @param ball the ball to set
	 */
	public void setBall(final IFilter ball)
	{
		this.ball = ball;
	}
	
	
	/**
	 * @return the newYellowBots
	 */
	public Map<Integer, UnregisteredBot> getNewYellowBots()
	{
		return newYellowBots;
	}
	
	
	/**
	 * @return the newBlueBots
	 */
	public Map<Integer, UnregisteredBot> getNewBlueBots()
	{
		return newBlueBots;
	}
	
	
	/**
	 * @return the filterTimeOffset
	 */
	public double getFilterTimeOffset()
	{
		return filterTimeOffset;
	}
}
