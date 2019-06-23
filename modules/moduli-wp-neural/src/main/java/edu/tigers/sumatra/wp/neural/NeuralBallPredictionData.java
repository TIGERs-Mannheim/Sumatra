/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.math.IVector3;


/**
 * This class contains all predicted data for the ball by the neural network
 * 
 * @author KaiE
 */
public class NeuralBallPredictionData implements INeuralPredicitonData
{
	private IVector3	pos;
	private IVector3	vel;
	private IVector3	acc;
	private CamBall	lastball;
	private long		timestamp;
	
	
	/**
	 * Setter to update the data
	 * 
	 * @param p
	 * @param v
	 * @param a
	 * @param cb
	 * @param time
	 */
	public void update(final IVector3 p, final IVector3 v, final IVector3 a, final CamBall cb, final long time)
	{
		pos = p;
		vel = v;
		acc = a;
		lastball = cb;
		timestamp = time;
	}
	
	
	/**
	 * @return the timestamp of the ball
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the position
	 */
	public IVector3 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the velocity
	 */
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @return the acceleration
	 */
	public IVector3 getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @return the last ball
	 */
	public CamBall getLastball()
	{
		return lastball;
	}
}
