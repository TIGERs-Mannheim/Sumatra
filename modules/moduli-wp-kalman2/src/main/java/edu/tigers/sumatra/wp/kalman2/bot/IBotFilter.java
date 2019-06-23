/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 4, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2.bot;

import edu.tigers.sumatra.math.vector.IVector3;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBotFilter
{
	
	/**
	 * @param pos
	 * @param timestamp
	 */
	void update(IVector3 pos, long timestamp);
	
	
	/**
	 * @param timestamp
	 */
	void predict(long timestamp);
	
	
	/**
	 * @param control
	 */
	void setControl(IVector3 control);
	
	
	/**
	 * @return
	 */
	IVector3 getPos();
	
	
	/**
	 * @return
	 */
	IVector3 getVel();
	
	
	/**
	 * @return
	 */
	IVector3 getAcc();
	
	
	/**
	 * @return
	 */
	IVector3 getCurAcc();
	
	
	/**
	 * @return
	 */
	IVector3 getCurVel();
	
	
	/**
	 * @return
	 */
	IVector3 getCurPos();
	
	
	/**
	 * @return
	 */
	long getCurTimestamp();
	
}