/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.data;

import java.util.Map;

import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;


/**
 * Sample for ball model identification.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BallModelSample
{
	private final EBallModelIdentType type;
	private final Map<String, Double> parameters;
	private boolean sampleUsed = true;
	
	
	/** For jackson binding */
	protected BallModelSample()
	{
		type = EBallModelIdentType.STRAIGHT_TWO_PHASE;
		parameters = null;
	}
	
	
	/**
	 * @param type
	 * @param parameters
	 */
	public BallModelSample(final EBallModelIdentType type, final Map<String, Double> parameters)
	{
		this.type = type;
		this.parameters = parameters;
	}
	
	
	/**
	 * @return the type
	 */
	public EBallModelIdentType getType()
	{
		return type;
	}
	
	
	/**
	 * @return the parameters
	 */
	public Map<String, Double> getParameters()
	{
		return parameters;
	}
	
	
	/**
	 * @return the sampleUsed
	 */
	public boolean isSampleUsed()
	{
		return sampleUsed;
	}
	
	
	/**
	 * @param sampleUsed the sampleUsed to set
	 */
	public void setSampleUsed(final boolean sampleUsed)
	{
		this.sampleUsed = sampleUsed;
	}
}
