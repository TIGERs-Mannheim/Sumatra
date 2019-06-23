/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 10, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control;

import Jama.Matrix;


/**
 * Interface for classes that can calculate an action from given state
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPolicyController
{
	/**
	 * @param state
	 * @return
	 */
	Matrix getControl(final Matrix state);
	
	
	/**
	 * Wrapper method that calculates the input for getControl(Matrix).
	 * 
	 * @param destination
	 * @param bot
	 * @return
	 */
	// default IVector3 getControl(final IVector3 destination, final TrackedBot bot)
	// {
	// IVector2 error = destination.getXYVector().subtractNew(bot.getPos()).multiply(0.001f);
	// double errorW = AngleMath.getShortestRotation(bot.getAngle(), destination.z());
	// double[] stateArr = new double[] { error.x(), error.y(), errorW, bot.getVel().x(), bot.getVel().y(),
	// bot.getaVel() };
	// Matrix state = new Matrix(stateArr, 1);
	// Matrix u = getControl(state);
	// return new Vector3(u.get(0, 0), u.get(0, 1), u.get(0, 2));
	// }
	
	
	/**
	 * @return
	 */
	int getStateDimension();
	
	
	/**
	 * @return
	 */
	double getDt();
}
