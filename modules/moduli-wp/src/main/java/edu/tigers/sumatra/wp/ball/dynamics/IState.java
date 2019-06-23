/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import Jama.Matrix;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IState
{
	
	/**
	 * @return the pos [mm]
	 */
	IVector3 getPos();
	
	
	/**
	 * @return the vel [m/s]
	 */
	IVector3 getVel();
	
	
	/**
	 * @return the acc [m/s^2]
	 */
	IVector3 getAcc();
	
	
	/**
	 * get state in Matrix form
	 * 
	 * @return Matrix: [p v a confidence]
	 */
	default Matrix getStateMatrix()
	{
		double[] state = { getPos().x() * 1e-3, getPos().y() * 1e-3, getPos().z() * 1e-3, getVel().x(), getVel().y(),
				getVel().z(),
				getAcc().x(), getAcc().y(), getAcc().z(), 1.0 };
		return new Matrix(state, 10);
	}
	
}