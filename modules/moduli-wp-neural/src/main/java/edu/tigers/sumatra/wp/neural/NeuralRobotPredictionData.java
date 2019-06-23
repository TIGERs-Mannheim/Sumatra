/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;


/**
 * This class contains all important prediction data for the Robots.
 * 
 * @author KaiE
 */
public class NeuralRobotPredictionData implements INeuralPredicitonData
{
	
	private long		lastUpdate	= 0;
	private CamRobot	lastData;
	private IVector2	pos			= AVector2.ZERO_VECTOR;
	private IVector2	vel			= AVector2.ZERO_VECTOR;
	private IVector2	acc			= AVector2.ZERO_VECTOR;
	private double		orient		= 0;
	private double		orientVel	= 0;
	private double		orientAcc	= 0;
	private BotID		id;
	
	
	/**
	 * Method to set and update the data
	 * 
	 * @param id
	 * @param cr
	 * @param time
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param orient
	 * @param orientVel
	 * @param orientAcc
	 */
	public void update(final BotID id, final CamRobot cr, final long time,
			final IVector2 pos, final IVector2 vel, final IVector2 acc, final double orient,
			final double orientVel, final double orientAcc)
	{
		lastUpdate = time;
		lastData = cr;
		this.id = id;
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.orient = orient;
		this.orientVel = orientVel;
		this.orientAcc = orientAcc;
	}
	
	
	/**
	 * @return the lastUpdate
	 */
	public long getUpdateTimestamp()
	{
		return lastUpdate;
	}
	
	
	/**
	 * @return the lastData
	 */
	public CamRobot getLastData()
	{
		return lastData;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the velocity
	 */
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @return the acceleration
	 */
	public IVector2 getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @return the orient
	 */
	public double getOrient()
	{
		return orient;
	}
	
	
	/**
	 * @return the orientVel
	 */
	public double getOrientVel()
	{
		return orientVel;
	}
	
	
	/**
	 * @return the orientAcc
	 */
	public double getOrientAcc()
	{
		return orientAcc;
	}
	
	
	/**
	 * @return the id
	 */
	public BotID getId()
	{
		return id;
	}
}
